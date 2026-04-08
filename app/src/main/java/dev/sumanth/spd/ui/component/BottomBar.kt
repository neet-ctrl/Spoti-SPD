package dev.sumanth.spd.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.model.NavigationItem
import dev.sumanth.spd.ui.theme.SpotifyGreen
import kotlinx.coroutines.launch

@Composable
fun BottomBar(navigationItems: List<NavigationItem>, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEachIndexed { index, item ->
                val isSelected = pagerState.currentPage == index

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "iconColor"
                )

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) SpotifyGreen.copy(alpha = 0.12f) else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "bgColor"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 0.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "indicatorWidth"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor
                    )

                    Box(
                        modifier = Modifier
                            .width(indicatorWidth)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(SpotifyGreen)
                    )
                }
            }
        }
    }
}
