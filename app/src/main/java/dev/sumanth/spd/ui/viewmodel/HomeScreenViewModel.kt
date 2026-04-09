package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.sumanth.spd.model.DownloadHistoryItem
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.LocalPlaybackItem
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.service.MusicPlayerService
import dev.sumanth.spd.service.MusicPlayerWidgetProvider
import dev.sumanth.spd.service.WidgetSongListFactory
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

    var selectedSongs by mutableStateOf(setOf<Int>())
    var isSelectionMode by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)
    var showPlayer by mutableStateOf(false)
    var currentPlayingIndex by mutableStateOf(-1)
    var isShuffleMode by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var isPlayerLoading by mutableStateOf(false)

    var currentTime by mutableFloatStateOf(0f)
    var duration by mutableFloatStateOf(0f)
    private var _volume by mutableFloatStateOf(1f)
    val volume: Float get() = _volume
    var repeatMode by mutableStateOf(RepeatMode.NONE)
    var isPlayerCollapsed by mutableStateOf(false)
    var playerOffsetX by mutableFloatStateOf(0f)
    var playerOffsetY by mutableFloatStateOf(0f)
    var isFavorite by mutableStateOf(false)
    var playbackSpeed by mutableFloatStateOf(1f)
        private set
    private val speedCycle = floatArrayOf(1f, 1.25f, 1.5f, 2f, 0.75f)
    private var speedCycleIndex = 0

    var isLocalPlayback by mutableStateOf(false)
        private set
    private val localPlaybackList = mutableListOf<LocalPlaybackItem>()
    var currentLocalIndex by mutableStateOf(-1)
        private set

    val currentLocalFilePath: String?
        get() = if (isLocalPlayback && currentLocalIndex in localPlaybackList.indices)
            localPlaybackList[currentLocalIndex].filePath
        else null

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var playbackJob: Job? = null

    val failedTracks = mutableListOf<Track>()
    var appStatus by mutableStateOf(Status.IDLE)
    var spotifyList by mutableStateOf(JSONArray())

    // Broadcast receiver for notification button actions
    private val notificationActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicPlayerService.ACTION_PLAY_PAUSE -> togglePlayPause()
                MusicPlayerService.ACTION_NEXT -> nextSong()
                MusicPlayerService.ACTION_PREV -> previousSong()
                MusicPlayerService.ACTION_SHUFFLE -> toggleShuffle()
                MusicPlayerService.ACTION_REPEAT -> toggleRepeatMode()
                MusicPlayerService.ACTION_TOGGLE_FAVORITE -> toggleFavorite()
                MusicPlayerService.ACTION_SEEK_BACKWARD -> seekBy(-10f)
                MusicPlayerService.ACTION_SEEK_FORWARD -> seekBy(10f)
                MusicPlayerService.ACTION_CLOSE -> closePlayer()
                MusicPlayerService.ACTION_PLAY_SONG_INDEX -> {
                    val idx = intent?.getIntExtra(MusicPlayerService.EXTRA_SONG_INDEX, -1) ?: -1
                    if (idx >= 0) playLocalSongFromWidget(idx)
                }
                MusicPlayerService.ACTION_SPEED_CHANGE -> cyclePlaybackSpeed()
                MusicPlayerService.ACTION_VOLUME_UP -> adjustVolume(0.1f)
                MusicPlayerService.ACTION_VOLUME_DOWN -> adjustVolume(-0.1f)
                MusicPlayerService.ACTION_EQUALIZER -> openEqualizerIntent()
            }
        }
    }

    init {
        loadHistory()
        registerNotificationReceiver()
    }

    private fun registerNotificationReceiver() {
        val filter = IntentFilter().apply {
            addAction(MusicPlayerService.ACTION_PLAY_PAUSE)
            addAction(MusicPlayerService.ACTION_NEXT)
            addAction(MusicPlayerService.ACTION_PREV)
            addAction(MusicPlayerService.ACTION_SHUFFLE)
            addAction(MusicPlayerService.ACTION_REPEAT)
            addAction(MusicPlayerService.ACTION_TOGGLE_FAVORITE)
            addAction(MusicPlayerService.ACTION_SEEK_BACKWARD)
            addAction(MusicPlayerService.ACTION_SEEK_FORWARD)
            addAction(MusicPlayerService.ACTION_CLOSE)
            addAction(MusicPlayerService.ACTION_PLAY_SONG_INDEX)
            addAction(MusicPlayerService.ACTION_SPEED_CHANGE)
            addAction(MusicPlayerService.ACTION_VOLUME_UP)
            addAction(MusicPlayerService.ACTION_VOLUME_DOWN)
            addAction(MusicPlayerService.ACTION_EQUALIZER)
        }
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(notificationActionReceiver, filter)
    }

    private fun playLocalSongFromWidget(index: Int) {
        val prefs = getApplication<Application>().getSharedPreferences(
            WidgetSongListFactory.PREFS_NAME, Context.MODE_PRIVATE
        )
        val jsonStr = prefs.getString(WidgetSongListFactory.KEY_SONGS_JSON, null) ?: return
        try {
            val arr = org.json.JSONArray(jsonStr)
            val songs = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LocalPlaybackItem(
                    title = obj.optString("title", "Unknown"),
                    artist = obj.optString("artist", ""),
                    filePath = obj.optString("filePath", "")
                )
            }.filter { it.filePath.isNotBlank() }
            if (index in songs.indices) {
                playLocalPlaylist(songs, index)
            }
        } catch (_: Exception) {}
    }

    private fun updateMusicNotification() {
        val song = getCurrentSong() ?: return
        val songPos = if (isLocalPlayback) currentLocalIndex else -1
        val songTotal = if (isLocalPlayback) localPlaybackList.size else 0
        val intent = MusicPlayerService.buildUpdateIntent(
            getApplication(),
            song.title,
            song.artist,
            isPlaying,
            currentTime,
            duration,
            isPlayerLoading,
            isShuffleMode,
            repeatMode.ordinal,
            isFavorite,
            speed = playbackSpeed,
            volume = _volume,
            songPosition = songPos,
            songTotal = songTotal
        )
        getApplication<Application>().startService(intent)
        persistPlayerWidgetState(song.title, song.artist)
        MusicPlayerWidgetProvider.updateAllWidgets(getApplication())
    }

    private fun persistPlayerWidgetState(title: String, artist: String) {
        val prefs = getApplication<Application>().getSharedPreferences("player_widget_prefs", Context.MODE_PRIVATE)
        val songPos = if (isLocalPlayback) currentLocalIndex else -1
        val songTotal = if (isLocalPlayback) localPlaybackList.size else 0
        prefs.edit()
            .putString("title", title)
            .putString("artist", artist)
            .putFloat("current_time", currentTime)
            .putFloat("duration", duration)
            .putBoolean("is_playing", isPlaying)
            .putBoolean("is_loading", isPlayerLoading)
            .putBoolean("is_shuffle", isShuffleMode)
            .putInt("repeat_mode", repeatMode.ordinal)
            .putBoolean("is_favorite", isFavorite)
            .putFloat("speed", playbackSpeed)
            .putFloat("volume", _volume)
            .putInt("song_position", songPos)
            .putInt("song_total", songTotal)
            .apply()
        val songIndex = if (isLocalPlayback) currentLocalIndex else -1
        WidgetSongListFactory.saveCurrentIndex(getApplication(), songIndex)
    }

    private fun cyclePlaybackSpeed() {
        speedCycleIndex = (speedCycleIndex + 1) % speedCycle.size
        playbackSpeed = speedCycle[speedCycleIndex]
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(playbackSpeed)
                    ?: android.media.PlaybackParams().setSpeed(playbackSpeed)
            }
        } catch (_: Exception) {}
        updateMusicNotification()
    }

    private fun adjustVolume(delta: Float) {
        _volume = (_volume + delta).coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(_volume, _volume)
        } catch (_: Exception) {}
        updateMusicNotification()
    }

    private fun openEqualizerIntent() {
        try {
            val sessionId = mediaPlayer?.audioSessionId ?: 0
            val eqIntent = Intent(android.media.audiofx.AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                putExtra(android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE,
                    android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (eqIntent.resolveActivity(getApplication<Application>().packageManager) != null) {
                getApplication<Application>().startActivity(eqIntent)
            }
        } catch (_: Exception) {}
    }

    private fun stopMusicService() {
        try {
            getApplication<Application>().stopService(
                Intent(getApplication(), MusicPlayerService::class.java)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
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
        if (spotifyList.length() == 0) return Toast.makeText(getApplication(), "Playlist is empty.", Toast.LENGTH_SHORT).show()
        downloadJob = viewModelScope.launch {
            appStatus = Status.DOWNLOADING
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()

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

                selectedSongs.sorted().forEachIndexed { index, songIndex ->
                    if (songIndex in 0 until spotifyList.length()) {
                        val track = spotifyList.getJSONObject(songIndex)
                        val trackName = track.getString("title")
                        val artist = track.getString("artist")

                        try {
                            fileName = "Downloading ${index + 1}/${selectedSongs.size}: $trackName"
                            totalProgress = (index.toFloat() / selectedSongs.size) * 0.9f

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

                fileName = "Creating ZIP archive..."
                totalProgress = 0.95f

                val zipFileName = "Spotify_Download_${System.currentTimeMillis()}.zip"
                val zipPath = "$downloadPath/$zipFileName"

                withContext(Dispatchers.IO) {
                    DownloadManager.createZipFile(downloadedFiles, zipPath)
                }

                tempDir.deleteRecursively()

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

    fun downloadSelectedSongsIndividually() {
        if (selectedSongs.isEmpty()) return

        viewModelScope.launch {
            try {
                appStatus = Status.DOWNLOADING
                failedTracks.clear()
                val downloadPath = sharedPref.getDownloadPath()

                currentDownload = DownloadHistoryItem(
                    spotifyUrl = spotifyLink,
                    title = "Downloading selected songs",
                    artist = "Multiple songs",
                    totalTracks = selectedSongs.size,
                    filePath = downloadPath,
                    convertedToMp3 = convertToMp3,
                    status = DownloadStatus.DOWNLOADING
                )

                var successCount = 0

                selectedSongs.sorted().forEachIndexed { index, songIndex ->
                    if (songIndex in 0 until spotifyList.length()) {
                        val track = spotifyList.getJSONObject(songIndex)
                        val trackName = track.getString("title")
                        val artist = track.getString("artist")
                        try {
                            val fileMeta = withContext(Dispatchers.IO) {
                                DownloadManager.getFileMeta(trackName, artist)
                            }
                            fileName = "Downloading ${index + 1}/${selectedSongs.size}: $trackName"
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

                            totalProgress = (index + 1).toFloat() / selectedSongs.size
                            successCount++
                        } catch (e: Exception) {
                            failedTracks.add(Track(trackName, artist))
                            e.printStackTrace()
                        }
                    }
                }

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
                clearSelection()
                showDownloadDialog = false
                Toast.makeText(getApplication(), "Downloaded ${successCount} selected songs", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                appStatus = Status.SCRAPED
                Toast.makeText(getApplication(), "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
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

                    val historyItem = DownloadHistoryItem(
                        spotifyUrl = spotifyLink,
                        title = trackName,
                        artist = artist,
                        totalTracks = 1,
                        successfulTracks = 1,
                        failedTracks = 0,
                        filePath = downloadPath,
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

    fun playLocalPlaylist(songs: List<LocalPlaybackItem>, startIndex: Int) {
        if (songs.isEmpty() || startIndex !in songs.indices) return
        localPlaybackList.clear()
        localPlaybackList.addAll(songs)
        isLocalPlayback = true
        currentPlayingIndex = -1
        playLocalAtIndex(startIndex)
    }

    private fun playLocalAtIndex(index: Int) {
        if (index !in localPlaybackList.indices) return
        currentLocalIndex = index
        showPlayer = true
        isPlaying = false
        isPlayerLoading = true
        currentTime = 0f
        duration = 0f

        val item = localPlaybackList[index]
        playbackJob?.cancel()
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(item.filePath)
                        setVolume(volume, volume)
                        setOnPreparedListener { mp ->
                            this@HomeScreenViewModel.duration = mp.duration / 1000f
                            this@HomeScreenViewModel.currentTime = 0f
                            this@HomeScreenViewModel.isPlayerLoading = false
                            this@HomeScreenViewModel.isPlaying = true
                            mp.start()
                            this@HomeScreenViewModel.startPlaybackProgressUpdater()
                            this@HomeScreenViewModel.updateMusicNotification()
                        }
                        setOnCompletionListener {
                            when (this@HomeScreenViewModel.repeatMode) {
                                RepeatMode.ONE -> { it.seekTo(0); it.start() }
                                RepeatMode.ALL -> this@HomeScreenViewModel.nextSong()
                                else -> {
                                    this@HomeScreenViewModel.isPlaying = false
                                    this@HomeScreenViewModel.updateMusicNotification()
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

    fun playSongAtIndex(index: Int) {
        if (index !in 0 until spotifyList.length()) return
        if (!isLocalPlayback && currentPlayingIndex == index && mediaPlayer != null) {
            togglePlayPause()
            return
        }

        isLocalPlayback = false
        currentLocalIndex = -1
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
                            this@HomeScreenViewModel.updateMusicNotification()
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
                                    this@HomeScreenViewModel.updateMusicNotification()
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
            updateMusicNotification()
        }
    }

    fun nextSong() {
        if (isLocalPlayback) {
            val nextIdx = when {
                isShuffleMode -> localPlaybackList.indices.random()
                currentLocalIndex < localPlaybackList.size - 1 -> currentLocalIndex + 1
                repeatMode == RepeatMode.ALL -> 0
                else -> return
            }
            isPlayerLoading = true
            isPlaying = false
            currentTime = 0f
            duration = 0f
            playLocalAtIndex(nextIdx)
        } else {
            val nextIndex = when {
                isShuffleMode -> (0 until spotifyList.length()).random()
                currentPlayingIndex < spotifyList.length() - 1 -> currentPlayingIndex + 1
                repeatMode == RepeatMode.ALL -> 0
                else -> return
            }
            playSongAtIndex(nextIndex)
        }
    }

    fun previousSong() {
        if (isLocalPlayback) {
            val prevIdx = when {
                currentLocalIndex > 0 -> currentLocalIndex - 1
                repeatMode == RepeatMode.ALL -> localPlaybackList.size - 1
                else -> return
            }
            isPlayerLoading = true
            isPlaying = false
            currentTime = 0f
            duration = 0f
            playLocalAtIndex(prevIdx)
        } else {
            val previousIndex = when {
                currentPlayingIndex > 0 -> currentPlayingIndex - 1
                repeatMode == RepeatMode.ALL -> spotifyList.length() - 1
                else -> return
            }
            playSongAtIndex(previousIndex)
        }
    }

    fun toggleShuffle() {
        isShuffleMode = !isShuffleMode
        updateMusicNotification()
    }

    fun closePlayer() {
        showPlayer = false
        isPlaying = false
        currentPlayingIndex = -1
        currentLocalIndex = -1
        isLocalPlayback = false
        localPlaybackList.clear()
        playbackJob?.cancel()
        mediaPlayer?.pause()
        stopMusicService()
        persistPlayerWidgetState("No song selected", "")
        MusicPlayerWidgetProvider.updateAllWidgets(getApplication())
    }

    fun getCurrentSong(): Track? {
        return if (isLocalPlayback && currentLocalIndex in localPlaybackList.indices) {
            val item = localPlaybackList[currentLocalIndex]
            Track(item.title, item.artist)
        } else if (!isLocalPlayback && currentPlayingIndex in 0 until spotifyList.length()) {
            val track = spotifyList.getJSONObject(currentPlayingIndex)
            Track(track.getString("title"), track.getString("artist"))
        } else null
    }

    fun seekTo(positionSeconds: Float) {
        currentTime = positionSeconds.coerceIn(0f, duration)
        mediaPlayer?.seekTo((currentTime * 1000).toInt())
        updateMusicNotification()
    }

    fun seekBy(deltaSeconds: Float) {
        seekTo((currentTime + deltaSeconds).coerceIn(0f, duration))
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
        updateMusicNotification()
    }

    fun toggleFavorite() {
        isFavorite = !isFavorite
        updateMusicNotification()
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
        try {
            LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(notificationActionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        playbackJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
