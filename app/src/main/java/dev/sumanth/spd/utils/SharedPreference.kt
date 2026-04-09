package dev.sumanth.spd.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.edit
import java.io.File

class SharedPref(context: Context) {

    private val sharedPref = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getDownloadPath(): String {
        return sharedPref.getString(KEY_DOWNLOAD_PATH, null) ?: getDefaultDownloadPath()
    }

    fun storeDownloadPath(path: String) {
        sharedPref.edit {
            putString(KEY_DOWNLOAD_PATH, path)
        }
    }

    fun getLibraryScanPath(): String {
        return sharedPref.getString(KEY_LIBRARY_SCAN_PATH, null) ?: getDefaultDownloadPath()
    }

    fun storeLibraryScanPath(path: String) {
        sharedPref.edit {
            putString(KEY_LIBRARY_SCAN_PATH, path)
        }
    }

    fun getScanWholeStorage(): Boolean {
        return sharedPref.getBoolean(KEY_SCAN_WHOLE_STORAGE, false)
    }

    fun storeScanWholeStorage(enabled: Boolean) {
        sharedPref.edit {
            putBoolean(KEY_SCAN_WHOLE_STORAGE, enabled)
        }
    }

    fun registerOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun getAutoUpdateCheck(): Boolean {
        return sharedPref.getBoolean(KEY_AUTO_UPDATE_CHECK, true)
    }

    fun storeAutoUpdateCheck(enabled: Boolean) {
        sharedPref.edit { 
            putBoolean(KEY_AUTO_UPDATE_CHECK, enabled)
        }
    }

    private fun getDefaultDownloadPath(): String {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, DEFAULT_FOLDER_NAME).absolutePath
    }

    companion object {
        private const val PREFS_NAME = "spd_settings"
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val KEY_LIBRARY_SCAN_PATH = "library_scan_path"
        const val KEY_SCAN_WHOLE_STORAGE = "scan_whole_storage"
        private const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
        private const val DEFAULT_FOLDER_NAME = "spotify-playlist-downloader"
    }
}