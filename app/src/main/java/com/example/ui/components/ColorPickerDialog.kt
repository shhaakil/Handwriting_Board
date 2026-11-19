package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.PRESET_COLORS
import com.example.ui.theme.PickerBgBottom
import com.example.ui.theme.PickerBgTop
import com.example.ui.theme.PickerOkGreen
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var hue by remember(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(initialHsv[2]) }

    val selectedColor = Color.hsv(hue, sat, value)
    val hexString = remember(selectedColor) {
        String.format(Locale.US, "#%06X", 0xFFFFFF and selectedColor.toArgb())
    }

    val cdSv = stringResource(R.string.cd_sv_panel)
    val cdHue = stringResource(R.string.cd_hue_slider)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(PickerBgTop, PickerBgBottom)))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = Color(0xFFFFD54A),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SV panel + Hue slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Saturation-Value square
                Canvas(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .semantics { contentDescription = cdSv }
                        .testTag("sv_panel")
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                sat = (down.position.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (down.position.y / size.height).coerceIn(0f, 1f)

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    sat = (change.position.x / size.width).coerceIn(0f, 1f)
                                    value = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                                    change.consume()
                                }
                            }
                        }
                ) {
                    val pureHue = Color.hsv(hue, 1f, 1f)
                    drawRect(color = pureHue)
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, Color.Transparent)
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )

                    val thumbCenter = Offset(
                        x = sat * size.width,
                        y = (1f - value) * size.height
                    )
                    drawCircle(
                        color = selectedColor,
                        radius = 13.dp.toPx(),
                        center = thumbCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 13.dp.toPx(),
                        center = thumbCenter,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }

                // Vertical Hue bar
                Canvas(
                    modifier = Modifier
                        .width(34.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .semantics { contentDescription = cdHue }
                        .testTag("hue_slider")
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                hue = ((down.position.y / size.height).coerceIn(0f, 1f) * 359.99f)

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    hue = ((change.position.y / size.height).coerceIn(0f, 1f) * 359.99f)
                                    change.consume()
                                }
                            }
                        }
                ) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF0000),
                                Color(0xFFFFFF00),
                                Color(0xFF00FF00),
                                Color(0xFF00FFFF),
                                Color(0xFF0000FF),
                                Color(0xFFFF00FF),
                                Color(0xFFFF0000)
                            )
                        )
                    )

                    val thumbCenter = Offset(
                        x = size.width / 2f,
                        y = (hue / 360f).coerceIn(0f, 1f) * size.height
                    )
                    drawCircle(
                        color = Color.hsv(hue, 1f, 1f),
                        radius = 13.dp.toPx(),
                        center = thumbCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 13.dp.toPx(),
                        center = thumbCenter,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview chip + Hex code
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(selectedColor)
                        .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                )
                Text(
                    text = hexString,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 21 preset swatches
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRESET_COLORS.forEachIndexed { index, preset ->
                    val swatchHex = String.format(Locale.US, "#%06X", 0xFFFFFF and preset.toArgb())
                    val swatchDesc = stringResource(R.string.cd_preset_swatch, swatchHex)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(preset)
                            .border(2.5.dp, Color.White, CircleShape)
                            .semantics { contentDescription = swatchDesc }
                            .testTag("preset_color_$index")
                            .clickable(
                                role = Role.Button,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(preset.toArgb(), hsv)
                                    hue = hsv[0]
                                    sat = hsv[1]
                                    value = hsv[2]
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("picker_cancel_button")
                ) {
                    Text(
                        text = stringResource(R.string.picker_cancel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = { onColorSelected(selectedColor) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PickerOkGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("picker_ok_button")
                ) {
                    Text(
                        text = stringResource(R.string.picker_ok),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
