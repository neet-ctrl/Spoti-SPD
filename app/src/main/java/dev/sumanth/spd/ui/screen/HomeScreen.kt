package dev.sumanth.spd.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.component.SpotifyDialog
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.Status
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import java.util.Locale
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel()) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.appStatus == Status.SCRAPING) {
            SpotifyDialog(viewModel)
        }

        // Download Dialog
        if (viewModel.showDownloadDialog) {
            DownloadDialog(viewModel)
        }

        // Music Player
        if (viewModel.showPlayer) {
            MusicPlayerDialog(viewModel)
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ============= HEADER SECTION =============
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Spotify Downloads",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Add your Spotify link to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ============= INPUT CARD WITH PASTE =============
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Paste Your Link",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Input Field with Paste Button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = viewModel.spotifyLink,
                            onValueChange = { viewModel.spotifyLink = it },
                            label = { Text("Spotify Playlist / Album / Track") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            minLines = 3,
                            maxLines = 4,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Paste Button
                    FilledTonalButton(
                        onClick = { viewModel.pasteFromClipboard() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Filled.ContentPaste,
                            contentDescription = "Paste",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Paste from Clipboard")
                    }

                    // Convert to MP3 Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Convert to MP3",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Higher compatibility",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = viewModel.convertToMp3,
                                onCheckedChange = { viewModel.convertToMp3 = it }
                            )
                        }
                    }
                }
            }

            // ============= ACTION BUTTONS =============
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = { viewModel.startScraping() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    enabled = viewModel.appStatus != Status.SCRAPING,
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
                ) {
                    if (viewModel.appStatus == Status.SCRAPING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Scrape")
                    }
                }
                
                ElevatedButton(
                    onClick = { viewModel.downloadPlaylist() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    enabled = viewModel.appStatus == Status.SCRAPED,
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (viewModel.appStatus == Status.DOWNLOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Text("Download All")
                    }
                }
            }

            // ============= SUCCESS ANIMATION =============
            AnimatedVisibility(
                visible = viewModel.appStatus == Status.SCRAPED,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("✓", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Column {
                            Text(
                                "Ready to Download",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${viewModel.spotifyList.length()} songs found",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // ============= SONGS LIST =============
            AnimatedVisibility(
                visible = viewModel.appStatus == Status.SCRAPED && viewModel.spotifyList.length() > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header with selection controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Songs (${viewModel.spotifyList.length()})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (viewModel.isSelectionMode) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { viewModel.selectAllSongs() }) {
                                        Text("Select All")
                                    }
                                    TextButton(onClick = { viewModel.clearSelection() }) {
                                        Text("Cancel")
                                    }
                                    if (viewModel.selectedSongs.isNotEmpty()) {
                                        FilledTonalButton(onClick = { viewModel.downloadSelectedSongs() }) {
                                            Text("Download (${viewModel.selectedSongs.size})")
                                        }
                                    }
                                }
                            }
                        }

                        // Songs list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed((0 until viewModel.spotifyList.length()).toList()) { index, _ ->
                                val track = viewModel.spotifyList.getJSONObject(index)
                                val title = track.getString("title")
                                val artist = track.getString("artist")
                                val isSelected = viewModel.selectedSongs.contains(index)

                                var offsetX by remember { mutableStateOf(0f) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    when {
                                                        offsetX > 100 -> {
                                                            // Swipe right - select
                                                            viewModel.toggleSongSelection(index)
                                                        }
                                                        offsetX < -100 -> {
                                                            // Swipe left - download
                                                            viewModel.downloadSongAtIndex(index)
                                                        }
                                                    }
                                                    offsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    offsetX += dragAmount
                                                    offsetX = offsetX.coerceIn(-200f, 200f)
                                                }
                                            )
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    if (viewModel.isSelectionMode) {
                                                        viewModel.toggleSongSelection(index)
                                                    }
                                                },
                                                onLongClick = {
                                                    viewModel.toggleSongSelection(index)
                                                    if (!viewModel.isSelectionMode) {
                                                        viewModel.enterSelectionMode()
                                                    }
                                                }
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Song info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Controls
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (viewModel.isSelectionMode) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { viewModel.toggleSongSelection(index) }
                                                )
                                            } else {
                                                IconButton(
                                                    onClick = { viewModel.playSongAtIndex(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.PlayArrow,
                                                        contentDescription = "Play",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom padding
                        Box(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // ============= PROGRESS CARDS =============
            AnimatedVisibility(
                visible = viewModel.totalProgress > 0f,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (viewModel.totalProgress == 1f) "Complete!" 
                                else "Downloading...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (viewModel.appStatus == Status.DOWNLOADING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        // Current File
                        if (viewModel.totalProgress < 1f) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    viewModel.fileName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                LinearProgressIndicator(
                                    progress = viewModel.fileProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                                Text(
                                    String.format(Locale.ENGLISH, "%.0f%%", viewModel.fileProgress * 100),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Overall Progress
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Overall Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(viewModel.spotifyList.length() * viewModel.totalProgress).toInt()}/${viewModel.spotifyList.length()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = viewModel.totalProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                String.format(Locale.ENGLISH, "%.1f%%", viewModel.totalProgress * 100),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }

                        // Destination location
                        if (viewModel.currentDownload != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Open file location
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(android.net.Uri.parse("file://${viewModel.currentDownload?.filePath}"), "resource/folder")
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Folder,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    " ${viewModel.currentDownload?.filePath}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Cancel Button
                        if (viewModel.appStatus == Status.DOWNLOADING) {
                            FilledTonalButton(
                                onClick = { viewModel.cancelDownload() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Download")
                            }
                        }
                    }
                }
            }

            // ============= FAILED DOWNLOADS CARD =============
            AnimatedVisibility(
                visible = viewModel.failedTracks.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Download Issues",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "${viewModel.getFailedDownloadsCount()} songs failed to download",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { viewModel.retryFailedDownloads() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(16.dp).padding(end = 8.dp)
                            )
                            Text("Retry Failed Downloads")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadDialog(viewModel: HomeScreenViewModel) {
    val context = LocalContext.current
    val sharedPref = remember { dev.sumanth.spd.utils.SharedPref(context) }
    var downloadPath by remember { mutableStateOf(sharedPref.getDownloadPath()) }
    var isExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.showDownloadDialog = false },
        title = { Text("Download Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Location:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            downloadPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TextButton(onClick = {
                        // TODO: Implement location picker
                    }) {
                        Text("Change")
                    }
                }

                // Selected songs count
                Text(
                    "${viewModel.selectedSongs.size} songs selected",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Expandable song list
                if (isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.selectedSongs.sorted().forEach { index ->
                            if (index in 0 until viewModel.spotifyList.length()) {
                                val track = viewModel.spotifyList.getJSONObject(index)
                                val title = track.getString("title")
                                val artist = track.getString("artist")
                                Text(
                                    "${index + 1}. $title - $artist",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Collapse" else "Show Songs")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    // Download individually
                    viewModel.selectedSongs.forEach { index ->
                        viewModel.downloadSongAtIndex(index)
                    }
                    viewModel.clearSelection()
                    viewModel.showDownloadDialog = false
                }) {
                    Text("Download Each")
                }
                Button(onClick = {
                    // Download as ZIP
                    viewModel.downloadSelectedSongsAsZip()
                }) {
                    Text("Download as ZIP")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showDownloadDialog = false }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MusicPlayerDialog(viewModel: HomeScreenViewModel) {
    val currentSong = viewModel.getCurrentSong()

    Dialog(
        onDismissRequest = { viewModel.closePlayer() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Song info
                if (currentSong != null) {
                    Text(
                        currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        currentSong.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousSong() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (viewModel.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.nextSong() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Shuffle toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleShuffle() }
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (viewModel.isShuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Shuffle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (viewModel.isShuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Close button
                TextButton(onClick = { viewModel.closePlayer() }) {
                    Text("Close Player")
                }
            }
        }
    }
}
