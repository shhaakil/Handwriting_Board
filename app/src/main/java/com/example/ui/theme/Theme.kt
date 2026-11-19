package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlayfulColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = MenuButtonBg,
    onPrimaryContainer = MenuButtonFg,
    secondary = SoftPurple,
    onSecondary = Color.White,
    secondaryContainer = PaperCardBg,
    onSecondaryContainer = DefaultInk,
    tertiary = PickerOkGreen,
    onTertiary = Color.White,
    background = BgBlue,
    onBackground = DefaultInk,
    surface = Color.White,
    onSurface = DefaultInk,
    surfaceVariant = DrawerBottomBg,
    onSurfaceVariant = SlateGray,
    outline = BoardDashedBorder
)

@Composable
fun HandwritingBoardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PlayfulColorScheme,
        typography = AppTypography,
        content = content
    )
}
