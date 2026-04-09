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

class LibraryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val PREFS_NAME = "library_widget_prefs"
        private const val KEY_SONG_COUNT = "song_count"
        private const val KEY_TOTAL_DURATION = "total_duration"
        private const val KEY_TOTAL_SIZE = "total_size"
        private const val KEY_SCAN_PATH = "scan_path"
        private const val KEY_IS_SCANNING = "is_scanning"
        const val ACTION_REFRESH_LIBRARY = "dev.sumanth.spd.ACTION_REFRESH_LIBRARY"
        const val ACTION_OPEN_LIBRARY = "dev.sumanth.spd.ACTION_OPEN_LIBRARY"
        const val EXTRA_REFRESH_LIBRARY = "extra_refresh_library"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, LibraryWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(thisWidget)
            if (ids.isEmpty()) return
            ids.forEach { appWidgetId ->
                updateAppWidget(context, manager, appWidgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val songCount = prefs.getInt(KEY_SONG_COUNT, 0)
            val durationSeconds = prefs.getLong(KEY_TOTAL_DURATION, 0L)
            val scanPath = prefs.getString(KEY_SCAN_PATH, "Library") ?: "Library"
            val isScanning = prefs.getBoolean(KEY_IS_SCANNING, false)

            val durationText = buildDurationText(durationSeconds)
            val countText = when {
                songCount == 0 -> "No songs found"
                songCount == 1 -> "1 song"
                else -> "$songCount songs"
            }
            val statusText = if (isScanning) "Scanning library..." else "Library synced"

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_music_player).apply {
                setTextViewText(R.id.widget_library_title, "My Library")
                setTextViewText(R.id.widget_song_count, countText)
                setTextViewText(R.id.widget_duration, durationText)
                setTextViewText(R.id.widget_scan_path, scanPath)
                setTextViewText(R.id.widget_status, statusText)
                setImageViewResource(R.id.widget_refresh, android.R.drawable.ic_popup_sync)
                setOnClickPendingIntent(R.id.widget_refresh, buildRefreshIntent(context))
                setOnClickPendingIntent(R.id.widget_root, buildOpenLibraryIntent(context, false))
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        private fun buildDurationText(totalSeconds: Long): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return when {
                hours > 0 -> String.format("%dh %02dm", hours, minutes)
                minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
                else -> String.format("%02ds", seconds)
            }
        }

        private fun buildRefreshIntent(context: Context): PendingIntent {
            val intent = Intent(context, LibraryWidgetProvider::class.java).apply {
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
