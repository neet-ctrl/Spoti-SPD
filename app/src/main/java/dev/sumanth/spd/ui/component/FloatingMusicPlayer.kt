package dev.sumanth.spd.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.theme.SpotifyGreen
import dev.sumanth.spd.ui.theme.SpotifyGreenLight
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.RepeatMode

@Composable
fun FloatingMusicPlayer(viewModel: HomeScreenViewModel = viewModel()) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(10f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePlayerCollapse() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(SpotifyGreen, SpotifyGreenLight)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        viewModel.getCurrentSong()?.title ?: "No song selected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
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
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.toggleFavorite() }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        if (viewModel.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        null,
                                        tint = if (viewModel.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.closePlayer() }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        Icons.Filled.Close,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = !viewModel.isPlayerCollapsed,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))

                                if (viewModel.isPlayerLoading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp)),
                                        color = SpotifyGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        viewModel.currentTime.toInt().let { "%d:%02d".format(it / 60, it % 60) },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        viewModel.duration.toInt().let { "%d:%02d".format(it / 60, it % 60) },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Slider(
                                    value = viewModel.currentTime.coerceIn(0f, viewModel.duration),
                                    onValueChange = { viewModel.seekTo(it) },
                                    valueRange = 0f..viewModel.duration.coerceAtLeast(1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = SpotifyGreen,
                                        activeTrackColor = SpotifyGreen,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.toggleShuffle() }, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                            Icons.Filled.Shuffle,
                                            null,
                                            tint = if (viewModel.isShuffleMode) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.previousSong() }, modifier = Modifier.size(44.dp)) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                            .background(SpotifyGreen)
                                            .clickable { viewModel.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (viewModel.isPlayerLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(28.dp),
                                                color = Color.Black,
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            Icon(
                                                if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.nextSong() }, modifier = Modifier.size(44.dp)) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.toggleRepeatMode() }, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                            when (viewModel.repeatMode) {
                                                RepeatMode.ONE -> Icons.Filled.RepeatOne
                                                else -> Icons.Filled.Repeat
                                            },
                                            null,
                                            tint = if (viewModel.repeatMode != RepeatMode.NONE) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.VolumeDown,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Slider(
                                        value = viewModel.volume,
                                        onValueChange = { viewModel.setVolume(it) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.onSurface,
                                            activeTrackColor = MaterialTheme.colorScheme.onSurface,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                    )
                                    Icon(
                                        Icons.Filled.VolumeUp,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = viewModel.isPlayerCollapsed,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.previousSong() }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SpotifyGreen)
                                        .clickable { viewModel.togglePlayPause() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.nextSong() }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
