package dev.sumanth.spd.service

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import dev.sumanth.spd.R
import dev.sumanth.spd.utils.WidgetLogger
import org.json.JSONArray

class WidgetSongListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return try {
            WidgetLogger(applicationContext).logInfo("WidgetSongListService onGetViewFactory called")
            WidgetSongListFactory(applicationContext, intent)
        } catch (e: Exception) {
            WidgetSongListFactory.saveLastError(applicationContext, "Factory init failed: ${e.message}")
            WidgetLogger(applicationContext).logError("WidgetSongListService onGetViewFactory failed", e)
            throw e
        }
    }
}

class WidgetSongListFactory(
    private val context: Context,
    private val intent: Intent? = null
) : RemoteViewsService.RemoteViewsFactory {

    private val logger = WidgetLogger(context)

    companion object {
        const val PREFS_NAME = "widget_song_list_prefs"
        const val KEY_SONGS_JSON = "songs_json"
        const val KEY_CURRENT_INDEX = "current_song_index"
        const val KEY_LAST_ERROR = "last_error"

        private const val TYPE_SONG = 0
        private const val TYPE_PLACEHOLDER = 1

        fun saveSongsToPrefs(context: Context, songsJson: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_SONGS_JSON, songsJson).apply()
        }

        fun saveCurrentIndex(context: Context, index: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_CURRENT_INDEX, index).apply()
        }

        fun saveLastError(context: Context, error: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST_ERROR, error).apply()
        }

        fun getLastError(context: Context): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_ERROR, "") ?: ""
        }
    }

    data class SongEntry(
        val title: String,
        val artist: String,
        val filePath: String,
        val duration: Long,
        val index: Int
    )

    private val songs = mutableListOf<SongEntry>()
    private var currentIndex = -1
    private var isEmpty = true

    override fun onCreate() {
        logger.logInfo("WidgetSongListFactory onCreate called")
        loadData()
    }

    override fun onDataSetChanged() {
        logger.logInfo("WidgetSongListFactory onDataSetChanged called")
        loadData()
    }

    override fun onDestroy() {
        logger.logInfo("WidgetSongListFactory onDestroy called")
        songs.clear()
    }

    private fun loadData() {
        logger.logDebug("WidgetSongListFactory loadData called")
        songs.clear()
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            currentIndex = prefs.getInt(KEY_CURRENT_INDEX, -1)
            val jsonStr = prefs.getString(KEY_SONGS_JSON, null)
            if (jsonStr.isNullOrBlank()) {
                isEmpty = true
                logger.logInfo("No songs data found in preferences")
                return
            }
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                try {
                    val obj = arr.getJSONObject(i)
                    songs.add(
                        SongEntry(
                            title = obj.optString("title", "Unknown").take(60),
                            artist = obj.optString("artist", "").take(40),
                            filePath = obj.optString("filePath", ""),
                            duration = obj.optLong("duration", 0L),
                            index = i
                        )
                    )
                } catch (e: Exception) {
                    logger.logWarn("Failed to parse song at index $i", mapOf("error" to e.message))
                }
            }
            isEmpty = songs.isEmpty()
            logger.logInfo("Loaded songs data", mapOf(
                "songCount" to songs.size,
                "currentIndex" to currentIndex,
                "isEmpty" to isEmpty
            ))
        } catch (e: Exception) {
            isEmpty = true
            val errorMessage = "Load error: ${e.message?.take(80)}"
            saveLastError(context, errorMessage)
            logger.logError("Failed to load songs data", e, mapOf("error" to errorMessage))
        }
    }

    override fun getCount(): Int = if (isEmpty) 1 else songs.size

    override fun getViewTypeCount(): Int = 2

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewAt(position: Int): RemoteViews {
        logger.logDebug("getViewAt called", mapOf("position" to position, "isEmpty" to isEmpty, "songsSize" to songs.size))
        if (isEmpty) {
            logger.logDebug("Returning placeholder view (empty)")
            return buildPlaceholderView()
        }
        if (position < 0 || position >= songs.size) {
            logger.logWarn("Invalid position requested", mapOf("position" to position, "songsSize" to songs.size))
            return buildPlaceholderView()
        }
        return try {
            val song = songs[position]
            val isPlaying = position == currentIndex
            logger.logDebug("Building song view", mapOf(
                "position" to position,
                "title" to song.title,
                "isPlaying" to isPlaying
            ))
            buildSongView(song, isPlaying)
        } catch (e: Exception) {
            saveLastError(context, "View error: ${e.message?.take(80)}")
            logger.logError("Failed to build song view", e, mapOf("position" to position))
            buildPlaceholderView()
        }
    }

    private fun buildSongView(song: SongEntry, isPlaying: Boolean): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_song_item).apply {
            setTextViewText(R.id.song_item_title, song.title.ifBlank { "Unknown" })
            setTextViewText(R.id.song_item_artist, song.artist.ifBlank { "Unknown Artist" })

            val durationSec = (song.duration / 1000).toInt()
            val durationStr = if (durationSec > 0)
                "%d:%02d".format(durationSec / 60, durationSec % 60)
            else ""
            setTextViewText(R.id.song_item_duration, durationStr)

            if (isPlaying) {
                setImageViewResource(R.id.song_item_icon, R.drawable.ic_playing_indicator_widget)
                setInt(R.id.song_item_root, "setBackgroundResource", R.drawable.widget_song_item_playing_bg)
                setTextColor(R.id.song_item_title, 0xFF1DB954.toInt())
                setTextColor(R.id.song_item_artist, 0xFF1DB95480.toInt())
            } else {
                setImageViewResource(R.id.song_item_icon, R.drawable.ic_music_note_widget)
                setInt(R.id.song_item_root, "setBackgroundResource", R.drawable.widget_song_item_bg)
                setTextColor(R.id.song_item_title, 0xFFFFFFFF.toInt())
                setTextColor(R.id.song_item_artist, 0xFF9999BB.toInt())
            }

            val fillIntent = Intent().putExtra(MusicPlayerService.EXTRA_SONG_INDEX, song.index)
            setOnClickFillInIntent(R.id.song_item_root, fillIntent)
        }
    }

    private fun buildPlaceholderView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_song_item).apply {
            setTextViewText(R.id.song_item_title, "No songs loaded")
            setTextViewText(R.id.song_item_artist, "Tap Library tab to load songs")
            setTextViewText(R.id.song_item_duration, "")
            setImageViewResource(R.id.song_item_icon, R.drawable.ic_music_note_widget)
            setInt(R.id.song_item_root, "setBackgroundResource", R.drawable.widget_song_item_bg)
            setTextColor(R.id.song_item_title, 0xFF9999BB.toInt())
            setTextColor(R.id.song_item_artist, 0xFF666688.toInt())
        }
    }
}
