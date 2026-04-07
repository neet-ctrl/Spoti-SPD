package dev.sumanth.spd.utils

import android.content.Context
import dev.sumanth.spd.model.DownloadHistoryItem
import dev.sumanth.spd.model.DownloadStatus
import org.json.JSONArray
import org.json.JSONObject

class DownloadHistoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("download_history", Context.MODE_PRIVATE)
    private val key = "history_list"

    fun addHistory(item: DownloadHistoryItem) {
        val history = getHistory().toMutableList()
        history.add(0, item) // Add to beginning for chronological order
        // Keep only last 100 items
        if (history.size > 100) {
            history.removeAt(history.size - 1)
        }
        saveHistory(history)
    }

    fun getHistory(): List<DownloadHistoryItem> {
        return try {
            val jsonString = sharedPreferences.getString(key, "[]") ?: "[]"
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<DownloadHistoryItem>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(jsonToHistoryItem(obj))
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteHistory(id: String) {
        val history = getHistory().toMutableList()
        history.removeAll { it.id == id }
        saveHistory(history)
    }

    fun clearAllHistory() {
        sharedPreferences.edit().remove(key).apply()
    }

    private fun saveHistory(items: List<DownloadHistoryItem>) {
        try {
            val jsonArray = JSONArray()
            items.forEach { item ->
                jsonArray.put(historyItemToJson(item))
            }
            sharedPreferences.edit().putString(key, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun historyItemToJson(item: DownloadHistoryItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("spotifyUrl", item.spotifyUrl)
            put("title", item.title)
            put("artist", item.artist)
            put("downloadedAt", item.downloadedAt)
            put("totalTracks", item.totalTracks)
            put("successfulTracks", item.successfulTracks)
            put("failedTracks", item.failedTracks)
            put("filePath", item.filePath)
            put("format", item.format)
            put("convertedToMp3", item.convertedToMp3)
            put("status", item.status.name)
        }
    }

    private fun jsonToHistoryItem(obj: JSONObject): DownloadHistoryItem {
        return DownloadHistoryItem(
            id = obj.optString("id", System.currentTimeMillis().toString()),
            spotifyUrl = obj.optString("spotifyUrl", ""),
            title = obj.optString("title", "Unknown"),
            artist = obj.optString("artist", "Unknown"),
            downloadedAt = obj.optLong("downloadedAt", System.currentTimeMillis()),
            totalTracks = obj.optInt("totalTracks", 0),
            successfulTracks = obj.optInt("successfulTracks", 0),
            failedTracks = obj.optInt("failedTracks", 0),
            filePath = obj.optString("filePath", ""),
            format = obj.optString("format", "m4a"),
            convertedToMp3 = obj.optBoolean("convertedToMp3", false),
            status = try {
                DownloadStatus.valueOf(obj.optString("status", "COMPLETED"))
            } catch (e: Exception) {
                DownloadStatus.COMPLETED
            }
        )
    }
}
