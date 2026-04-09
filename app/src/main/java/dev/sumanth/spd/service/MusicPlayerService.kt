package dev.sumanth.spd.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.sumanth.spd.MainActivity
import dev.sumanth.spd.R
import org.json.JSONArray

class MusicPlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_UPDATE = "dev.sumanth.spd.ACTION_UPDATE"
        const val ACTION_PLAY_PAUSE = "dev.sumanth.spd.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "dev.sumanth.spd.ACTION_NEXT"
        const val ACTION_PREV = "dev.sumanth.spd.ACTION_PREV"
        const val ACTION_CLOSE = "dev.sumanth.spd.ACTION_CLOSE"
        const val ACTION_SHUFFLE = "dev.sumanth.spd.ACTION_SHUFFLE"
        const val ACTION_REPEAT = "dev.sumanth.spd.ACTION_REPEAT"
        const val ACTION_TOGGLE_FAVORITE = "dev.sumanth.spd.ACTION_TOGGLE_FAVORITE"
        const val ACTION_SEEK_BACKWARD = "dev.sumanth.spd.ACTION_SEEK_BACKWARD"
        const val ACTION_SEEK_FORWARD = "dev.sumanth.spd.ACTION_SEEK_FORWARD"
        const val ACTION_SEEK_TO = "dev.sumanth.spd.ACTION_SEEK_TO"
        const val ACTION_PLAY_SONG_INDEX = "dev.sumanth.spd.ACTION_PLAY_SONG_INDEX"
        const val ACTION_SPEED_CHANGE = "dev.sumanth.spd.ACTION_SPEED_CHANGE"
        const val ACTION_VOLUME_UP = "dev.sumanth.spd.ACTION_VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "dev.sumanth.spd.ACTION_VOLUME_DOWN"
        const val ACTION_EQUALIZER = "dev.sumanth.spd.ACTION_EQUALIZER"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_CURRENT_TIME = "extra_current_time"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_IS_LOADING = "extra_is_loading"
        const val EXTRA_IS_SHUFFLE = "extra_is_shuffle"
        const val EXTRA_REPEAT_MODE = "extra_repeat_mode"
        const val EXTRA_IS_FAVORITE = "extra_is_favorite"
        const val EXTRA_SONG_INDEX = "extra_song_index"
        const val EXTRA_HANDLED_BY_SERVICE = "extra_handled_by_service"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_SONG_POSITION = "extra_song_position"
        const val EXTRA_SONG_TOTAL = "extra_song_total"
        const val EXTRA_SEEK_POSITION = "extra_seek_position"

        private const val PENDING_PREFS = "player_pending_prefs"
        private const val KEY_PENDING_ACTION = "pending_action"

        fun savePendingAction(context: Context, action: String) {
            context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_PENDING_ACTION, action).apply()
        }

        fun consumePendingAction(context: Context): String? {
            val prefs = context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
            val action = prefs.getString(KEY_PENDING_ACTION, null)
            if (action != null) prefs.edit().remove(KEY_PENDING_ACTION).apply()
            return action
        }

        fun buildUpdateIntent(
            context: Context,
            title: String,
            artist: String,
            isPlaying: Boolean,
            currentTime: Float,
            duration: Float,
            isLoading: Boolean,
            isShuffle: Boolean,
            repeatMode: Int,
            isFavorite: Boolean,
            speed: Float = 1f,
            volume: Float = 1f,
            songPosition: Int = -1,
            songTotal: Int = 0
        ): Intent {
            return Intent(context, MusicPlayerService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_CURRENT_TIME, currentTime)
                putExtra(EXTRA_DURATION, duration)
                putExtra(EXTRA_IS_LOADING, isLoading)
                putExtra(EXTRA_IS_SHUFFLE, isShuffle)
                putExtra(EXTRA_REPEAT_MODE, repeatMode)
                putExtra(EXTRA_IS_FAVORITE, isFavorite)
                putExtra(EXTRA_SPEED, speed)
                putExtra(EXTRA_VOLUME, volume)
                putExtra(EXTRA_SONG_POSITION, songPosition)
                putExtra(EXTRA_SONG_TOTAL, songTotal)
            }
        }
    }

    data class LocalPlaybackSong(
        val title: String,
        val artist: String,
        val filePath: String,
        val duration: Long,
        val index: Int
    )

    private var mediaSession: MediaSessionCompat? = null
    private val localPlaybackList = mutableListOf<LocalPlaybackSong>()
    private var currentLocalIndex = -1
    private var localMediaPlayer: MediaPlayer? = null
    private var isLocalLoading = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
    }

    override fun onDestroy() {
        releaseLocalPlayer()
        localPlaybackList.clear()
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "SPDMusicPlayer").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = dispatchAction(ACTION_PLAY_PAUSE)
                override fun onPause() = dispatchAction(ACTION_PLAY_PAUSE)
                override fun onSkipToNext() = dispatchAction(ACTION_NEXT)
                override fun onSkipToPrevious() = dispatchAction(ACTION_PREV)
                override fun onFastForward() = dispatchAction(ACTION_SEEK_FORWARD)
                override fun onRewind() = dispatchAction(ACTION_SEEK_BACKWARD)
                override fun onSeekTo(pos: Long) {
                    savePendingAction(this@MusicPlayerService, ACTION_SEEK_TO)
                    LocalBroadcastManager.getInstance(this@MusicPlayerService).sendBroadcast(
                        Intent(ACTION_SEEK_TO).apply {
                            setPackage(packageName)
                            putExtra(EXTRA_SEEK_POSITION, pos / 1000f)
                        }
                    )
                }
                override fun onStop() = dispatchAction(ACTION_CLOSE)
            })
            isActive = true
        }
    }

    private fun dispatchAction(action: String) {
        savePendingAction(this, action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(action).apply { setPackage(packageName) }
        )
    }

    private fun handlePlaySongIndex(songIndex: Int) {
        if (!loadLocalPlaybackList()) return
        if (songIndex !in localPlaybackList.indices) return

        val song = localPlaybackList[songIndex]
        currentLocalIndex = songIndex
        savePendingAction(this, ACTION_PLAY_SONG_INDEX)
        getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
            .edit().putInt("pending_song_index", songIndex).apply()

        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(ACTION_PLAY_SONG_INDEX).apply {
                setPackage(packageName)
                putExtra(EXTRA_SONG_INDEX, songIndex)
                putExtra(EXTRA_HANDLED_BY_SERVICE, true)
            }
        )

        playLocalSong(song)
    }

    private fun handlePlayPause(): Boolean {
        localMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                updateServiceNotification(false)
                saveWidgetPlaybackState(getCurrentSongTitle(), getCurrentSongArtist(), false, false, getCurrentPlaybackPosition(), getCurrentDuration())
            } else {
                it.start()
                updateServiceNotification(true)
                saveWidgetPlaybackState(getCurrentSongTitle(), getCurrentSongArtist(), true, false, getCurrentPlaybackPosition(), getCurrentDuration())
            }
            return true
        }
        return false
    }

    private fun playLocalSong(song: LocalPlaybackSong) {
        releaseLocalPlayer()
        isLocalLoading = true
        updateServiceNotification(false, 0f, 0f, true, song.title, song.artist)
        saveWidgetPlaybackState(song.title, song.artist, false, true, 0f, 0f)

        try {
            localMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(song.filePath)
                setOnPrepared { player: MediaPlayer ->
                    isLocalLoading = false
                    player.start()
                    val durationSeconds = (player.duration / 1000f).coerceAtLeast(0f)
                    updateServiceNotification(true, 0f, durationSeconds, false, song.title, song.artist)
                    saveWidgetPlaybackState(song.title, song.artist, true, false, 0f, durationSeconds)
                }
                setOnCompletion { player: MediaPlayer ->
                    updateServiceNotification(false, getCurrentPlaybackPosition(), getCurrentDuration(), false, song.title, song.artist)
                    saveWidgetPlaybackState(song.title, song.artist, false, false, getCurrentPlaybackPosition(), getCurrentDuration())
                }
                setOnError { mp: MediaPlayer, what: Int, extra: Int ->
                    updateServiceNotification(false, 0f, 0f, false, song.title, song.artist)
                    saveWidgetPlaybackState(song.title, song.artist, false, false, 0f, 0f)
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            updateServiceNotification(false, 0f, 0f, false, song.title, song.artist)
            saveWidgetPlaybackState(song.title, song.artist, false, false, 0f, 0f)
        }
    }

    private fun getCurrentSongTitle(): String {
        return localPlaybackList.getOrNull(currentLocalIndex)?.title ?: "Unknown"
    }

    private fun getCurrentSongArtist(): String {
        return localPlaybackList.getOrNull(currentLocalIndex)?.artist ?: ""
    }

    private fun getCurrentPlaybackPosition(): Float {
        return localMediaPlayer?.currentPosition?.div(1000f) ?: 0f
    }

    private fun getCurrentDuration(): Float {
        return localMediaPlayer?.duration?.div(1000f) ?: 0f
    }

    private fun updateServiceNotification(
        isPlaying: Boolean,
        currentTime: Float = getCurrentPlaybackPosition(),
        duration: Float = getCurrentDuration(),
        isLoading: Boolean = false,
        title: String = getCurrentSongTitle(),
        artist: String = getCurrentSongArtist()
    ) {
        val notification = buildNotification(
            title,
            artist,
            isPlaying,
            currentTime,
            duration,
            isLoading,
            false,
            0,
            false,
            1f,
            1f,
            currentLocalIndex,
            localPlaybackList.size
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun saveWidgetPlaybackState(
        title: String,
        artist: String,
        isPlaying: Boolean,
        isLoading: Boolean,
        currentTime: Float,
        duration: Float
    ) {
        val prefs = getSharedPreferences("player_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("title", title)
            .putString("artist", artist)
            .putFloat("current_time", currentTime)
            .putFloat("duration", duration)
            .putBoolean("is_playing", isPlaying)
            .putBoolean("is_loading", isLoading)
            .apply()
        MusicPlayerWidgetProvider.updateAllWidgets(this)
    }

    private fun loadLocalPlaybackList(): Boolean {
        localPlaybackList.clear()
        val prefs = getSharedPreferences(WidgetSongListFactory.PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(WidgetSongListFactory.KEY_SONGS_JSON, null) ?: return false
        return try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val filePath = obj.optString("filePath", "")
                if (filePath.isBlank()) continue
                localPlaybackList.add(
                    LocalPlaybackSong(
                        title = obj.optString("title", "Unknown"),
                        artist = obj.optString("artist", ""),
                        filePath = filePath,
                        duration = obj.optLong("duration", 0L),
                        index = i
                    )
                )
            }
            localPlaybackList.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun releaseLocalPlayer() {
        localMediaPlayer?.reset()
        localMediaPlayer?.release()
        localMediaPlayer = null
    }

    private fun handleNext(): Boolean {
        if (localPlaybackList.isEmpty() || currentLocalIndex < 0) return false
        val nextIndex = if (currentLocalIndex < localPlaybackList.size - 1) currentLocalIndex + 1 else 0
        val nextSong = localPlaybackList[nextIndex]
        currentLocalIndex = nextIndex
        playLocalSong(nextSong)
        return true
    }

    private fun handlePrev(): Boolean {
        if (localPlaybackList.isEmpty() || currentLocalIndex < 0) return false
        val prevIndex = if (currentLocalIndex > 0) currentLocalIndex - 1 else localPlaybackList.size - 1
        val prevSong = localPlaybackList[prevIndex]
        currentLocalIndex = prevIndex
        playLocalSong(prevSong)
        return true
    }

    private fun handleSeekForward(): Boolean {
        localMediaPlayer?.let {
            val newPos = (it.currentPosition + 10000).coerceAtMost(it.duration)
            it.seekTo(newPos)
            updateServiceNotification(it.isPlaying, newPos / 1000f, it.duration / 1000f)
            saveWidgetPlaybackState(getCurrentSongTitle(), getCurrentSongArtist(), it.isPlaying, false, newPos / 1000f, it.duration / 1000f)
            return true
        }
        return false
    }

    private fun handleSeekBackward(): Boolean {
        localMediaPlayer?.let {
            val newPos = (it.currentPosition - 10000).coerceAtLeast(0)
            it.seekTo(newPos)
            updateServiceNotification(it.isPlaying, newPos / 1000f, it.duration / 1000f)
            saveWidgetPlaybackState(getCurrentSongTitle(), getCurrentSongArtist(), it.isPlaying, false, newPos / 1000f, it.duration / 1000f)
            return true
        }
        return false
    }

    private fun handleClose(): Boolean {
        releaseLocalPlayer()
        localPlaybackList.clear()
        currentLocalIndex = -1
        isLocalLoading = false
        saveWidgetPlaybackState("No song selected", "", false, false, 0f, 0f)
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                val action = intent.action ?: return START_STICKY
                if (!handlePlayPause()) {
                    dispatchAction(action)
                }
            }
            ACTION_NEXT -> {
                if (!handleNext()) {
                    dispatchAction(ACTION_NEXT)
                }
            }
            ACTION_PREV -> {
                if (!handlePrev()) {
                    dispatchAction(ACTION_PREV)
                }
            }
            ACTION_SEEK_FORWARD -> {
                if (!handleSeekForward()) {
                    dispatchAction(ACTION_SEEK_FORWARD)
                }
            }
            ACTION_SEEK_BACKWARD -> {
                if (!handleSeekBackward()) {
                    dispatchAction(ACTION_SEEK_BACKWARD)
                }
            }
            ACTION_CLOSE -> {
                if (!handleClose()) {
                    dispatchAction(ACTION_CLOSE)
                    mediaSession?.isActive = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                }
            }
            ACTION_SHUFFLE,
            ACTION_REPEAT,
            ACTION_TOGGLE_FAVORITE,
            ACTION_SPEED_CHANGE,
            ACTION_VOLUME_UP,
            ACTION_VOLUME_DOWN,
            ACTION_EQUALIZER -> {
                val action = intent.action ?: return START_STICKY
                dispatchAction(action)
            }
            ACTION_PLAY_SONG_INDEX -> {
                val songIndex = intent.getIntExtra(EXTRA_SONG_INDEX, -1)
                if (songIndex >= 0) {
                    handlePlaySongIndex(songIndex)
                }
            }
            ACTION_UPDATE -> handleUpdate(intent)
        }
        return START_STICKY
    }

    private fun handleUpdate(intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown"
        val artist = intent.getStringExtra(EXTRA_ARTIST) ?: ""
        val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
        val currentTime = intent.getFloatExtra(EXTRA_CURRENT_TIME, 0f)
        val duration = intent.getFloatExtra(EXTRA_DURATION, 0f)
        val isLoading = intent.getBooleanExtra(EXTRA_IS_LOADING, false)
        val isShuffle = intent.getBooleanExtra(EXTRA_IS_SHUFFLE, false)
        val repeatMode = intent.getIntExtra(EXTRA_REPEAT_MODE, 0)
        val isFavorite = intent.getBooleanExtra(EXTRA_IS_FAVORITE, false)
        val speed = intent.getFloatExtra(EXTRA_SPEED, 1f)
        val volume = intent.getFloatExtra(EXTRA_VOLUME, 1f)
        val songPos = intent.getIntExtra(EXTRA_SONG_POSITION, -1)
        val songTotal = intent.getIntExtra(EXTRA_SONG_TOTAL, 0)

        updateMediaSession(title, artist, isPlaying, currentTime, duration)

        val notification = buildNotification(
            title, artist, isPlaying, currentTime, duration,
            isLoading, isShuffle, repeatMode, isFavorite,
            speed, volume, songPos, songTotal
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateMediaSession(
        title: String,
        artist: String,
        isPlaying: Boolean,
        currentTime: Float,
        duration: Float
    ) {
        val session = mediaSession ?: return
        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (duration * 1000).toLong())
                .build()
        )
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    (currentTime * 1000).toLong(),
                    if (isPlaying) 1f else 0f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_STOP
                )
                .build()
        )
    }

    private fun launchApp() {
        try {
            val i = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(i)
        } catch (_: Exception) {}
    }

    private fun buildNotification(
        title: String,
        artist: String,
        isPlaying: Boolean,
        currentTime: Float,
        duration: Float,
        isLoading: Boolean,
        isShuffle: Boolean,
        repeatMode: Int,
        isFavorite: Boolean,
        speed: Float = 1f,
        volume: Float = 1f,
        songPos: Int = -1,
        songTotal: Int = 0
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = buildServicePendingIntent(ACTION_PREV, 1)
        val playPauseIntent = buildServicePendingIntent(ACTION_PLAY_PAUSE, 2)
        val nextIntent = buildServicePendingIntent(ACTION_NEXT, 3)
        val closeIntent = buildServicePendingIntent(ACTION_CLOSE, 4)
        val shuffleIntent = buildServicePendingIntent(ACTION_SHUFFLE, 5)
        val repeatIntent = buildServicePendingIntent(ACTION_REPEAT, 6)
        val favoriteIntent = buildServicePendingIntent(ACTION_TOGGLE_FAVORITE, 7)
        val seekBackIntent = buildServicePendingIntent(ACTION_SEEK_BACKWARD, 8)
        val seekFwdIntent = buildServicePendingIntent(ACTION_SEEK_FORWARD, 9)
        val speedIntent = buildServicePendingIntent(ACTION_SPEED_CHANGE, 10)
        val volUpIntent = buildServicePendingIntent(ACTION_VOLUME_UP, 11)
        val volDownIntent = buildServicePendingIntent(ACTION_VOLUME_DOWN, 12)
        val eqIntent = buildServicePendingIntent(ACTION_EQUALIZER, 13)

        val progressPct = if (duration > 0f) ((currentTime.coerceIn(0f, duration) / duration) * 100).toInt() else 0
        val volumePct = (volume.coerceIn(0f, 1f) * 100).toInt()
        val speedLabel = when {
            speed <= 0.76f -> "0.75×"
            speed <= 1.01f -> "1×"
            speed <= 1.26f -> "1.25×"
            speed <= 1.51f -> "1.5×"
            else -> "2×"
        }
        val queueLabel = if (songPos >= 0 && songTotal > 0) "${songPos + 1} / $songTotal" else ""

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.drawable.spd_icon)
        } catch (_: Exception) { null }

        val sessionToken = mediaSession?.sessionToken
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note_widget)
            .setContentTitle(if (isLoading) "Loading..." else title)
            .setContentText(artist.ifBlank { "Unknown Artist" })
            .setSubText(if (isPlaying) "▶ Playing" else "⏸ Paused")
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentIntent(openAppIntent)
            .setDeleteIntent(closeIntent)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_skip_prev_widget, "Previous", prevIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause_widget else R.drawable.ic_play_widget,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(R.drawable.ic_skip_next_widget, "Next", nextIntent)
            .addAction(R.drawable.ic_fast_forward_widget, "Forward", seekFwdIntent)

        if (sessionToken != null) {
            builder.setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(closeIntent)
            )
        }

        return builder.build()
    }

    private fun buildServicePendingIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getService(
            this, requestCode,
            Intent(this, MusicPlayerService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "SPD Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "SPD Music Player Controls"
                setShowBadge(false)
                lightColor = Color.parseColor("#1DB954")
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
