package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PaperMode
import com.example.ui.theme.BoardDashedBorder
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.PaperDark
import com.example.ui.theme.PaperLight

@Composable
fun DrawingCanvasCard(
    committedBitmap: Bitmap?,
    canvasRevision: Int,
    paperMode: PaperMode,
    inkColor: Color,
    pencilSizeDp: Float,
    isErasing: Boolean,
    onCanvasSizeChanged: (Int, Int) -> Unit,
    onStrokeCommitted: (List<Offset>, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val paperColor by animateColorAsState(
        targetValue = if (paperMode == PaperMode.DARK) PaperDark else PaperLight,
        animationSpec = tween(durationMillis = 250),
        label = "paper_background"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { pencilSizeDp.dp.toPx() }
    val activePoints = remember { mutableStateListOf<Offset>() }

    val currentStrokeWidthPx by rememberUpdatedState(strokeWidthPx)
    val currentOnStrokeCommitted by rememberUpdatedState(onStrokeCommitted)

    val cdCanvas = stringResource(R.string.cd_drawing_canvas)

    // Outer white halo + purple shadow, matching .box in the web template
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = 6.dp, bottom = 14.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = BrandPurple.copy(alpha = 0.35f),
                spotColor = BrandPurple.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White)
            .padding(7.dp)
    ) {
        val boardShape = RoundedCornerShape(28.dp)
        val dashColor = BoardDashedBorder

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(boardShape)
                .background(paperColor)
                .drawBehind {
                    val strokePx = 3.dp.toPx()
                    val cornerPx = 28.dp.toPx()
                    drawRoundRect(
                        color = dashColor,
                        style = Stroke(
                            width = strokePx,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10.dp.toPx(), 7.dp.toPx()),
                                0f
                            )
                        ),
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                }
                .padding(3.dp)
                .clip(RoundedCornerShape(25.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("drawing_canvas")
                    .semantics { contentDescription = cdCanvas }
                    .onSizeChanged { size ->
                        onCanvasSizeChanged(size.width, size.height)
                    }
                    .graphicsLayer {
                        // Enables BlendMode.Clear to erase only the ink layer over the paper
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            activePoints.clear()
                            activePoints.add(down.position)

                            var pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId }
                                    ?: event.changes.firstOrNull()
                                    ?: break

                                if (!change.pressed) {
                                    change.consume()
                                    break
                                }
                                pointerId = change.id
                                if (change.position != activePoints.lastOrNull()) {
                                    activePoints.add(change.position)
                                }
                                change.consume()
                            }

                            if (activePoints.isNotEmpty()) {
                                currentOnStrokeCommitted(
                                    activePoints.toList(),
                                    currentStrokeWidthPx
                                )
                                activePoints.clear()
                            }
                        }
                    }
            ) {
                // Reference revision to guarantee redraw when bitmap contents mutate
                @Suppress("UNUSED_VARIABLE")
                val rev = canvasRevision

                committedBitmap?.let { bmp ->
                    drawImage(image = bmp.asImageBitmap())
                }

                if (activePoints.isNotEmpty()) {
                    val blend = if (isErasing) BlendMode.Clear else BlendMode.SrcOver
                    val drawColor = if (isErasing) Color.Black else inkColor

                    // Initial circle dot on touch down
                    drawCircle(
                        color = drawColor,
                        radius = currentStrokeWidthPx / 2f,
                        center = activePoints.first(),
                        blendMode = blend
                    )

                    if (activePoints.size > 1) {
                        val path = Path().apply {
                            var last = activePoints.first()
                            var mid = last
                            moveTo(mid.x, mid.y)
                            for (i in 1 until activePoints.size) {
                                val p = activePoints[i]
                                val newMid = Offset((last.x + p.x) / 2f, (last.y + p.y) / 2f)
                                quadraticTo(last.x, last.y, newMid.x, newMid.y)
                                last = p
                                mid = newMid
                            }
                            lineTo(last.x, last.y)
                        }
                        drawPath(
                            path = path,
                            color = drawColor,
                            style = Stroke(
                                width = currentStrokeWidthPx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            blendMode = blend
                        )
                    }
                }
            }
        }
    }
}
