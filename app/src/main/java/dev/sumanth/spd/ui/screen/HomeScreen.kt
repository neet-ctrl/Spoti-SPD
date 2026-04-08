package dev.sumanth.spd.ui.screen

import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalDensity
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
import dev.sumanth.spd.ui.viewmodel.RepeatMode
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import java.util.Locale
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.max
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

        // Music Player (Floating)
        if (viewModel.showPlayer) {
            FloatingMusicPlayer(viewModel)
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
    var showFullPath by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)

                val segments = uri.path?.split(":")
                if (segments != null && segments.size > 1) {
                    val folderPath = segments[1]
                    val storageBase = if (uri.path?.contains("primary") == true) {
                        Environment.getExternalStorageDirectory().path
                    } else {
                        "/storage/${segments[0].split("/").last()}"
                    }

                    downloadPath = "$storageBase/$folderPath"
                    sharedPref.storeDownloadPath(downloadPath)
                }
            } catch (e: Exception) {
                // Handle error, perhaps show toast
                Toast.makeText(context, "Failed to set download path: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.showDownloadDialog = false },
        title = { Text("Download Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Location section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Download Location:", style = MaterialTheme.typography.labelMedium)
                        
                        // Path display
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .heightIn(max = if (showFullPath) 120.dp else 40.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (showFullPath) {
                                    Text(
                                        downloadPath,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        downloadPath,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { showFullPath = !showFullPath }) {
                                Text(if (showFullPath) "Hide Path" else "Show Full Path")
                            }
                            Button(onClick = { launcher.launch(null) }) {
                                Text("Change Location")
                            }
                        }
                    }
                }

                // Selected songs count
                Text(
                    "${viewModel.selectedSongs.size} songs selected",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Expandable song list
                if (isExpanded) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(viewModel.selectedSongs.sorted().size) { index ->
                                val songIndex = viewModel.selectedSongs.sorted()[index]
                                if (songIndex in 0 until viewModel.spotifyList.length()) {
                                    val track = viewModel.spotifyList.getJSONObject(songIndex)
                                    val title = track.getString("title")
                                    val artist = track.getString("artist")
                                    Text(
                                        "${index + 1}. $title - $artist",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Collapse Songs" else "Show All Songs (${viewModel.selectedSongs.size})")
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

    if (currentSong == null || viewModel.currentPlayingIndex < 0) {
        // Auto-close if no song to play
        viewModel.closePlayer()
        return
    }

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
                .fillMaxWidth(0.9f)
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
                // Song Counter
                Text(
                    "Now Playing ${viewModel.currentPlayingIndex + 1} of ${viewModel.spotifyList.length()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Song info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        currentSong.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress bar placeholder
                LinearProgressIndicator(
                    progress = 0.3f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(onClick = { viewModel.previousSong() }, modifier = Modifier.weight(1f)) {
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

                    IconButton(onClick = { viewModel.nextSong() }, modifier = Modifier.weight(1f)) {
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
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleShuffle() }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (viewModel.isShuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (viewModel.isShuffleMode) "Shuffle ON" else "Shuffle OFF",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (viewModel.isShuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Close button
                Button(
                    onClick = { viewModel.closePlayer() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Player")
                }
            }
        }
    }
}

@Composable
fun FloatingMusicPlayer(viewModel: HomeScreenViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val collapsedHeight = 80.dp
        val expandedHeight = 260.dp
        val playerWidth = 340.dp
        val playerHeight = if (viewModel.isPlayerCollapsed) collapsedHeight else expandedHeight
        val playerWidthPx = with(LocalDensity.current) { playerWidth.toPx() }
        val playerHeightPx = with(LocalDensity.current) { playerHeight.toPx() }
        val maxOffsetX = max(0f, maxWidthPx - playerWidthPx)
        val maxOffsetY = max(0f, maxHeightPx - playerHeightPx)
        val snapThreshold = with(LocalDensity.current) { 40.dp.toPx() }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        viewModel.playerOffsetX.roundToInt().coerceIn(0, maxOffsetX.roundToInt()),
                        viewModel.playerOffsetY.roundToInt().coerceIn(0, maxOffsetY.roundToInt())
                    )
                }
                .pointerInput(viewModel.playerOffsetX, viewModel.playerOffsetY) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val nextX = viewModel.playerOffsetX + dragAmount.x
                            val nextY = viewModel.playerOffsetY + dragAmount.y
                            viewModel.updatePlayerPosition(
                                nextX.coerceIn(0f, maxOffsetX),
                                nextY.coerceIn(0f, maxOffsetY)
                            )
                        },
                        onDragEnd = {
                            val x = viewModel.playerOffsetX
                            if (x <= snapThreshold) {
                                viewModel.updatePlayerPosition(0f, viewModel.playerOffsetY)
                                if (!viewModel.isPlayerCollapsed) viewModel.togglePlayerCollapse()
                            } else if (x >= maxWidthPx - playerWidthPx - snapThreshold) {
                                viewModel.updatePlayerPosition(maxWidthPx - playerWidthPx, viewModel.playerOffsetY)
                                if (!viewModel.isPlayerCollapsed) viewModel.togglePlayerCollapse()
                            }
                        }
                    )
                }
        ) {
            Surface(
                modifier = Modifier
                    .size(playerWidth, playerHeight)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(10f)
                    .clickable {
                        if (viewModel.isPlayerCollapsed) viewModel.togglePlayerCollapse()
                    },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                viewModel.getCurrentSong()?.title ?: "No song selected",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                viewModel.getCurrentSong()?.artist ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                                Icon(
                                    Icons.Filled.Shuffle,
                                    contentDescription = "Repeat",
                                    tint = if (viewModel.repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayerCollapse() }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Collapse",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!viewModel.isPlayerCollapsed) {
                        if (viewModel.isPlayerLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                viewModel.currentTime.toInt().let { "%02d:%02d".format(it / 60, it % 60) },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                viewModel.duration.toInt().let { "%02d:%02d".format(it / 60, it % 60) },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Slider(
                            value = viewModel.currentTime.coerceIn(0f, viewModel.duration),
                            onValueChange = { viewModel.seekTo(it) },
                            valueRange = 0f..viewModel.duration.coerceAtLeast(1f),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousSong() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                            }
                            FloatingActionButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(58.dp)) {
                                Icon(
                                    if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (viewModel.isPlaying) "Pause" else "Play"
                                )
                            }
                            IconButton(onClick = { viewModel.nextSong() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { viewModel.setVolume((viewModel.volume - 0.1f).coerceIn(0f, 1f)) }) {
                                Text("Vol -")
                            }
                            TextButton(onClick = { viewModel.setVolume((viewModel.volume + 0.1f).coerceIn(0f, 1f)) }) {
                                Text("Vol +")
                            }
                            TextButton(onClick = { viewModel.closePlayer() }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
