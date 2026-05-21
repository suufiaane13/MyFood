package com.si.myfoodordering.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.si.myfoodordering.ui.theme.PrimaryOrange

/** Cercle + bordure orange + icône (même style que l’état vide du panier). */
@Composable
fun BrandedCircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    circleSize: Dp = 132.dp,
    iconSize: Dp = 58.dp,
    circleBackgroundAlpha: Float = 0.12f,
    borderAlpha: Float = 0.18f,
) {
    Surface(
        modifier = modifier.size(circleSize),
        shape = CircleShape,
        color = PrimaryOrange.copy(alpha = circleBackgroundAlpha),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, PrimaryOrange.copy(alpha = borderAlpha))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = PrimaryOrange
            )
        }
    }
}
