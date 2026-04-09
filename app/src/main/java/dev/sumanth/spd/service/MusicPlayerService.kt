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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
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

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_CURRENT_TIME = "extra_current_time"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_IS_LOADING = "extra_is_loading"
        const val EXTRA_IS_SHUFFLE = "extra_is_shuffle"
        const val EXTRA_REPEAT_MODE = "extra_repeat_mode"
        const val EXTRA_IS_FAVORITE = "extra_is_favorite"

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
            isFavorite: Boolean
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
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PLAY_PAUSE).apply { setPackage(packageName) })
            }
            ACTION_NEXT -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_NEXT).apply { setPackage(packageName) })
            }
            ACTION_PREV -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PREV).apply { setPackage(packageName) })
            }
            ACTION_SHUFFLE -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_SHUFFLE).apply { setPackage(packageName) })
            }
            ACTION_REPEAT -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_REPEAT).apply { setPackage(packageName) })
            }
            ACTION_TOGGLE_FAVORITE -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_TOGGLE_FAVORITE).apply { setPackage(packageName) })
            }
            ACTION_SEEK_BACKWARD -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_SEEK_BACKWARD).apply { setPackage(packageName) })
            }
            ACTION_SEEK_FORWARD -> {
                bringAppToForeground()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_SEEK_FORWARD).apply { setPackage(packageName) })
            }
            ACTION_CLOSE -> {
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_CLOSE).apply { setPackage(packageName) })
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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
                val notification = buildNotification(
                    title,
                    artist,
                    isPlaying,
                    currentTime,
                    duration,
                    isLoading,
                    isShuffle,
                    repeatMode,
                    isFavorite
                )
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun bringAppToForeground() {
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_BRING_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(mainActivityIntent)
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
        isFavorite: Boolean
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getService(
            this, 2,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 3,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shuffleIntent = PendingIntent.getService(
            this, 5,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_SHUFFLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val repeatIntent = PendingIntent.getService(
            this, 6,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_REPEAT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val favoriteIntent = PendingIntent.getService(
            this, 7,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_TOGGLE_FAVORITE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val seekBackwardIntent = PendingIntent.getService(
            this, 8,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_SEEK_BACKWARD },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val seekForwardIntent = PendingIntent.getService(
            this, 9,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_SEEK_FORWARD },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val closeIntent = PendingIntent.getService(
            this, 4,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_CLOSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progressPercent = if (duration > 0f) {
            ((currentTime.coerceIn(0f, duration) / duration) * 100).toInt()
        } else {
            0
        }

        val notificationView = RemoteViews(packageName, R.layout.notification_music_player).apply {
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_artist, artist)
            setTextViewText(R.id.notification_current_time, "%d:%02d".format((currentTime.toInt() / 60), currentTime.toInt() % 60))
            setTextViewText(R.id.notification_total_duration, "%d:%02d".format((duration.toInt() / 60), duration.toInt() % 60))
            setTextViewText(R.id.notification_header, "Library Player")
            setTextViewText(R.id.notification_subheader, "SPD Music")
            setTextViewText(R.id.notification_footer, if (isLoading) "Loading..." else "Library mode")
            setProgressBar(R.id.notification_progress, 100, progressPercent, false)
            setImageViewResource(
                R.id.action_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            setImageViewResource(
                R.id.action_shuffle,
                if (isShuffle) android.R.drawable.ic_menu_rotate else android.R.drawable.ic_media_rew
            )
            setImageViewResource(
                R.id.action_repeat,
                if (repeatMode == 1) android.R.drawable.ic_popup_sync else android.R.drawable.ic_menu_rotate
            )
            setImageViewResource(
                R.id.action_favorite,
                if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
            setImageViewResource(
                R.id.action_seek_backward,
                android.R.drawable.ic_media_rew
            )
            setImageViewResource(
                R.id.action_seek_forward,
                android.R.drawable.ic_media_ff
            )
            setOnClickPendingIntent(R.id.action_prev, prevIntent)
            setOnClickPendingIntent(R.id.action_play_pause, playPauseIntent)
            setOnClickPendingIntent(R.id.action_next, nextIntent)
            setOnClickPendingIntent(R.id.action_shuffle, shuffleIntent)
            setOnClickPendingIntent(R.id.action_repeat, repeatIntent)
            setOnClickPendingIntent(R.id.action_favorite, favoriteIntent)
            setOnClickPendingIntent(R.id.action_seek_backward, seekBackwardIntent)
            setOnClickPendingIntent(R.id.action_seek_forward, seekForwardIntent)
            setOnClickPendingIntent(R.id.notification_close, closeIntent)
            setOnClickPendingIntent(R.id.notification_root, openAppIntent)
            setInt(R.id.notification_root, "setBackgroundColor", Color.parseColor("#121212"))
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("Library Player")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .setCustomContentView(notificationView)
            .setCustomBigContentView(notificationView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
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
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
