package dev.sumanth.spd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = modernShapes,
        typography = modernTypography,
        content = content
    )
}

private val modernShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

private val modernTypography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.material3.Typography().displayLarge.copy(
        fontSize = 36.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = androidx.compose.material3.Typography().displayMedium.copy(
        fontSize = 28.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = androidx.compose.material3.Typography().displaySmall.copy(
        fontSize = 22.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineLarge = androidx.compose.material3.Typography().headlineLarge.copy(
        fontSize = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineMedium = androidx.compose.material3.Typography().headlineMedium.copy(
        fontSize = 18.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineSmall = androidx.compose.material3.Typography().headlineSmall.copy(
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleLarge = androidx.compose.material3.Typography().titleLarge.copy(
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.material3.Typography().titleMedium.copy(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleSmall = androidx.compose.material3.Typography().titleSmall.copy(
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    bodyLarge = androidx.compose.material3.Typography().bodyLarge.copy(
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = androidx.compose.material3.Typography().bodyMedium.copy(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 20.sp
    ),
    bodySmall = androidx.compose.material3.Typography().bodySmall.copy(
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 16.sp
    ),
    labelLarge = androidx.compose.material3.Typography().labelLarge.copy(
        fontSize = 13.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    labelMedium = androidx.compose.material3.Typography().labelMedium.copy(
        fontSize = 11.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        letterSpacing = 0.2.sp
    ),
    labelSmall = androidx.compose.material3.Typography().labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        letterSpacing = 0.2.sp
    )
)
