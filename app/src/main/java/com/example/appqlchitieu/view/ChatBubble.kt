package com.example.appqlchitieu.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChatBubble(
    onClick: () -> Unit,
    size: Dp = 56.dp
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val sizePx = with(density) { size.toPx() }

    //  Animatable cho animation iOS
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(300f) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    offsetX.value.roundToInt(),
                    offsetY.value.roundToInt()
                )
            }
            .size(size)
            .alpha(0.7f) // mờ 70%
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()

                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        val targetX =
                            if (offsetX.value + sizePx / 2 < screenWidthPx / 2) {
                                0f
                            } else {
                                screenWidthPx - sizePx
                            }

                        scope.launch {
                            offsetX.animateTo(
                                targetX,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_ai),
            contentDescription = "AI Chat",
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
