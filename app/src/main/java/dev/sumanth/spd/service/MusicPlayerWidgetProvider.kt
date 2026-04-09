package dev.sumanth.spd.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import dev.sumanth.spd.MainActivity
import dev.sumanth.spd.R

class MusicPlayerWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val PREFS_NAME = "player_widget_prefs"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST = "artist"
        private const val KEY_CURRENT_TIME = "current_time"
        private const val KEY_DURATION = "duration"
        private const val KEY_IS_PLAYING = "is_playing"
        private const val KEY_IS_LOADING = "is_loading"
        private const val KEY_IS_SHUFFLE = "is_shuffle"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_IS_FAVORITE = "is_favorite"
        private const val KEY_SONG_COUNT = "song_count"
        private const val KEY_SPEED = "speed"
        private const val KEY_VOLUME = "volume"
        private const val KEY_SONG_POSITION = "song_position"
        private const val KEY_SONG_TOTAL = "song_total"

        const val ACTION_REFRESH_LIBRARY = "dev.sumanth.spd.ACTION_REFRESH_LIBRARY"
        const val ACTION_OPEN_LIBRARY = "dev.sumanth.spd.ACTION_OPEN_LIBRARY"
        const val EXTRA_REFRESH_LIBRARY = "extra_refresh_library"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MusicPlayerWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(thisWidget)
            if (ids.isEmpty()) return
            ids.forEach { appWidgetId -> updateAppWidget(context, manager, appWidgetId) }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val title = prefs.getString(KEY_TITLE, "No song selected") ?: "No song selected"
            val artist = prefs.getString(KEY_ARTIST, "") ?: ""
            val currentTime = prefs.getFloat(KEY_CURRENT_TIME, 0f)
            val duration = prefs.getFloat(KEY_DURATION, 0f)
            val isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false)
            val isLoading = prefs.getBoolean(KEY_IS_LOADING, false)
            val isShuffle = prefs.getBoolean(KEY_IS_SHUFFLE, false)
            val repeatMode = prefs.getInt(KEY_REPEAT_MODE, 0)
            val isFavorite = prefs.getBoolean(KEY_IS_FAVORITE, false)
            val songCount = prefs.getInt(KEY_SONG_COUNT, 0)
            val speed = prefs.getFloat(KEY_SPEED, 1f)
            val volume = prefs.getFloat(KEY_VOLUME, 1f)
            val songPos = prefs.getInt(KEY_SONG_POSITION, -1)
            val songTotal = prefs.getInt(KEY_SONG_TOTAL, 0)

            val progress = if (duration > 0f)
                ((currentTime.coerceIn(0f, duration) / duration) * 100).toInt()
            else 0

            val volumePercent = (volume.coerceIn(0f, 1f) * 100).toInt()

            val speedLabel = when {
                speed <= 0.76f -> "0.75×"
                speed <= 1.01f -> "1×"
                speed <= 1.26f -> "1.25×"
                speed <= 1.51f -> "1.5×"
                else -> "2×"
            }

            val queueLabel = if (songPos >= 0 && songTotal > 0) "${songPos + 1} / $songTotal" else "— / —"

            val adapterIntent = Intent(context, WidgetSongListService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val itemClickFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val itemClickTemplate = PendingIntent.getService(
                context,
                appWidgetId * 1000,
                Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY_SONG_INDEX
                },
                itemClickFlags
            )

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_music_player).apply {
                setTextViewText(R.id.widget_title, if (isLoading) "Loading..." else title)
                setTextViewText(R.id.widget_artist, artist.ifBlank { "Library Player" })
                setTextViewText(
                    R.id.widget_current_time,
                    "%d:%02d".format(currentTime.toInt() / 60, currentTime.toInt() % 60)
                )
                setTextViewText(
                    R.id.widget_total_duration,
                    "%d:%02d".format(duration.toInt() / 60, duration.toInt() % 60)
                )
                setProgressBar(R.id.widget_progress, 100, progress, false)
                setProgressBar(R.id.widget_volume_bar, 100, volumePercent, false)
                setTextViewText(R.id.widget_speed_label, speedLabel)
                setTextViewText(R.id.widget_queue_pos, queueLabel)
                setTextViewText(
                    R.id.widget_status,
                    if (isLoading) "Scanning library..." else "Song Queue"
                )
                setTextViewText(
                    R.id.widget_song_count,
                    if (songCount > 0) "$songCount songs" else ""
                )

                setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) R.drawable.ic_pause_widget else R.drawable.ic_play_widget
                )
                setImageViewResource(R.id.widget_shuffle, R.drawable.ic_shuffle_widget)
                setInt(R.id.widget_shuffle, "setColorFilter",
                    if (isShuffle) 0xFF1DB954.toInt() else 0xFFAAAAAA.toInt())

                setImageViewResource(R.id.widget_seek_backward, R.drawable.ic_fast_rewind_widget)
                setImageViewResource(R.id.widget_prev, R.drawable.ic_skip_prev_widget)
                setImageViewResource(R.id.widget_next, R.drawable.ic_skip_next_widget)
                setImageViewResource(R.id.widget_seek_forward, R.drawable.ic_fast_forward_widget)

                setImageViewResource(R.id.widget_repeat,
                    if (repeatMode == 1) R.drawable.ic_repeat_one_widget else R.drawable.ic_repeat_widget)
                setInt(R.id.widget_repeat, "setColorFilter",
                    if (repeatMode > 0) 0xFF1DB954.toInt() else 0xFFAAAAAA.toInt())

                setImageViewResource(R.id.widget_favorite,
                    if (isFavorite) R.drawable.ic_favorite_widget else R.drawable.ic_favorite_border_widget)

                setImageViewResource(R.id.widget_album_art, R.drawable.spd_icon)

                setRemoteAdapter(R.id.widget_song_list, adapterIntent)
                setPendingIntentTemplate(R.id.widget_song_list, itemClickTemplate)

                setOnClickPendingIntent(R.id.widget_shuffle, buildControlIntent(context, MusicPlayerService.ACTION_SHUFFLE))
                setOnClickPendingIntent(R.id.widget_seek_backward, buildControlIntent(context, MusicPlayerService.ACTION_SEEK_BACKWARD))
                setOnClickPendingIntent(R.id.widget_prev, buildControlIntent(context, MusicPlayerService.ACTION_PREV))
                setOnClickPendingIntent(R.id.widget_play_pause, buildControlIntent(context, MusicPlayerService.ACTION_PLAY_PAUSE))
                setOnClickPendingIntent(R.id.widget_next, buildControlIntent(context, MusicPlayerService.ACTION_NEXT))
                setOnClickPendingIntent(R.id.widget_seek_forward, buildControlIntent(context, MusicPlayerService.ACTION_SEEK_FORWARD))
                setOnClickPendingIntent(R.id.widget_repeat, buildControlIntent(context, MusicPlayerService.ACTION_REPEAT))
                setOnClickPendingIntent(R.id.widget_favorite, buildControlIntent(context, MusicPlayerService.ACTION_TOGGLE_FAVORITE))
                setOnClickPendingIntent(R.id.widget_speed, buildControlIntent(context, MusicPlayerService.ACTION_SPEED_CHANGE))
                setOnClickPendingIntent(R.id.widget_volume_up, buildControlIntent(context, MusicPlayerService.ACTION_VOLUME_UP))
                setOnClickPendingIntent(R.id.widget_volume_down, buildControlIntent(context, MusicPlayerService.ACTION_VOLUME_DOWN))
                setOnClickPendingIntent(R.id.widget_refresh, buildRefreshIntent(context))
                setOnClickPendingIntent(R.id.widget_root, buildOpenLibraryIntent(context, false))
            }

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_song_list)
        }

        private fun buildRefreshIntent(context: Context): PendingIntent {
            val intent = Intent(context, MusicPlayerWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_LIBRARY
            }
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildOpenLibraryIntent(context: Context, refresh: Boolean): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_LIBRARY
                putExtra(EXTRA_REFRESH_LIBRARY, refresh)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildControlIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicPlayerService::class.java).apply { this.action = action }
            return PendingIntent.getService(
                context, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { id -> updateAppWidget(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH_LIBRARY -> buildOpenLibraryIntent(context, true).send()
            ACTION_OPEN_LIBRARY -> buildOpenLibraryIntent(context, false).send()
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> updateAllWidgets(context)
        }
    }
}
