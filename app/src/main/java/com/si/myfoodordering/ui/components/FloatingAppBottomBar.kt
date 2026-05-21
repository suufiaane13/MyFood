package com.si.myfoodordering.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FloatingBottomNavItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

/**
 * Barre de navigation flottante unique pour l’app client et l’espace admin (même design).
 */
@Composable
fun FloatingAppBottomBar(
    modifier: Modifier = Modifier,
    items: List<FloatingBottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    /** Index de l’onglet qui affiche un badge (ex. panier client). */
    badgeIndex: Int? = null,
    badgeCount: Int = 0,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface,
        shadowElevation = 15.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedIndex == index
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!selected) onSelect(index)
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (badgeIndex == index && badgeCount > 0) {
                                    Badge(
                                        containerColor = scheme.primary,
                                        contentColor = scheme.onPrimary,
                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text(
                                            text = badgeCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.title,
                                tint = if (selected) scheme.primary else scheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(scheme.primary, CircleShape)
                            )
                        } else {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
