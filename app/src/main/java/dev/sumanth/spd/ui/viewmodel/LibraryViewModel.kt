package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.LocalSong
import dev.sumanth.spd.service.MusicPlayerWidgetProvider
import dev.sumanth.spd.service.WidgetSongListFactory
import dev.sumanth.spd.utils.LibraryScanner
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class LibrarySortOrder {
    DATE_ADDED, TITLE_ASC, TITLE_DESC, ARTIST_ASC, DURATION_DESC, FAVORITES_FIRST
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = LibraryScanner(application)
    private val sharedPref = SharedPref(application)

    companion object {
        private const val PREFS_NAME = "library_widget_prefs"
        private const val KEY_SONG_COUNT = "song_count"
        private const val KEY_TOTAL_DURATION = "total_duration"
        private const val KEY_TOTAL_SIZE = "total_size"
        private const val KEY_SCAN_PATH = "scan_path"
        private const val KEY_IS_SCANNING = "is_scanning"
    }

    private val _songs = MutableStateFlow<List<LocalSong>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    private val _scanProgress = MutableStateFlow(Pair(0, 0))
    private val _sortOrder = MutableStateFlow(LibrarySortOrder.DATE_ADDED)
    private val _searchQuery = MutableStateFlow("")
    private val _favorites = MutableStateFlow(setOf<String>())
    private val _scanError = MutableStateFlow<String?>(null)
    private val _scanWholeStorage = MutableStateFlow(false)
    private val _libraryScanPath = MutableStateFlow(sharedPref.getLibraryScanPath())
    val scanWholeStorage: StateFlow<Boolean> = _scanWholeStorage.asStateFlow()

    val scanPath: StateFlow<String> = combine(_scanWholeStorage, _libraryScanPath) { scanWhole, path ->
        if (scanWhole) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            path
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _libraryScanPath.value)

    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    val scanProgress: StateFlow<Pair<Int, Int>> = _scanProgress.asStateFlow()
    val sortOrder: StateFlow<LibrarySortOrder> = _sortOrder.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    val filteredSortedSongs: StateFlow<List<LocalSong>> =
        combine(_songs, sortOrder, searchQuery, _favorites) { songs, order, query, favorites ->
            val withFavs = songs.map { it.copy(isFavorite = favorites.contains(it.filePath)) }
            val filtered = if (query.isBlank()) withFavs else withFavs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
            when (order) {
                LibrarySortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateModified }
                LibrarySortOrder.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
                LibrarySortOrder.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
                LibrarySortOrder.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase() }
                LibrarySortOrder.DURATION_DESC -> filtered.sortedByDescending { it.duration }
                LibrarySortOrder.FAVORITES_FIRST -> filtered.sortedByDescending { it.isFavorite }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Save filtered songs to widget whenever they change
        viewModelScope.launch {
            filteredSortedSongs.collect { songs ->
                saveFilteredSongsForWidget(songs)
            }
        }
    }

    val totalDuration: StateFlow<Long> = _songs
        .map { songs -> songs.sumOf { it.duration } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSize: StateFlow<Long> = _songs
        .map { songs -> songs.sumOf { it.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val songCount: StateFlow<Int> = _songs
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadFavorites()
        _scanWholeStorage.value = sharedPref.getScanWholeStorage()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isScanning.value = true
            persistWidgetState(true)
            _scanProgress.value = Pair(0, 0)
            _scanError.value = null
            try {
                val path = scanPath.value
                val songs = scanner.scanDirectory(path) { processed, total ->
                    _scanProgress.value = Pair(processed, total)
                }
                _songs.value = songs
                persistWidgetState(false)
                
                // Save songs to widget list preferences for widget display
                val songsJson = JSONArray().apply {
                    songs.forEach { song ->
                        put(org.json.JSONObject().apply {
                            put("title", song.title)
                            put("artist", song.artist)
                            put("filePath", song.filePath)
                            put("duration", song.duration)
                        })
                    }
                }
                WidgetSongListFactory.saveSongsToPrefs(getApplication(), songsJson.toString())
                // Reset current index since the list changed
                WidgetSongListFactory.saveCurrentIndex(getApplication(), -1)
            } catch (e: Exception) {
                _scanError.value = e.message
                persistWidgetState(false)
            } finally {
                _isScanning.value = false
                MusicPlayerWidgetProvider.updateAllWidgets(getApplication())
                
                // Clear loading state in player widget prefs
                val playerPrefs = getApplication<Application>().getSharedPreferences("player_widget_prefs", Context.MODE_PRIVATE)
                playerPrefs.edit().putBoolean("is_loading", false).apply()
            }
        }
    }

    fun setScanWholeStorage(enabled: Boolean) {
        _scanWholeStorage.value = enabled
        sharedPref.storeScanWholeStorage(enabled)
        refresh()
    }

    fun setLibraryScanPath(path: String) {
        sharedPref.storeLibraryScanPath(path)
        _libraryScanPath.value = path
        if (!_scanWholeStorage.value) {
            refresh()
        }
    }

    private fun persistWidgetState(isScanning: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_SONG_COUNT, _songs.value.size)
            .putLong(KEY_TOTAL_DURATION, _songs.value.sumOf { it.duration })
            .putLong(KEY_TOTAL_SIZE, _songs.value.sumOf { it.size })
            .putString(KEY_SCAN_PATH, scanPath.value)
            .putBoolean(KEY_IS_SCANNING, isScanning)
            .apply()

        val playerPrefs = getApplication<Application>().getSharedPreferences("player_widget_prefs", Context.MODE_PRIVATE)
        playerPrefs.edit().putInt("song_count", _songs.value.size).apply()

        if (!isScanning) {
            // Removed saveSongsForWidget() - now done automatically when filtered songs change
        }
    }

    private fun saveFilteredSongsForWidget(songs: List<LocalSong>) {
        val arr = JSONArray()
        songs.forEachIndexed { _, song ->
            arr.put(JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist)
                put("filePath", song.filePath)
                put("duration", song.duration)
            })
        }
        WidgetSongListFactory.saveSongsToPrefs(getApplication(), arr.toString())
        // Update widget to refresh the list
        MusicPlayerWidgetProvider.updateAllWidgets(getApplication())
    }

    private fun saveSongsForWidget() {
        val arr = JSONArray()
        _songs.value.forEachIndexed { _, song ->
            arr.put(JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist)
                put("filePath", song.filePath)
                put("duration", song.duration)
            })
        }
        WidgetSongListFactory.saveSongsToPrefs(getApplication(), arr.toString())
    }

    fun setSortOrder(order: LibrarySortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun toggleFavorite(song: LocalSong) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(song.filePath)) {
            current.remove(song.filePath)
        } else {
            current.add(song.filePath)
        }
        _favorites.value = current
        saveFavorites()
    }

    fun deleteSong(song: LocalSong): Boolean {
        return try {
            val deleted = File(song.filePath).delete()
            if (deleted) {
                _songs.value = _songs.value.filter { it.filePath != song.filePath }
            }
            deleted
        } catch (e: Exception) {
            false
        }
    }

    private fun loadFavorites() {
        val prefs = getApplication<Application>()
            .getSharedPreferences("library_prefs", Context.MODE_PRIVATE)
        val favSet = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        _favorites.value = favSet
    }

    private fun saveFavorites() {
        getApplication<Application>()
            .getSharedPreferences("library_prefs", Context.MODE_PRIVATE)
            .edit()
            .putStringSet("favorites", _favorites.value)
            .apply()
    }
}
