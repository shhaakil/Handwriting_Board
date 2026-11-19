package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RainbowColors

@Composable
fun TactileButtonContainer(
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 18.dp,
    activeRingColor: Color? = null,
    description: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 70),
        label = "tactile_offset"
    )
    val lipHeight by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 5.dp,
        animationSpec = tween(durationMillis = 70),
        label = "tactile_lip"
    )

    val outerShape = RoundedCornerShape(cornerRadius)
    val innerShape = RoundedCornerShape(cornerRadius)
    val lipColor = Color.Black.copy(alpha = 0.20f)

    val ringModifier = if (activeRingColor != null) {
        Modifier
            .border(3.dp, activeRingColor, RoundedCornerShape(cornerRadius + 3.dp))
            .padding(3.dp)
            .border(3.dp, Color.White, outerShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else 0.4f)
            .then(ringModifier)
            .offset(y = pressOffset)
            .clip(outerShape)
            .background(lipColor)
            .padding(bottom = lipHeight)
            .clip(innerShape)
            .background(backgroundColor)
            .then(
                if (description != null) {
                    Modifier.semantics { contentDescription = description }
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun RainbowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(5.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.horizontalGradient(RainbowColors))
    )
}
