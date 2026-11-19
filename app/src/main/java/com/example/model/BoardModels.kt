package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DefaultInk

enum class PaperMode {
    LIGHT,
    DARK
}

sealed interface BoardAction {
    data class Stroke(
        val points: List<Offset>,
        val colorArgb: Int,
        val strokeWidthPx: Float,
        val isEraser: Boolean
    ) : BoardAction

    data class ImageOverlay(
        val bitmap: Bitmap
    ) : BoardAction

    data object Clear : BoardAction
}

data class BoardUiState(
    val inkColor: Color = DefaultInk,
    val pencilSize: Float = 8f,
    val isErasing: Boolean = false,
    val paperMode: PaperMode = PaperMode.LIGHT,
    val canUndo: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val isColorPickerOpen: Boolean = false,
    val canvasRevision: Int = 0
)

val PRESET_COLORS: List<Color> = listOf(
    Color(0xFF2D3436),
    Color(0xFF636E72),
    Color(0xFFB2BEC3),
    Color(0xFFFFFFFF),
    Color(0xFFD63031),
    Color(0xFFE17055),
    Color(0xFFFAB1A0),
    Color(0xFFFD79A8),
    Color(0xFFE84393),
    Color(0xFFFDCB6E),
    Color(0xFFF1C40F),
    Color(0xFFFFEAA7),
    Color(0xFF00B894),
    Color(0xFF55EFC4),
    Color(0xFF6AB04C),
    Color(0xFF0984E3),
    Color(0xFF74B9FF),
    Color(0xFF00CEC9),
    Color(0xFF6C5CE7),
    Color(0xFFA29BFE),
    Color(0xFF8D5524)
)
