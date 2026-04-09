package dev.sumanth.spd.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.model.LocalPlaybackItem
import dev.sumanth.spd.model.LocalSong
import dev.sumanth.spd.ui.theme.SpotifyGreen
import dev.sumanth.spd.ui.theme.SpotifyGreenLight
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.LibrarySortOrder
import dev.sumanth.spd.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun colorForText(text: String): Color {
    val palette = listOf(
        Color(0xFF1DB954), Color(0xFF2196F3), Color(0xFFE91E63),
        Color(0xFFFF5722), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFF9800), Color(0xFF4CAF50), Color(0xFF795548),
        Color(0xFF607D8B), Color(0xFF3F51B5), Color(0xFF009688)
    )
    return palette[abs(text.hashCode()) % palette.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    homeViewModel: HomeScreenViewModel,
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val songs by libraryViewModel.filteredSortedSongs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val scanProgress by libraryViewModel.scanProgress.collectAsState()
    val sortOrder by libraryViewModel.sortOrder.collectAsState()
    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val scanWholeStorage by libraryViewModel.scanWholeStorage.collectAsState()
    val scanPath by libraryViewModel.scanPath.collectAsState()
    val totalDuration by libraryViewModel.totalDuration.collectAsState()
    val totalSize by libraryViewModel.totalSize.collectAsState()
    val songCount by libraryViewModel.songCount.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<LocalSong?>(null) }

    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
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
                val newPath = "$storageBase/$folderPath"
                libraryViewModel.setLibraryScanPath(newPath)
            }
        }
    }

    val infiniteTransitionFab = rememberInfiniteTransition(label = "fabSpin")
    val infiniteFabRotation by infiniteTransitionFab.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "fabInfiniteRotation"
    )
    val fabRotation = if (isScanning) infiniteFabRotation else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                LibraryHeader(
                    songCount = songCount,
                    totalDuration = totalDuration,
                    totalSize = totalSize,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    scanWholeStorage = scanWholeStorage,
                    scanPath = scanPath,
                    onScanWholeStorageChange = { libraryViewModel.setScanWholeStorage(it) },
                    onChangeLocation = { launcher.launch(null) }
                )
            }

            item {
                LibraryControls(
                    searchQuery = searchQuery,
                    searchActive = searchActive,
                    sortOrder = sortOrder,
                    onSearchToggle = { searchActive = !searchActive },
                    onQueryChange = { libraryViewModel.setSearchQuery(it) },
                    onSortClick = { showSortSheet = true },
                    onClearSearch = {
                        libraryViewModel.setSearchQuery("")
                        searchActive = false
                    },
                    onSortOrderChange = { libraryViewModel.setSortOrder(it) }
                )
            }

            if (songs.isEmpty() && !isScanning) {
                item {
                    LibraryEmptyState(onScan = { libraryViewModel.refresh() })
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.filePath }
            ) { index, song ->
                val isCurrentlyPlaying = homeViewModel.isLocalPlayback &&
                        homeViewModel.currentLocalFilePath == song.filePath

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = (index * 30).coerceAtMost(300))) +
                            slideInVertically(
                                tween(300, delayMillis = (index * 30).coerceAtMost(300))
                            ) { it / 2 }
                ) {
                    SwipeableSongItem(
                        song = song,
                        isPlaying = isCurrentlyPlaying && homeViewModel.isPlaying,
                        isCurrentSong = isCurrentlyPlaying,
                        onTap = {
                            val playlist = songs.map {
                                LocalPlaybackItem(it.filePath, it.title, it.artist)
                            }
                            homeViewModel.playLocalPlaylist(playlist, index)
                        },
                        onFavorite = { libraryViewModel.toggleFavorite(song) },
                        onDelete = { songToDelete = song }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(if (homeViewModel.showPlayer) 100.dp else 16.dp)) }
        }

        FloatingActionButton(
            onClick = { if (!isScanning) libraryViewModel.refresh() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (homeViewModel.showPlayer) 110.dp else 24.dp, end = 16.dp),
            containerColor = SpotifyGreen,
            contentColor = Color.Black
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Refresh Library",
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            SortBottomSheet(
                currentOrder = sortOrder,
                onSelect = {
                    libraryViewModel.setSortOrder(it)
                    scope.launch { sortSheetState.hide() }.invokeOnCompletion { showSortSheet = false }
                }
            )
        }
    }

    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete Song") },
            text = {
                Text(
                    "Remove \"${song.title}\" from your library? This will delete the file permanently.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (homeViewModel.currentLocalFilePath == song.filePath) {
                            homeViewModel.closePlayer()
                        }
                        libraryViewModel.deleteSong(song)
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
@Composable
private fun LibraryHeader(
    songCount: Int,
    totalDuration: Long,
    totalSize: Long,
    isScanning: Boolean,
    scanProgress: Pair<Int, Int>,
    scanWholeStorage: Boolean,
    scanPath: String,
    onScanWholeStorageChange: (Boolean) -> Unit,
    onChangeLocation: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutCubic)),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpotifyGreen.copy(alpha = 0.3f),
                        SpotifyGreen.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(SpotifyGreen, SpotifyGreenLight))
                        )
                        .scale(if (isScanning) pulse else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Headphones,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "My Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (isScanning) "Scanning your music…"
                        else "Your downloaded tracks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (scanWholeStorage) "Whole Storage" else "Selected Folder",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = scanWholeStorage,
                        onCheckedChange = onScanWholeStorageChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpotifyGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                }
            }

            // Scan location display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Folder,
                        null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (scanWholeStorage) "Scanning: Whole Internal Storage"
                        else "Scanning: ${scanPath.replace(Environment.getExternalStorageDirectory().path, "Internal Storage")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!scanWholeStorage) {
                        IconButton(
                            onClick = onChangeLocation,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.MusicNote,
                    label = "Songs",
                    value = "$songCount"
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.PlayArrow,
                    label = "Duration",
                    value = formatDuration(totalDuration)
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Album,
                    label = "Storage",
                    value = formatSize(totalSize)
                )
            }

            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val progress = if (scanProgress.second > 0)
                        scanProgress.first.toFloat() / scanProgress.second
                    else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = SpotifyGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Text(
                        if (scanProgress.second > 0)
                            "Scanned ${scanProgress.first} of ${scanProgress.second} files"
                        else "Scanning…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                null,
                tint = SpotifyGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LibraryControls(
    searchQuery: String,
    searchActive: Boolean,
    sortOrder: LibrarySortOrder,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onClearSearch: () -> Unit,
    onSortOrderChange: (LibrarySortOrder) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchActive) {
        if (searchActive) {
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(spring(stiffness = Spring.StiffnessMedium)),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        null,
                        tint = if (searchActive) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onSearchToggle() }
                    )
                    if (searchActive) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(SpotifyGreen),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search songs, artists…",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Close,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onClearSearch() }
                            )
                        }
                    } else {
                        Text(
                            "Search songs, artists…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSearchToggle() }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable(onClick = onSortClick),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Sort,
                        null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orders = listOf(
                LibrarySortOrder.DATE_ADDED to "Recent",
                LibrarySortOrder.TITLE_ASC to "A-Z",
                LibrarySortOrder.ARTIST_ASC to "Artist",
                LibrarySortOrder.DURATION_DESC to "Duration",
                LibrarySortOrder.FAVORITES_FIRST to "Favorites"
            )
            items(orders.size) { i ->
                val (order, label) = orders[i]
                val selected = sortOrder == order
                val chipColor by animateColorAsState(
                    if (selected) SpotifyGreen else MaterialTheme.colorScheme.surfaceContainer,
                    label = "chipColor"
                )
                val textColor by animateColorAsState(
                    if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
                    label = "textColor"
                )
                Surface(
                    modifier = Modifier.clickable { onSortOrderChange(order) },
                    shape = RoundedCornerShape(20.dp),
                    color = chipColor
                ) {
                    Text(
                        label,
                        modifier = Modifier
                            .clickable { onSortOrderChange(order) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableSongItem(
    song: LocalSong,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onTap: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "swipeOffset"
    )

    val revealFraction = (-animatedOffset / 200f).coerceIn(0f, 1f)
    val bgColor by animateColorAsState(
        targetValue = if (revealFraction > 0.3f) MaterialTheme.colorScheme.errorContainer
        else Color.Transparent,
        label = "swipeBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .alpha(revealFraction * 2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Delete,
                null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .background(
                    if (isCurrentSong)
                        SpotifyGreen.copy(alpha = 0.06f)
                    else MaterialTheme.colorScheme.background
                )
                .clickable { onTap() }
                .pointerInput(song.filePath) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -120f) {
                                onDelete()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                    }
                }
        ) {
            SongItemContent(
                song = song,
                isPlaying = isPlaying,
                isCurrentSong = isCurrentSong,
                onFavorite = onFavorite
            )
        }
    }
}

@Composable
private fun SongItemContent(
    song: LocalSong,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onFavorite: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playingPulse")
    val playingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = EaseInOutCubic)
        ),
        label = "playingAlpha"
    )

    val avatarColor = colorForText(song.artist)
    val initial = song.title.firstOrNull()?.uppercaseChar() ?: '?'

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(avatarColor, avatarColor.copy(alpha = 0.6f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrentSong) {
                Icon(
                    if (isPlaying) Icons.Filled.MusicNote else Icons.Filled.PlayArrow,
                    null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (isPlaying) playingAlpha else 1f)
                )
            } else {
                Text(
                    initial.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentSong) SpotifyGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.album,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                formatDuration(song.duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        null,
                        tint = if (song.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(SpotifyGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                null,
                tint = SpotifyGreen,
                modifier = Modifier.size(44.dp)
            )
        }
        Text(
            "No songs found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Download some songs from the Home tab, then refresh to see them here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            onClick = onScan,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotifyGreen,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scan Library", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
@Composable
private fun SortBottomSheet(
    currentOrder: LibrarySortOrder,
    onSelect: (LibrarySortOrder) -> Unit
) {
    val options = listOf(
        LibrarySortOrder.DATE_ADDED to "Recently Added",
        LibrarySortOrder.TITLE_ASC to "Title A → Z",
        LibrarySortOrder.TITLE_DESC to "Title Z → A",
        LibrarySortOrder.ARTIST_ASC to "Artist A → Z",
        LibrarySortOrder.DURATION_DESC to "Longest First",
        LibrarySortOrder.FAVORITES_FIRST to "Favorites First"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Sort Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        options.forEach { (order, label) ->
            val isSelected = currentOrder == order
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(order) }
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) SpotifyGreen.copy(alpha = 0.1f) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(order) },
                        modifier = Modifier.size(20.dp),
                        colors = RadioButtonDefaults.colors(selectedColor = SpotifyGreen)
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
