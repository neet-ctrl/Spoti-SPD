package dev.sumanth.spd.service

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

class WidgetCopyLogReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COPY_LOG = "dev.sumanth.spd.ACTION_COPY_WIDGET_LOG"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_COPY_LOG) return
        try {
            val playerPrefs = context.getSharedPreferences("player_widget_prefs", Context.MODE_PRIVATE)
            val listPrefs = context.getSharedPreferences(WidgetSongListFactory.PREFS_NAME, Context.MODE_PRIVATE)
            val pendingPrefs = context.getSharedPreferences("player_pending_prefs", Context.MODE_PRIVATE)

            val title = playerPrefs.getString("title", "none") ?: "none"
            val artist = playerPrefs.getString("artist", "none") ?: "none"
            val isPlaying = playerPrefs.getBoolean("is_playing", false)
            val currentTime = playerPrefs.getFloat("current_time", 0f)
            val duration = playerPrefs.getFloat("duration", 0f)
            val speed = playerPrefs.getFloat("speed", 1f)
            val volume = playerPrefs.getFloat("volume", 1f)
            val songPos = playerPrefs.getInt("song_position", -1)
            val songTotal = playerPrefs.getInt("song_total", 0)

            val songsJson = listPrefs.getString(WidgetSongListFactory.KEY_SONGS_JSON, null)
            val songCount = if (songsJson != null) {
                try { org.json.JSONArray(songsJson).length() } catch (_: Exception) { -1 }
            } else 0
            val lastError = listPrefs.getString(WidgetSongListFactory.KEY_LAST_ERROR, "none") ?: "none"
            val pendingAction = pendingPrefs.getString("pending_action", "none") ?: "none"

            val log = buildString {
                appendLine("=== SPD Widget Debug Log ===")
                appendLine("Time: ${System.currentTimeMillis()}")
                appendLine("--- Player State ---")
                appendLine("Title: $title")
                appendLine("Artist: $artist")
                appendLine("Playing: $isPlaying")
                appendLine("Position: ${"%.1f".format(currentTime)}s / ${"%.1f".format(duration)}s")
                appendLine("Speed: ${speed}x")
                appendLine("Volume: ${(volume * 100).toInt()}%")
                appendLine("Queue: ${if (songPos >= 0) "${songPos + 1}/${songTotal}" else "none"}")
                appendLine("--- Widget Song List ---")
                appendLine("Songs loaded: $songCount")
                appendLine("Last error: $lastError")
                appendLine("--- Pending ---")
                appendLine("Pending action: $pendingAction")
                appendLine("=== End Log ===")
            }

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("SPD Widget Log", log))
            Toast.makeText(context, "Widget log copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Copy failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
