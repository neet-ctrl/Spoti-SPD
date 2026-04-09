package dev.sumanth.spd.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.sumanth.spd.MainActivity
import dev.sumanth.spd.R
import dev.sumanth.spd.SongPickerDialogActivity
import dev.sumanth.spd.service.MusicPlayerService

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
        
        private const val REQUEST_CODE_SONG_CLICK = 999

        const val ACTION_REFRESH_LIBRARY = "dev.sumanth.spd.ACTION_REFRESH_LIBRARY"
        const val ACTION_OPEN_LIBRARY = "dev.sumanth.spd.ACTION_OPEN_LIBRARY"
        const val ACTION_PICK_SONG = "dev.sumanth.spd.ACTION_PICK_SONG"
        const val EXTRA_REFRESH_LIBRARY = "extra_refresh_library"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MusicPlayerWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(thisWidget)
            if (ids.isEmpty()) return
            ids.forEach { appWidgetId ->
                updateAppWidget(context, manager, appWidgetId)
            }
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

            val progress = if (duration > 0f) {
                ((currentTime.coerceIn(0f, duration) / duration) * 100).toInt()
            } else {
                0
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_music_player).apply {
                setTextViewText(R.id.widget_title, title)
                setTextViewText(R.id.widget_artist, artist)
                setProgressBar(R.id.widget_progress, 100, progress, false)
                setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
                setImageViewResource(
                    R.id.widget_shuffle,
                    if (isShuffle) android.R.drawable.ic_menu_rotate else android.R.drawable.ic_media_rew
                )
                setImageViewResource(
                    R.id.widget_repeat,
                    if (repeatMode == 1) android.R.drawable.ic_popup_sync else android.R.drawable.ic_menu_rotate
                )
                setImageViewResource(
                    R.id.widget_favorite,
                    if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
                )
                setTextViewText(R.id.widget_status, if (isLoading) "Loading..." else "Library mode")
                setOnClickPendingIntent(R.id.widget_shuffle, buildControlIntent(context, MusicPlayerService.ACTION_SHUFFLE))
                setOnClickPendingIntent(R.id.widget_prev, buildControlIntent(context, MusicPlayerService.ACTION_PREV))
                setOnClickPendingIntent(R.id.widget_play_pause, buildControlIntent(context, MusicPlayerService.ACTION_PLAY_PAUSE))
                setOnClickPendingIntent(R.id.widget_next, buildControlIntent(context, MusicPlayerService.ACTION_NEXT))
                setOnClickPendingIntent(R.id.widget_repeat, buildControlIntent(context, MusicPlayerService.ACTION_REPEAT))
                setOnClickPendingIntent(R.id.widget_favorite, buildControlIntent(context, MusicPlayerService.ACTION_TOGGLE_FAVORITE))
                setOnClickPendingIntent(R.id.widget_change_song, buildPickSongIntent(context))
                setOnClickPendingIntent(R.id.widget_refresh, buildRefreshIntent(context))
                setOnClickPendingIntent(R.id.widget_progress, buildOpenLibraryIntent(context, false))
                setOnClickPendingIntent(R.id.widget_root, buildOpenLibraryIntent(context, false))
                
                // Set up remote adapter for song list
                val intent = Intent(context, WidgetSongListService::class.java)
                setRemoteAdapter(R.id.widget_song_list, intent)
                
                // Set up pending intent template for song list item clicks
                val songClickIntent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY_SONG_INDEX
                    flags = PendingIntent.FLAG_UPDATE_CURRENT
                }
                val songClickTemplate = PendingIntent.getService(
                    context,
                    REQUEST_CODE_SONG_CLICK,
                    songClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                setPendingIntentTemplate(R.id.widget_song_list, songClickTemplate)
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        private fun buildRefreshIntent(context: Context): PendingIntent {
            val intent = Intent(context, MusicPlayerWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_LIBRARY
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
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
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildPickSongIntent(context: Context): PendingIntent {
            val intent = Intent(context, SongPickerDialogActivity::class.java).apply {
                action = ACTION_PICK_SONG
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return PendingIntent.getActivity(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildControlIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicPlayerService::class.java).apply { this.action = action }
            return PendingIntent.getService(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH_LIBRARY -> {
                context?.let {
                    buildOpenLibraryIntent(it, true).send()
                }
            }
            ACTION_OPEN_LIBRARY -> {
                context?.let {
                    buildOpenLibraryIntent(it, false).send()
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                context?.let {
                    updateAllWidgets(it)
                }
            }
        }
    }
}
