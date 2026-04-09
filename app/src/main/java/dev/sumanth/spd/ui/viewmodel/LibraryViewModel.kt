package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.LocalSong
import dev.sumanth.spd.utils.LibraryScanner
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _songs = MutableStateFlow<List<LocalSong>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    private val _scanProgress = MutableStateFlow(Pair(0, 0))
    private val _sortOrder = MutableStateFlow(LibrarySortOrder.DATE_ADDED)
    private val _searchQuery = MutableStateFlow("")
    private val _favorites = MutableStateFlow(setOf<String>())
    private val _scanError = MutableStateFlow<String?>(null)
    private val _scanWholeStorage = MutableStateFlow(false)
    val scanWholeStorage: StateFlow<Boolean> = _scanWholeStorage.asStateFlow()

    val scanPath: StateFlow<String> = _scanWholeStorage
        .map { scanWhole ->
            if (scanWhole) {
                Environment.getExternalStorageDirectory().absolutePath
            } else {
                sharedPref.getLibraryScanPath()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sharedPref.getLibraryScanPath())

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
            _scanProgress.value = Pair(0, 0)
            _scanError.value = null
            try {
                val path = scanPath.value
                val songs = scanner.scanDirectory(path) { processed, total ->
                    _scanProgress.value = Pair(processed, total)
                }
                _songs.value = songs
            } catch (e: Exception) {
                _scanError.value = e.message
            } finally {
                _isScanning.value = false
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
        if (!_scanWholeStorage.value) {
            refresh()
        }
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
