package dev.sumanth.spd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sumanth.spd.model.LocalPlaybackItem
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.LibraryViewModel

class SongPickerDialogActivity : ComponentActivity() {

    private val homeViewModel: HomeScreenViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val songs by libraryViewModel.filteredSortedSongs.collectAsState(initial = emptyList())
            var showDialog by remember { mutableStateOf(true) }

            if (showDialog) {
                SongPickerDialog(
                    songs = songs.map { LocalPlaybackItem(it.filePath, it.title, it.artist) },
                    onSongSelected = { index ->
                        homeViewModel.playLocalPlaylist(
                            songs.map { LocalPlaybackItem(it.filePath, it.title, it.artist) },
                            index
                        )
                        showDialog = false
                        // Add delay to ensure playback coroutine is initiated before activity finishes
                        window.decorView.postDelayed({
                            finish()
                        }, 300)
                    },
                    onDismiss = {
                        showDialog = false
                        finish()
                    }
                )
            }
        }
    }
}
