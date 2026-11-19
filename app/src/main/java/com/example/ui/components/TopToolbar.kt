package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BoardDashedBorder
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.ClearButtonBg
import com.example.ui.theme.ClearButtonFg
import com.example.ui.theme.EraserActiveBg
import com.example.ui.theme.EraserActiveFg
import com.example.ui.theme.EraserActiveRing
import com.example.ui.theme.EraserButtonBg
import com.example.ui.theme.EraserButtonFg
import com.example.ui.theme.MenuButtonBg
import com.example.ui.theme.MenuButtonFg
import com.example.ui.theme.UndoButtonBg
import com.example.ui.theme.UndoButtonFg

@Composable
fun TopToolbar(
    inkColor: Color,
    isErasing: Boolean,
    canUndo: Boolean,
    onMenuClick: () -> Unit,
    onColorClick: () -> Unit,
    onEraserClick: () -> Unit,
    onUndoClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // Menu button
            TactileButtonContainer(
                onClick = onMenuClick,
                backgroundColor = MenuButtonBg,
                description = stringResource(R.string.cd_open_menu),
                modifier = Modifier
                    .size(50.dp)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = MenuButtonFg,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Color droplet button
            TactileButtonContainer(
                onClick = onColorClick,
                backgroundColor = Color.White,
                description = stringResource(R.string.cd_choose_colour),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("color_picker_button")
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(-45f)
                            .border(2.5.dp, BoardDashedBorder, RoundedCornerShape(9.dp, 9.dp, 9.dp, 2.dp))
                            .padding(2.5.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(7.dp, 7.dp, 7.dp, 2.dp))
                            .clip(RoundedCornerShape(7.dp, 7.dp, 7.dp, 2.dp))
                            .background(inkColor)
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Eraser button
            TactileButtonContainer(
                onClick = onEraserClick,
                backgroundColor = if (isErasing) EraserActiveBg else EraserButtonBg,
                activeRingColor = if (isErasing) EraserActiveRing else null,
                description = stringResource(R.string.cd_eraser),
                modifier = Modifier
                    .size(50.dp)
                    .testTag("eraser_button")
            ) {
                EraserIcon(
                    tint = if (isErasing) EraserActiveFg else EraserButtonFg,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Undo button
            TactileButtonContainer(
                onClick = onUndoClick,
                enabled = canUndo,
                backgroundColor = UndoButtonBg,
                description = stringResource(R.string.cd_undo),
                modifier = Modifier
                    .size(50.dp)
                    .testTag("undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = null,
                    tint = UndoButtonFg,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Clear button
            TactileButtonContainer(
                onClick = onClearClick,
                backgroundColor = ClearButtonBg,
                description = stringResource(R.string.cd_clear_screen),
                modifier = Modifier
                    .size(50.dp)
                    .testTag("clear_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = ClearButtonFg,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        RainbowDivider()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun EraserIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        val strokeWidth = 2.6f * sx

        // Baseline: M8 19 h11
        drawLine(
            color = tint,
            start = Offset(8f * sx, 19f * sy),
            end = Offset(19f * sx, 19f * sy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Eraser body: M5.5 14.5 l8 -8 a2.1 2.1 0 0 1 3 0 l2 2 a2.1 2.1 0 0 1 0 3 l-6 6 H9.5 l-4 -4
        val body = Path().apply {
            moveTo(5.2f * sx, 14.3f * sy)
            lineTo(13.5f * sx, 6.0f * sy)
            quadraticTo(15.0f * sx, 4.5f * sy, 16.5f * sx, 6.0f * sy)
            lineTo(18.5f * sx, 8.0f * sy)
            quadraticTo(20.0f * sx, 9.5f * sy, 18.5f * sx, 11.0f * sy)
            lineTo(12.5f * sx, 17.0f * sy)
            lineTo(9.5f * sx, 17.0f * sy)
            lineTo(5.2f * sx, 14.3f * sy)
            close()
        }
        drawPath(
            path = body,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Divider seam across eraser
        drawLine(
            color = tint,
            start = Offset(9.5f * sx, 10.0f * sy),
            end = Offset(14.5f * sx, 15.0f * sy),
            strokeWidth = strokeWidth * 0.85f,
            cap = StrokeCap.Round
        )
    }
}
