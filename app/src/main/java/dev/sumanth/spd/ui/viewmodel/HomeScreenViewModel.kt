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
import java.io.File
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

enum class RepeatMode {
    NONE, ONE, ALL
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
    
    // New state for enhanced UI
    var selectedSongs by mutableStateOf(setOf<Int>())
    var isSelectionMode by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)
    var showPlayer by mutableStateOf(false)
    var currentPlayingIndex by mutableStateOf(-1)
    var isShuffleMode by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var isPlayerLoading by mutableStateOf(false)

    // Media Player state
    var currentTime by mutableFloatStateOf(0f)
    var duration by mutableFloatStateOf(0f)
    private var _volume by mutableFloatStateOf(1f)
    val volume: Float get() = _volume
    var repeatMode by mutableStateOf(RepeatMode.NONE) // NONE, ONE, ALL
    var isPlayerCollapsed by mutableStateOf(false)
    var playerOffsetX by mutableFloatStateOf(0f)
    var playerOffsetY by mutableFloatStateOf(0f)
    var isFavorite by mutableStateOf(false)

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var playbackJob: Job? = null
    
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
    
    // New methods for enhanced UI
    fun toggleSongSelection(index: Int) {
        if (isSelectionMode) {
            selectedSongs = if (selectedSongs.contains(index)) {
                selectedSongs - index
            } else {
                selectedSongs + index
            }
            if (selectedSongs.isEmpty()) {
                isSelectionMode = false
            }
        } else {
            isSelectionMode = true
            selectedSongs = setOf(index)
        }
    }
    
    fun selectAllSongs() {
        selectedSongs = (0 until spotifyList.length()).toSet()
        isSelectionMode = true
    }
    
    fun clearSelection() {
        selectedSongs = emptySet()
        isSelectionMode = false
    }
    
    fun enterSelectionMode() {
        isSelectionMode = true
    }
    
    fun downloadSelectedSongs() {
        if (selectedSongs.isNotEmpty()) {
            showDownloadDialog = true
        }
    }
    
    fun downloadSelectedSongsAsZip() {
        if (selectedSongs.isEmpty()) return
        
        viewModelScope.launch {
            try {
                appStatus = Status.DOWNLOADING
                val downloadPath = sharedPref.getDownloadPath()
                val tempDir = File("$downloadPath/temp_zip_${System.currentTimeMillis()}")
                tempDir.mkdirs()
                
                val downloadedFiles = mutableListOf<File>()
                var successfulDownloads = 0
                var failedDownloads = 0
                
                // Download all selected songs to temp directory
                selectedSongs.sorted().forEachIndexed { index, songIndex ->
                    if (songIndex in 0 until spotifyList.length()) {
                        val track = spotifyList.getJSONObject(songIndex)
                        val trackName = track.getString("title")
                        val artist = track.getString("artist")
                        
                        try {
                            fileName = "Downloading ${index + 1}/${selectedSongs.size}: $trackName"
                            totalProgress = (index.toFloat() / selectedSongs.size) * 0.9f // 90% for downloads
                            
                            val fileMeta = withContext(Dispatchers.IO) {
                                DownloadManager.getFileMeta(trackName, artist)
                            }
                            
                            val sanitizedName = sanitizeFilename(trackName)
                            val tempPath = "${tempDir.absolutePath}/$sanitizedName"
                            
                            withContext(Dispatchers.IO) {
                                DownloadManager.downloadFile(fileMeta.url, "$tempPath.${fileMeta.extention}") { b, c ->
                                    fileProgress = (b * 100 / c).toFloat() / 100
                                }
                                
                                if (convertToMp3) {
                                    DownloadManager.convertToMp3(tempPath, fileMeta.extention, trackName, artist)
                                    downloadedFiles.add(File("$tempPath.mp3"))
                                } else {
                                    DownloadManager.tagFile(tempPath, fileMeta.extention, trackName, artist)
                                    downloadedFiles.add(File("$tempPath.${fileMeta.extention}"))
                                }
                            }
                            
                            successfulDownloads++
                        } catch (e: Exception) {
                            failedDownloads++
                            e.printStackTrace()
                        }
                    }
                }
                
                // Create ZIP file
                fileName = "Creating ZIP archive..."
                totalProgress = 0.95f
                
                val zipFileName = "Spotify_Download_${System.currentTimeMillis()}.zip"
                val zipPath = "$downloadPath/$zipFileName"
                
                withContext(Dispatchers.IO) {
                    DownloadManager.createZipFile(downloadedFiles, zipPath)
                }
                
                // Clean up temp files
                tempDir.deleteRecursively()
                
                // Add to history
                val historyItem = DownloadHistoryItem(
                    spotifyUrl = spotifyLink,
                    title = "ZIP Archive: ${selectedSongs.size} songs",
                    artist = "",
                    totalTracks = selectedSongs.size,
                    successfulTracks = successfulDownloads,
                    failedTracks = failedDownloads,
                    filePath = zipPath,
                    convertedToMp3 = convertToMp3,
                    status = if (failedDownloads == 0) DownloadStatus.COMPLETED else DownloadStatus.PARTIAL
                )
                historyManager.addHistory(historyItem)
                loadHistory()
                
                totalProgress = 1f
                appStatus = Status.SCRAPED
                
                clearSelection()
                showDownloadDialog = false
                
                Toast.makeText(getApplication(), "ZIP created: $zipFileName", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                e.printStackTrace()
                appStatus = Status.SCRAPED
                Toast.makeText(getApplication(), "ZIP creation failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    fun downloadSongAtIndex(index: Int) {
        if (index in 0 until spotifyList.length()) {
            val track = spotifyList.getJSONObject(index)
            val trackName = track.getString("title")
            val artist = track.getString("artist")
            
            viewModelScope.launch {
                try {
                    val downloadPath = sharedPref.getDownloadPath()
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
                    
                    // Add to history
                    val historyItem = DownloadHistoryItem(
                        spotifyUrl = spotifyLink,
                        title = trackName,
                        artist = artist,
                        totalTracks = 1,
                        successfulTracks = 1,
                        failedTracks = 0,
                        filePath = path,
                        convertedToMp3 = convertToMp3,
                        status = DownloadStatus.COMPLETED
                    )
                    historyManager.addHistory(historyItem)
                    loadHistory()
                    
                    Toast.makeText(getApplication(), "Downloaded: $trackName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun playSongAtIndex(index: Int) {
        if (index !in 0 until spotifyList.length()) return
        if (currentPlayingIndex == index && mediaPlayer != null) {
            togglePlayPause()
            return
        }

        currentPlayingIndex = index
        showPlayer = true
        isPlaying = false
        isPlayerLoading = true
        currentTime = 0f
        duration = 0f

        playbackJob?.cancel()
        viewModelScope.launch {
            try {
                val track = spotifyList.getJSONObject(index)
                val trackName = track.getString("title")
                val artist = track.getString("artist")

                val fileMeta = withContext(Dispatchers.IO) {
                    DownloadManager.getFileMeta(trackName, artist)
                }

                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(fileMeta.url)
                        setVolume(volume, volume)
                        setOnPreparedListener { mp ->
                            this@HomeScreenViewModel.duration = mp.duration / 1000f
                            this@HomeScreenViewModel.currentTime = 0f
                            this@HomeScreenViewModel.isPlayerLoading = false
                            this@HomeScreenViewModel.isPlaying = true
                            mp.start()
                            this@HomeScreenViewModel.startPlaybackProgressUpdater()
                        }
                        setOnCompletionListener {
                            when (this@HomeScreenViewModel.repeatMode) {
                                RepeatMode.ONE -> {
                                    it.seekTo(0)
                                    it.start()
                                }
                                RepeatMode.ALL -> this@HomeScreenViewModel.nextSong()
                                else -> {
                                    this@HomeScreenViewModel.isPlaying = false
                                }
                            }
                        }
                        prepareAsync()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isPlayerLoading = false
                isPlaying = false
                Toast.makeText(getApplication(), "Player error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startPlaybackProgressUpdater() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (mediaPlayer != null && isPlaying) {
                mediaPlayer?.let { mp ->
                    currentTime = mp.currentPosition / 1000f
                    duration = if (mp.duration > 0) mp.duration / 1000f else duration
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }
    
    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.start()
                isPlaying = true
                startPlaybackProgressUpdater()
            }
        }
    }
    
    fun nextSong() {
        val nextIndex = when {
            isShuffleMode -> (0 until spotifyList.length()).random()
            currentPlayingIndex < spotifyList.length() - 1 -> currentPlayingIndex + 1
            repeatMode == RepeatMode.ALL -> 0
            else -> return
        }
        playSongAtIndex(nextIndex)
    }
    
    fun previousSong() {
        val previousIndex = when {
            currentPlayingIndex > 0 -> currentPlayingIndex - 1
            repeatMode == RepeatMode.ALL -> spotifyList.length() - 1
            else -> return
        }
        playSongAtIndex(previousIndex)
    }
    
    fun toggleShuffle() {
        isShuffleMode = !isShuffleMode
    }
    
    fun closePlayer() {
        showPlayer = false
        isPlaying = false
        currentPlayingIndex = -1
        playbackJob?.cancel()
        mediaPlayer?.pause()
    }
    
    fun getCurrentSong(): Track? {
        if (currentPlayingIndex in 0 until spotifyList.length()) {
            val track = spotifyList.getJSONObject(currentPlayingIndex)
            return Track(track.getString("title"), track.getString("artist"))
        }
        return null
    }
    
    // Media Player Controls
    fun seekTo(positionSeconds: Float) {
        currentTime = positionSeconds.coerceIn(0f, duration)
        mediaPlayer?.seekTo((currentTime * 1000).toInt())
    }
    
    fun setVolume(newVolume: Float) {
        _volume = newVolume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(_volume, _volume)
    }
    
    fun toggleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }
    
    fun toggleFavorite() {
        isFavorite = !isFavorite
    }
    
    fun togglePlayerCollapse() {
        isPlayerCollapsed = !isPlayerCollapsed
    }
    
    fun resetPlayerPosition() {
        playerOffsetX = 0f
        playerOffsetY = 0f
    }
    
    fun updatePlayerPosition(x: Float, y: Float) {
        playerOffsetX = x
        playerOffsetY = y
    }
    
    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
