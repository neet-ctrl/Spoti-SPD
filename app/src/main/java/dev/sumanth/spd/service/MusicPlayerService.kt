package dev.sumanth.spd.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_SONG_POSITION = "extra_song_position"
        const val EXTRA_SONG_TOTAL = "extra_song_total"

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

    private var mediaSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "SPDMusicPlayer").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> broadcastAndBring(ACTION_PLAY_PAUSE)
            ACTION_NEXT -> broadcastAndBring(ACTION_NEXT)
            ACTION_PREV -> broadcastAndBring(ACTION_PREV)
            ACTION_SHUFFLE -> broadcastAndBring(ACTION_SHUFFLE)
            ACTION_REPEAT -> broadcastAndBring(ACTION_REPEAT)
            ACTION_TOGGLE_FAVORITE -> broadcastAndBring(ACTION_TOGGLE_FAVORITE)
            ACTION_SEEK_BACKWARD -> broadcastAndBring(ACTION_SEEK_BACKWARD)
            ACTION_SEEK_FORWARD -> broadcastAndBring(ACTION_SEEK_FORWARD)
            ACTION_SPEED_CHANGE -> broadcastAndBring(ACTION_SPEED_CHANGE)
            ACTION_VOLUME_UP -> broadcastAndBring(ACTION_VOLUME_UP)
            ACTION_VOLUME_DOWN -> broadcastAndBring(ACTION_VOLUME_DOWN)
            ACTION_EQUALIZER -> broadcastAndBring(ACTION_EQUALIZER)
            ACTION_PLAY_SONG_INDEX -> {
                val songIndex = intent.getIntExtra(EXTRA_SONG_INDEX, -1)
                if (songIndex >= 0) {
                    bringAppToForeground()
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                        Intent(ACTION_PLAY_SONG_INDEX).apply {
                            setPackage(packageName)
                            putExtra(EXTRA_SONG_INDEX, songIndex)
                        }
                    )
                }
            }
            ACTION_CLOSE -> {
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(ACTION_CLOSE).apply { setPackage(packageName) }
                )
                mediaSession?.isActive = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_UPDATE -> {
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
        }
        return START_STICKY
    }

    private fun broadcastAndBring(action: String) {
        bringAppToForeground()
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(action).apply { setPackage(packageName) }
        )
    }

    private fun updateMediaSession(
        title: String,
        artist: String,
        isPlaying: Boolean,
        currentTime: Float,
        duration: Float
    ) {
        val session = mediaSession ?: return
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (duration * 1000).toLong())
            .build()
        session.setMetadata(metadata)

        val state = PlaybackStateCompat.Builder()
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                (currentTime * 1000).toLong(),
                1f
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO
            )
            .build()
        session.setPlaybackState(state)
    }

    private fun bringAppToForeground() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
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

        val prevIntent = buildServiceIntent(ACTION_PREV, 1)
        val playPauseIntent = buildServiceIntent(ACTION_PLAY_PAUSE, 2)
        val nextIntent = buildServiceIntent(ACTION_NEXT, 3)
        val closeIntent = buildServiceIntent(ACTION_CLOSE, 4)
        val shuffleIntent = buildServiceIntent(ACTION_SHUFFLE, 5)
        val repeatIntent = buildServiceIntent(ACTION_REPEAT, 6)
        val favoriteIntent = buildServiceIntent(ACTION_TOGGLE_FAVORITE, 7)
        val seekBackwardIntent = buildServiceIntent(ACTION_SEEK_BACKWARD, 8)
        val seekForwardIntent = buildServiceIntent(ACTION_SEEK_FORWARD, 9)
        val speedIntent = buildServiceIntent(ACTION_SPEED_CHANGE, 10)
        val volUpIntent = buildServiceIntent(ACTION_VOLUME_UP, 11)
        val volDownIntent = buildServiceIntent(ACTION_VOLUME_DOWN, 12)
        val eqIntent = buildServiceIntent(ACTION_EQUALIZER, 13)

        val progressPercent = if (duration > 0f) {
            ((currentTime.coerceIn(0f, duration) / duration) * 100).toInt()
        } else 0

        val volumePercent = (volume.coerceIn(0f, 1f) * 100).toInt()

        val speedLabel = when {
            speed <= 0.76f -> "0.75×"
            speed <= 1.01f -> "1×"
            speed <= 1.26f -> "1.25×"
            speed <= 1.51f -> "1.5×"
            else -> "2×"
        }

        val queueLabel = if (songPos >= 0 && songTotal > 0) "${songPos + 1} / $songTotal" else ""

        val notificationView = RemoteViews(packageName, R.layout.notification_music_player).apply {
            setTextViewText(R.id.notification_title, if (isLoading) "Loading..." else title)
            setTextViewText(R.id.notification_artist, artist.ifBlank { "Unknown Artist" })
            setTextViewText(R.id.notification_subheader, "SPD Library")
            setTextViewText(R.id.notification_queue_pos, queueLabel)
            setTextViewText(R.id.notification_speed_label, speedLabel)
            setTextViewText(
                R.id.notification_current_time,
                "%d:%02d".format((currentTime.toInt() / 60), currentTime.toInt() % 60)
            )
            setTextViewText(
                R.id.notification_total_duration,
                "%d:%02d".format((duration.toInt() / 60), duration.toInt() % 60)
            )
            setTextViewText(
                R.id.notification_footer,
                when {
                    isLoading -> "Loading track..."
                    isPlaying -> "▶ Now Playing • Library"
                    else -> "⏸ Paused • Library"
                }
            )
            setProgressBar(R.id.notification_progress, 100, progressPercent, false)
            setProgressBar(R.id.notification_volume_bar, 100, volumePercent, false)

            setImageViewResource(
                R.id.action_play_pause,
                if (isPlaying) R.drawable.ic_pause_widget else R.drawable.ic_play_widget
            )
            setImageViewResource(R.id.action_prev, R.drawable.ic_skip_prev_widget)
            setImageViewResource(R.id.action_next, R.drawable.ic_skip_next_widget)
            setImageViewResource(R.id.action_seek_backward, R.drawable.ic_fast_rewind_widget)
            setImageViewResource(R.id.action_seek_forward, R.drawable.ic_fast_forward_widget)

            setImageViewResource(R.id.action_shuffle, R.drawable.ic_shuffle_widget)
            setInt(R.id.action_shuffle, "setColorFilter",
                if (isShuffle) 0xFF1DB954.toInt() else 0xFFAAAAAA.toInt())

            setImageViewResource(R.id.action_repeat,
                if (repeatMode == 1) R.drawable.ic_repeat_one_widget else R.drawable.ic_repeat_widget)
            setInt(R.id.action_repeat, "setColorFilter",
                if (repeatMode > 0) 0xFF1DB954.toInt() else 0xFFAAAAAA.toInt())

            setImageViewResource(R.id.action_favorite,
                if (isFavorite) R.drawable.ic_favorite_widget else R.drawable.ic_favorite_border_widget)

            setInt(R.id.notification_root, "setBackgroundColor", Color.parseColor("#1A1A2E"))

            setOnClickPendingIntent(R.id.action_prev, prevIntent)
            setOnClickPendingIntent(R.id.action_play_pause, playPauseIntent)
            setOnClickPendingIntent(R.id.action_next, nextIntent)
            setOnClickPendingIntent(R.id.action_shuffle, shuffleIntent)
            setOnClickPendingIntent(R.id.action_repeat, repeatIntent)
            setOnClickPendingIntent(R.id.action_favorite, favoriteIntent)
            setOnClickPendingIntent(R.id.action_seek_backward, seekBackwardIntent)
            setOnClickPendingIntent(R.id.action_seek_forward, seekForwardIntent)
            setOnClickPendingIntent(R.id.action_speed, speedIntent)
            setOnClickPendingIntent(R.id.action_volume_up, volUpIntent)
            setOnClickPendingIntent(R.id.action_volume_down, volDownIntent)
            setOnClickPendingIntent(R.id.action_equalizer, eqIntent)
            setOnClickPendingIntent(R.id.notification_close, closeIntent)
            setOnClickPendingIntent(R.id.notification_root, openAppIntent)
        }

        val sessionToken = mediaSession?.sessionToken
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(if (isPlaying) "▶ Playing" else "⏸ Paused")
            .setSmallIcon(R.drawable.ic_music_note_widget)
            .setLargeIcon(
                android.graphics.BitmapFactory.decodeResource(resources, R.drawable.spd_icon)
            )
            .setContentIntent(openAppIntent)
            .setCustomContentView(notificationView)
            .setCustomBigContentView(notificationView)
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
            .addAction(R.drawable.ic_close_widget, "Close", closeIntent)

        if (sessionToken != null) {
            builder.setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(closeIntent)
            )
        } else {
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }

        return builder.build()
    }

    private fun buildServiceIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MusicPlayerService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
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
