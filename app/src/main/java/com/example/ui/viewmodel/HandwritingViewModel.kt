package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.example.model.BoardAction
import com.example.model.BoardUiState
import com.example.model.PaperMode
import com.example.ui.theme.DefaultInk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min

class HandwritingViewModel : ViewModel() {

    companion object {
        private const val MAX_HISTORY = 20
    }

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val actions = mutableListOf<BoardAction>()
    private var baseBitmap: Bitmap? = null
    private var committedBitmap: Bitmap? = null

    var canvasWidthPx: Int = 0
        private set
    var canvasHeightPx: Int = 0
        private set

    fun getCommittedBitmap(): Bitmap? = committedBitmap

    fun onCanvasSizeChanged(widthPx: Int, heightPx: Int) {
        if (widthPx <= 0 || heightPx <= 0) return
        if (widthPx == canvasWidthPx && heightPx == canvasHeightPx) return

        val prevBase = baseBitmap
        canvasWidthPx = widthPx
        canvasHeightPx = heightPx

        if (prevBase != null) {
            val resizedBase = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            Canvas(resizedBase).drawBitmap(prevBase, 0f, 0f, null)
            baseBitmap = resizedBase
        }
        rebuildCommittedBitmap()
    }

    fun commitStroke(points: List<Offset>, strokeWidthPx: Float) {
        if (points.isEmpty()) return
        val state = _uiState.value
        val action = BoardAction.Stroke(
            points = points,
            colorArgb = state.inkColor.toArgb(),
            strokeWidthPx = strokeWidthPx,
            isEraser = state.isErasing
        )
        recordAction(action)
        committedBitmap?.let { bmp ->
            drawAction(Canvas(bmp), action, bmp.width, bmp.height)
            bumpRevision()
        }
    }

    fun addImage(bitmap: Bitmap) {
        val action = BoardAction.ImageOverlay(bitmap)
        recordAction(action)
        committedBitmap?.let { bmp ->
            drawAction(Canvas(bmp), action, bmp.width, bmp.height)
            bumpRevision()
        }
    }

    fun clear() {
        val action = BoardAction.Clear
        recordAction(action)
        committedBitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
        bumpRevision()
    }

    fun undo() {
        if (actions.isEmpty()) return
        actions.removeAt(actions.lastIndex)
        rebuildCommittedBitmap()
    }

    fun setColor(color: Color) {
        _uiState.update {
            it.copy(
                inkColor = color,
                isErasing = false,
                isColorPickerOpen = false
            )
        }
    }

    fun toggleEraser() {
        _uiState.update { it.copy(isErasing = !it.isErasing) }
    }

    fun setPencilSize(size: Float) {
        _uiState.update { it.copy(pencilSize = size.coerceIn(2f, 30f)) }
    }

    fun setPaperMode(mode: PaperMode) {
        _uiState.update { state ->
            val updatedColor = when {
                mode == PaperMode.DARK && state.inkColor == DefaultInk -> Color.White
                mode == PaperMode.LIGHT && state.inkColor == Color.White -> DefaultInk
                else -> state.inkColor
            }
            state.copy(
                paperMode = mode,
                inkColor = updatedColor,
                isErasing = if (updatedColor != state.inkColor) false else state.isErasing
            )
        }
    }

    fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true) }
    }

    fun closeDrawer() {
        _uiState.update { it.copy(isDrawerOpen = false) }
    }

    fun openColorPicker() {
        _uiState.update { it.copy(isColorPickerOpen = true) }
    }

    fun closeColorPicker() {
        _uiState.update { it.copy(isColorPickerOpen = false) }
    }

    private fun recordAction(action: BoardAction) {
        actions.add(action)
        if (actions.size > MAX_HISTORY) {
            val oldest = actions.removeAt(0)
            if (oldest is BoardAction.Clear) {
                baseBitmap = null
            } else if (canvasWidthPx > 0 && canvasHeightPx > 0) {
                val base = baseBitmap ?: Bitmap.createBitmap(
                    canvasWidthPx,
                    canvasHeightPx,
                    Bitmap.Config.ARGB_8888
                ).also { baseBitmap = it }
                drawAction(Canvas(base), oldest, base.width, base.height)
            }
        }
    }

    private fun rebuildCommittedBitmap() {
        if (canvasWidthPx <= 0 || canvasHeightPx <= 0) return
        val target = committedBitmap?.takeIf {
            it.width == canvasWidthPx && it.height == canvasHeightPx
        } ?: Bitmap.createBitmap(canvasWidthPx, canvasHeightPx, Bitmap.Config.ARGB_8888)
            .also { committedBitmap = it }

        target.eraseColor(android.graphics.Color.TRANSPARENT)
        val canvas = Canvas(target)

        // Start from last Clear action if one exists inside the undo window
        val lastClearIndex = actions.indexOfLast { it is BoardAction.Clear }
        if (lastClearIndex < 0) {
            baseBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }
        val startIndex = if (lastClearIndex >= 0) lastClearIndex + 1 else 0
        for (i in startIndex until actions.size) {
            drawAction(canvas, actions[i], target.width, target.height)
        }
        bumpRevision()
    }

    private fun bumpRevision() {
        _uiState.update {
            it.copy(
                canUndo = actions.isNotEmpty(),
                canvasRevision = it.canvasRevision + 1
            )
        }
    }

    private fun drawAction(
        canvas: Canvas,
        action: BoardAction,
        width: Int,
        height: Int
    ) {
        when (action) {
            is BoardAction.Clear -> {
                canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }

            is BoardAction.ImageOverlay -> {
                val bmp = action.bitmap
                val scale = min(width.toFloat() / bmp.width, height.toFloat() / bmp.height)
                val drawW = bmp.width * scale
                val drawH = bmp.height * scale
                val left = (width - drawW) / 2f
                val top = (height - drawH) / 2f
                val dest = RectF(left, top, left + drawW, top + drawH)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bmp, null, dest, paint)
            }

            is BoardAction.Stroke -> {
                val pts = action.points
                if (pts.isEmpty()) return
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = action.strokeWidthPx
                    if (action.isEraser) {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                        style = Paint.Style.STROKE
                    } else {
                        color = action.colorArgb
                        style = Paint.Style.STROKE
                    }
                }
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    if (action.isEraser) {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    } else {
                        color = action.colorArgb
                    }
                }
                canvas.drawCircle(pts[0].x, pts[0].y, action.strokeWidthPx / 2f, dotPaint)

                if (pts.size > 1) {
                    val path = Path().apply {
                        var last = pts[0]
                        var mid = pts[0]
                        moveTo(mid.x, mid.y)
                        for (i in 1 until pts.size) {
                            val p = pts[i]
                            val newMid = Offset((last.x + p.x) / 2f, (last.y + p.y) / 2f)
                            quadTo(last.x, last.y, newMid.x, newMid.y)
                            last = p
                            mid = newMid
                        }
                        lineTo(last.x, last.y)
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
    }
}
