package dev.sumanth.spd.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WidgetLogger(private val context: Context) {

    companion object {
        private const val TAG = "WidgetLogger"
        private const val PREFS_NAME = "widget_debug_logs"
        private const val KEY_LOGS = "logs_json"
        private const val MAX_LOGS = 100
        private const val LOG_LEVEL_DEBUG = "DEBUG"
        private const val LOG_LEVEL_INFO = "INFO"
        private const val LOG_LEVEL_WARN = "WARN"
        private const val LOG_LEVEL_ERROR = "ERROR"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun logDebug(message: String, details: Map<String, Any?> = emptyMap()) {
        log(LOG_LEVEL_DEBUG, message, details)
        Log.d(TAG, "$message ${detailsToString(details)}")
    }

    fun logInfo(message: String, details: Map<String, Any?> = emptyMap()) {
        log(LOG_LEVEL_INFO, message, details)
        Log.i(TAG, "$message ${detailsToString(details)}")
    }

    fun logWarn(message: String, details: Map<String, Any?> = emptyMap()) {
        log(LOG_LEVEL_WARN, message, details)
        Log.w(TAG, "$message ${detailsToString(details)}")
    }

    fun logError(message: String, throwable: Throwable? = null, details: Map<String, Any?> = emptyMap()) {
        val errorDetails = details.toMutableMap()
        throwable?.let {
            errorDetails["exception"] = it.javaClass.simpleName
            errorDetails["message"] = it.message
            errorDetails["stackTrace"] = it.stackTraceToString()
        }
        log(LOG_LEVEL_ERROR, message, errorDetails)
        Log.e(TAG, "$message ${detailsToString(errorDetails)}", throwable)
    }

    private fun log(level: String, message: String, details: Map<String, Any?>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val logsJson = prefs.getString(KEY_LOGS, "[]") ?: "[]"
            val logsArray = JSONArray(logsJson)

            val logEntry = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("level", level)
                put("message", message)
                put("details", JSONObject(details.filterValues { it != null }))
            }

            logsArray.put(logEntry)

            // Keep only the last MAX_LOGS entries
            while (logsArray.length() > MAX_LOGS) {
                logsArray.remove(0)
            }

            prefs.edit {
                putString(KEY_LOGS, logsArray.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log message: $message", e)
        }
    }

    fun getLogs(): List<LogEntry> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val logsJson = prefs.getString(KEY_LOGS, "[]") ?: "[]"
            val logsArray = JSONArray(logsJson)

            val logs = mutableListOf<LogEntry>()
            for (i in 0 until logsArray.length()) {
                val logObj = logsArray.getJSONObject(i)
                logs.add(
                    LogEntry(
                        timestamp = logObj.getLong("timestamp"),
                        level = logObj.getString("level"),
                        message = logObj.getString("message"),
                        details = logObj.optJSONObject("details")?.let { jsonToMap(it) } ?: emptyMap()
                    )
                )
            }
            logs.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get logs", e)
            emptyList()
        }
    }

    fun getLogsAsString(): String {
        val logs = getLogs()
        if (logs.isEmpty()) return "No logs available"

        return buildString {
            appendLine("=== WIDGET DEBUG LOGS ===")
            appendLine("Total entries: ${logs.size}")
            appendLine("Generated at: ${dateFormat.format(Date())}")
            appendLine()

            logs.forEach { entry ->
                appendLine("[${dateFormat.format(Date(entry.timestamp))}] ${entry.level}: ${entry.message}")
                if (entry.details.isNotEmpty()) {
                    entry.details.forEach { (key, value) ->
                        appendLine("  $key: $value")
                    }
                }
                appendLine()
            }
            appendLine("=== END LOGS ===")
        }
    }

    fun clearLogs() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_LOGS)
        }
        logInfo("Logs cleared by user")
    }

    private fun detailsToString(details: Map<String, Any?>): String {
        return if (details.isEmpty()) "" else details.entries.joinToString(", ") { "${it.key}=${it.value}" }
    }

    private fun jsonToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }

    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val message: String,
        val details: Map<String, Any?>
    )
}