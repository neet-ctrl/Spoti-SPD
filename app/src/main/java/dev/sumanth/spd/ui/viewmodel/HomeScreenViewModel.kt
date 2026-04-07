package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.DownloadHistoryItem
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.utils.DownloadHistoryManager
import dev.sumanth.spd.utils.DownloadManager
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

enum class Status {
    IDLE,
    SCRAPING,
    SCRAPED,
    DOWNLOADING,
    RETRYING,
    COMPLETED,
}
class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    var fileProgress by mutableFloatStateOf(0f)
    var totalProgress by mutableFloatStateOf(0f)
    var scrapingProgress by mutableFloatStateOf(0f)
    var fileName by mutableStateOf("")
    var spotifyLink by mutableStateOf("")
    var convertToMp3 by mutableStateOf(false)
    private val sharedPref = SharedPref(application)
    private val historyManager = DownloadHistoryManager(application)
    private var downloadJob: Job? = null
    
    var downloadHistory by mutableStateOf(listOf<DownloadHistoryItem>())
    var currentDownload by mutableStateOf<DownloadHistoryItem?>(null)
    
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
    
    val failedTracks = mutableListOf<Track>()
    var appStatus by mutableStateOf(Status.IDLE)
    var spotifyList by mutableStateOf(JSONArray())

    init {
        loadHistory()
    }

    fun loadHistory() {
        downloadHistory = historyManager.getHistory()
    }

    fun pasteFromClipboard() {
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text.toString()
                val normalizedUrl = normalizeSpotifyLink(text)
                if (!normalizedUrl.isNullOrBlank()) {
                    spotifyLink = normalizedUrl
                    Toast.makeText(getApplication(), "Pasted Spotify link", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(getApplication(), "Not a valid Spotify link", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Failed to paste: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeSpotifyLink(rawLink: String): String? {
        val link = rawLink.trim()
        if (link.startsWith("intent://")) {
            val fallback = Regex("S\\.browser_fallback_url=([^;]+)").find(link)?.groups?.get(1)?.value
            if (!fallback.isNullOrEmpty()) {
                return Uri.decode(fallback)
            }
        }
        if (link.startsWith("spotify:")) {
            val path = link.removePrefix("spotify:").replace(":", "/")
            return "https://open.spotify.com/$path"
        }
        if (link.contains("open.spotify.com") || link.contains("spotify.com")) {
            return link
        }
        return null
    }

    fun startScraping() {
        if (spotifyLink.isBlank()) return Toast.makeText(getApplication(), "Spotify link is invalid.", Toast.LENGTH_SHORT).show()
        appStatus = Status.SCRAPING
        scrapingProgress = 0f
    }

    fun downloadPlaylist() {
        if(spotifyList.length() == 0) return Toast.makeText(getApplication(), "Playlist is empty.", Toast.LENGTH_SHORT).show()
        downloadJob = viewModelScope.launch {
            appStatus = Status.DOWNLOADING
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()
                
                // Create history item
                currentDownload = DownloadHistoryItem(
                    spotifyUrl = spotifyLink,
                    title = "Downloading...",
                    artist = "Multiple songs",
                    totalTracks = spotifyList.length(),
                    filePath = downloadPath,
                    convertedToMp3 = convertToMp3,
                    status = DownloadStatus.DOWNLOADING
                )

                var successCount = 0
                for (i in 0 until spotifyList.length()) {
                    val track = spotifyList.getJSONObject(i)
                    val trackName = track.getString("title")
                    val artist = track.getString("artist")
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(trackName, artist)
                        }
                        fileName = "Downloading $trackName"
                        val path = "$downloadPath/${sanitizeFilename(trackName)}"
                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, trackName, artist)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, trackName, artist)
                            }
                        }
                        totalProgress = (i + 1).toFloat() / spotifyList.length()
                        successCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(Track(trackName, artist))
                    }
                }
                
                // Save to history
                currentDownload?.let { download ->
                    historyManager.addHistory(
                        download.copy(
                            status = if (failedTracks.isEmpty()) DownloadStatus.COMPLETED else DownloadStatus.PARTIAL,
                            successfulTracks = successCount,
                            failedTracks = failedTracks.size
                        )
                    )
                }
                loadHistory()
                appStatus = Status.COMPLETED
            } catch (e: Exception) {
                appStatus = Status.IDLE
                fileName = "Error: ${e.message}"
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        appStatus = Status.SCRAPED
        fileName = "Download cancelled"
        totalProgress = 0f
    }
    
    fun retryFailedDownloads() {
        downloadJob = viewModelScope.launch {
            appStatus = Status.DOWNLOADING
            val tracksToRetry = failedTracks.toList()
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()
                tracksToRetry.forEachIndexed { index, item ->
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(item.title, item.artist)
                        }
                        fileName = "Downloading ${item.title}"
                        val path = "$downloadPath/${sanitizeFilename(item.title)}"

                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, item.title, item.artist)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, item.title, item.artist)
                            }
                        }
                        totalProgress = (index + 1).toFloat() / tracksToRetry.size
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(item)
                    }
                }
                fileName = "Download completed"
            } catch (e: Exception) {
                fileName = "Error: ${e.message}"
            }
        }
    }
    
    fun getFailedDownloadsCount(): Int = failedTracks.size
    
    fun deleteHistoryItem(id: String) {
        historyManager.deleteHistory(id)
        loadHistory()
    }
    
    fun clearHistory() {
        historyManager.clearAllHistory()
        loadHistory()
    }
}
