package dev.sumanth.spd.service

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import dev.sumanth.spd.R
import org.json.JSONArray

class WidgetSongListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetSongListFactory(applicationContext)
    }
}

class WidgetSongListFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    companion object {
        const val PREFS_NAME = "widget_song_list_prefs"
        const val KEY_SONGS_JSON = "songs_json"
        const val KEY_CURRENT_INDEX = "current_song_index"

        fun saveSongsToPrefs(context: Context, songsJson: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SONGS_JSON, songsJson)
                .apply()
        }

        fun saveCurrentIndex(context: Context, index: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_CURRENT_INDEX, index)
                .apply()
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

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    override fun onDestroy() {
        songs.clear()
    }

    private fun loadData() {
        songs.clear()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentIndex = prefs.getInt(KEY_CURRENT_INDEX, -1)
        val jsonStr = prefs.getString(KEY_SONGS_JSON, null) ?: return
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                songs.add(
                    SongEntry(
                        title = obj.optString("title", "Unknown"),
                        artist = obj.optString("artist", ""),
                        filePath = obj.optString("filePath", ""),
                        duration = obj.optLong("duration", 0L),
                        index = i
                    )
                )
            }
        } catch (_: Exception) {}
    }

    override fun getCount(): Int = songs.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= songs.size) return RemoteViews(context.packageName, R.layout.widget_song_item)
        val song = songs[position]
        val isPlaying = (position == currentIndex)

        return RemoteViews(context.packageName, R.layout.widget_song_item).apply {
            setTextViewText(R.id.song_item_title, song.title)
            setTextViewText(R.id.song_item_artist, song.artist.ifBlank { "Unknown Artist" })

            val durationSec = (song.duration / 1000).toInt()
            val durationStr = "%d:%02d".format(durationSec / 60, durationSec % 60)
            setTextViewText(R.id.song_item_duration, durationStr)

            if (isPlaying) {
                setImageViewResource(R.id.song_item_icon, R.drawable.ic_playing_indicator_widget)
                setInt(R.id.song_item_root, "setBackgroundResource", R.drawable.widget_song_item_playing_bg)
                setTextColor(R.id.song_item_title, 0xFF1DB954.toInt())
            } else {
                setImageViewResource(R.id.song_item_icon, R.drawable.ic_music_note_widget)
                setInt(R.id.song_item_root, "setBackgroundResource", R.drawable.widget_song_item_bg)
                setTextColor(R.id.song_item_title, 0xFFFFFFFF.toInt())
            }

            val fillIntent = Intent().apply {
                putExtra(MusicPlayerService.EXTRA_SONG_INDEX, position)
            }
            setOnClickFillInIntent(R.id.song_item_root, fillIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
