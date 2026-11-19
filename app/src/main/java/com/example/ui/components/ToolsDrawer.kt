package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PaperMode
import com.example.ui.theme.AddPictureRowBg
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.DefaultInk
import com.example.ui.theme.DrawerBottomBg
import com.example.ui.theme.DrawerTopBg
import com.example.ui.theme.PaperCardBg
import com.example.ui.theme.PaperCardBorder
import com.example.ui.theme.SavePngRowBg
import com.example.ui.theme.ShareRowBg
import com.example.ui.theme.SizeCardBg
import com.example.ui.theme.SizeCardBorder
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SoftPurple
import kotlin.math.roundToInt

@Composable
fun ToolsDrawerOverlay(
    isOpen: Boolean,
    pencilSize: Float,
    inkColor: Color,
    isErasing: Boolean,
    paperMode: PaperMode,
    onDismiss: () -> Unit,
    onPencilSizeChange: (Float) -> Unit,
    onPaperModeChange: (PaperMode) -> Unit,
    onAddPictureClick: () -> Unit,
    onSavePngClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        BackHandler(onBack = onDismiss)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dim scrim
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DefaultInk.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        // Slide-in drawer panel
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            val drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.82f)
                    .widthIn(max = 310.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = drawerShape,
                        ambientColor = BrandPurple.copy(alpha = 0.3f),
                        spotColor = BrandPurple.copy(alpha = 0.3f)
                    )
                    .clip(drawerShape)
                    .background(Brush.verticalGradient(listOf(DrawerTopBg, DrawerBottomBg)))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume clicks inside drawer
                    )
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.tools_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrandPurple
                    )
                }

                // Pencil size card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SizeCardBg)
                        .border(2.dp, SizeCardBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pencil_size_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = DefaultInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val dotColor = if (isErasing) Color(0xFFB2BEC3) else inkColor
                            val needsOutline = !isErasing && inkColor == Color.White
                            Box(
                                modifier = Modifier
                                    .size(pencilSize.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .then(
                                        if (needsOutline) {
                                            Modifier.border(1.dp, Color(0xFFB2BEC3), CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }

                        Slider(
                            value = pencilSize,
                            onValueChange = onPencilSizeChange,
                            valueRange = 2f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandPurple,
                                activeTrackColor = BrandPurple,
                                inactiveTrackColor = BrandPurple.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pencil_size_slider")
                        )

                        Text(
                            text = pencilSize.roundToInt().toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DefaultInk,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }

                // Paper mode card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(PaperCardBg)
                        .border(2.dp, PaperCardBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.paper_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = DefaultInk
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(2.dp, PaperCardBorder, RoundedCornerShape(18.dp))
                    ) {
                        PaperSegmentButton(
                            label = stringResource(R.string.paper_light),
                            icon = Icons.Rounded.WbSunny,
                            selected = paperMode == PaperMode.LIGHT,
                            onClick = { onPaperModeChange(PaperMode.LIGHT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paper_light_button")
                        )
                        PaperSegmentButton(
                            label = stringResource(R.string.paper_dark),
                            icon = Icons.Rounded.NightlightRound,
                            selected = paperMode == PaperMode.DARK,
                            onClick = { onPaperModeChange(PaperMode.DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paper_dark_button")
                        )
                    }
                }

                // Action rows
                DrawerActionRow(
                    label = stringResource(R.string.action_add_picture),
                    icon = Icons.Rounded.Image,
                    iconTint = Color(0xFFD35400),
                    backgroundColor = AddPictureRowBg,
                    onClick = onAddPictureClick,
                    modifier = Modifier.testTag("add_picture_button")
                )

                DrawerActionRow(
                    label = stringResource(R.string.action_save_png),
                    icon = Icons.Rounded.SaveAlt,
                    iconTint = Color(0xFF00896B),
                    backgroundColor = SavePngRowBg,
                    onClick = onSavePngClick,
                    modifier = Modifier.testTag("save_png_button")
                )

                DrawerActionRow(
                    label = stringResource(R.string.action_share),
                    icon = Icons.Rounded.Share,
                    iconTint = Color(0xFF0B62B5),
                    backgroundColor = ShareRowBg,
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_button")
                )
            }
        }
    }
}

@Composable
private fun PaperSegmentButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgModifier = if (selected) {
        Modifier.background(Brush.linearGradient(listOf(SoftPurple, BrandPurple)))
    } else {
        Modifier.background(Color.Transparent)
    }

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(bgModifier)
            .clickable(
                role = Role.RadioButton,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else SlateGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) Color.White else SlateGray
        )
    }
}

@Composable
private fun DrawerActionRow(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TactileButtonContainer(
        onClick = onClick,
        backgroundColor = backgroundColor,
        cornerRadius = 22.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = DefaultInk
            )
        }
    }
}
