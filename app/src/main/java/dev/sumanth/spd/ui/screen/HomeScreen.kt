package dev.sumanth.spd.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.component.SpotifyDialog
import dev.sumanth.spd.ui.theme.SpotifyGreen
import dev.sumanth.spd.ui.theme.SpotifyGreenLight
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.RepeatMode
import dev.sumanth.spd.ui.viewmodel.Status
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel()) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.appStatus == Status.SCRAPING) {
            SpotifyDialog(viewModel)
        }

        if (viewModel.showDownloadDialog) {
            DownloadDialog(viewModel)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (viewModel.showPlayer) 90.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ============= HERO GRADIENT BANNER =============
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        "SPD",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpotifyGreen
                    )
                    Text(
                        "Spotify Playlist Downloader",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ============= LINK INPUT CARD =============
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Spotify Link",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = viewModel.spotifyLink,
                            onValueChange = { viewModel.spotifyLink = it },
                            placeholder = { Text("Paste playlist, album, or track URL") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpotifyGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Paste Button
                            FilledTonalButton(
                                onClick = { viewModel.pasteFromClipboard() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Icon(Icons.Filled.ContentPaste, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste")
                            }

                            // Convert to MP3 chip
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (viewModel.convertToMp3) SpotifyGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "MP3",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (viewModel.convertToMp3) SpotifyGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Switch(
                                        checked = viewModel.convertToMp3,
                                        onCheckedChange = { viewModel.convertToMp3 = it },
                                        modifier = Modifier.height(24.dp),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = SpotifyGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // ============= ACTION BUTTONS =============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startScraping() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = viewModel.appStatus != Status.SCRAPING,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        if (viewModel.appStatus == Status.SCRAPING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SpotifyGreen
                            )
                        } else {
                            Icon(Icons.Filled.PlaylistPlay, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scrape", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = { viewModel.downloadPlaylist() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = viewModel.appStatus == Status.SCRAPED,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black,
                            disabledContainerColor = SpotifyGreen.copy(alpha = 0.3f),
                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                        )
                    ) {
                        if (viewModel.appStatus == Status.DOWNLOADING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download All", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ============= DOWNLOAD PROGRESS =============
                AnimatedVisibility(
                    visible = viewModel.appStatus == Status.DOWNLOADING,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Downloading...",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SpotifyGreen
                                )
                                Text(
                                    "${(viewModel.totalProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { viewModel.totalProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SpotifyGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )

                            Text(
                                viewModel.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (viewModel.getFailedDownloadsCount() > 0) {
                                    FilledTonalButton(
                                        onClick = { viewModel.retryFailedDownloads() },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retry ${viewModel.getFailedDownloadsCount()} failed", fontSize = 12.sp)
                                    }
                                }
                                TextButton(onClick = { viewModel.cancelDownload() }) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // ============= SCRAPED STATUS BAR =============
                AnimatedVisibility(
                    visible = viewModel.appStatus == Status.SCRAPED,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SpotifyGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        "Ready to Download",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SpotifyGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${viewModel.spotifyList.length()} songs found",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Selection mode controls
                            if (viewModel.isSelectionMode && viewModel.selectedSongs.isNotEmpty()) {
                                FilledTonalButton(
                                    onClick = { viewModel.downloadSelectedSongs() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = SpotifyGreen.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("${viewModel.selectedSongs.size} selected", color = SpotifyGreen, fontSize = 12.sp)
                                }
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // List header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Songs (${viewModel.spotifyList.length()})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (viewModel.isSelectionMode) {
                                    TextButton(onClick = { viewModel.selectAllSongs() }) {
                                        Text("All", fontSize = 12.sp, color = SpotifyGreen)
                                    }
                                    TextButton(onClick = { viewModel.clearSelection() }) {
                                        Text("Cancel", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed((0 until viewModel.spotifyList.length()).toList()) { index, _ ->
                                val track = viewModel.spotifyList.getJSONObject(index)
                                val title = track.getString("title")
                                val artist = track.getString("artist")
                                val isSelected = viewModel.selectedSongs.contains(index)
                                val isCurrentlyPlaying = viewModel.currentPlayingIndex == index && viewModel.showPlayer

                                var offsetX by remember { mutableStateOf(0f) }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    when {
                                                        offsetX > 100 -> viewModel.toggleSongSelection(index)
                                                        offsetX < -100 -> viewModel.downloadSongAtIndex(index)
                                                    }
                                                    offsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    offsetX += dragAmount
                                                    offsetX = offsetX.coerceIn(-200f, 200f)
                                                }
                                            )
                                        },
                                    color = when {
                                        isCurrentlyPlaying -> SpotifyGreen.copy(alpha = 0.12f)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    shape = RoundedCornerShape(14.dp)
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
                                                    if (!viewModel.isSelectionMode) viewModel.enterSelectionMode()
                                                    viewModel.toggleSongSelection(index)
                                                }
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Track number / album art placeholder
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isCurrentlyPlaying) SpotifyGreen.copy(alpha = 0.3f)
                                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrentlyPlaying && viewModel.isPlaying) {
                                                Icon(
                                                    Icons.Filled.MusicNote,
                                                    null,
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text(
                                                    "${index + 1}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Song info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isCurrentlyPlaying) SpotifyGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Action icons
                                        if (viewModel.isSelectionMode) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { viewModel.toggleSongSelection(index) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = SpotifyGreen,
                                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                                IconButton(
                                                    onClick = { viewModel.playSongAtIndex(index) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        if (isCurrentlyPlaying && viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                        null,
                                                        tint = if (isCurrentlyPlaying) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.downloadSongAtIndex(index) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Download,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ============= COMPLETED STATUS =============
                AnimatedVisibility(
                    visible = viewModel.appStatus == Status.COMPLETED,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SpotifyGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = SpotifyGreen, modifier = Modifier.size(22.dp))
                                Column {
                                    Text("Download Complete!", style = MaterialTheme.typography.titleSmall, color = SpotifyGreen, fontWeight = FontWeight.Bold)
                                    if (viewModel.getFailedDownloadsCount() > 0) {
                                        Text(
                                            "${viewModel.getFailedDownloadsCount()} songs failed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            if (viewModel.getFailedDownloadsCount() > 0) {
                                FilledTonalButton(
                                    onClick = { viewModel.retryFailedDownloads() },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry Failed", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

    }
}

@Composable
fun DownloadDialog(viewModel: HomeScreenViewModel) {
    Dialog(
        onDismissRequest = { viewModel.showDownloadDialog = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Download ${viewModel.selectedSongs.size} Songs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Choose how to download selected songs:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Individual files
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.showDownloadDialog = false
                            viewModel.downloadPlaylist()
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = SpotifyGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Download, null, tint = SpotifyGreen, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Download Individually", fontWeight = FontWeight.SemiBold, color = SpotifyGreen)
                            Text("Save each song as a separate file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ZIP archive
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.downloadSelectedSongsAsZip()
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Archive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Download as ZIP", fontWeight = FontWeight.SemiBold)
                            Text("Compress all songs into one archive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                TextButton(
                    onClick = { viewModel.showDownloadDialog = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

