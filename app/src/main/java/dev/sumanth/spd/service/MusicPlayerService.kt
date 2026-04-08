package dev.sumanth.spd.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
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

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun buildUpdateIntent(context: Context, title: String, artist: String, isPlaying: Boolean): Intent {
            return Intent(context, MusicPlayerService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
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
                sendBroadcast(Intent(ACTION_PLAY_PAUSE))
            }
            ACTION_NEXT -> {
                sendBroadcast(Intent(ACTION_NEXT))
            }
            ACTION_PREV -> {
                sendBroadcast(Intent(ACTION_PREV))
            }
            ACTION_CLOSE -> {
                sendBroadcast(Intent(ACTION_CLOSE))
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
                val notification = buildNotification(title, artist, isPlaying)
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun buildNotification(title: String, artist: String, isPlaying: Boolean): Notification {
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

        val closeIntent = PendingIntent.getService(
            this, 4,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_CLOSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) 0 else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("SPD Player")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "Previous", prevIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPauseIntent)
            .addAction(0, "Next", nextIntent)
            .addAction(0, "Close", closeIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
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
