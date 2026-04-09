package dev.sumanth.spd.service

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.sumanth.spd.utils.LibraryScanner
import dev.sumanth.spd.utils.SharedPref
import org.json.JSONArray
import org.json.JSONObject

class LibraryRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val sharedPref = SharedPref(applicationContext)
            val scanWholeStorage = sharedPref.getScanWholeStorage()
            val scanPath = if (scanWholeStorage) {
                @Suppress("DEPRECATION")
                Environment.getExternalStorageDirectory().absolutePath
            } else {
                sharedPref.getLibraryScanPath()
            }

            val songs = LibraryScanner(applicationContext).scanDirectory(scanPath) { _, _ -> }
            val songsJson = JSONArray().apply {
                songs.forEach { song ->
                    put(JSONObject().apply {
                        put("title", song.title)
                        put("artist", song.artist)
                        put("filePath", song.filePath)
                        put("duration", song.duration)
                    })
                }
            }

            WidgetSongListFactory.saveSongsToPrefs(applicationContext, songsJson.toString())
            WidgetSongListFactory.saveCurrentIndex(applicationContext, -1)

            applicationContext.getSharedPreferences("player_widget_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_loading", false)
                .apply()

            MusicPlayerWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                applicationContext.getSharedPreferences("player_widget_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_loading", false)
                    .apply()
            } catch (_: Exception) {
            }
            Result.failure()
        }
    }
}
