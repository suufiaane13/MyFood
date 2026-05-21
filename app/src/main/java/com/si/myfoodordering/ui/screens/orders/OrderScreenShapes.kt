package com.si.myfoodordering.ui.screens.orders

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Rayons partagés pour l’écran détail commande. */
object OrderRadius {
    val navChip = RoundedCornerShape(12.dp)
    val bottomBar = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val card = RoundedCornerShape(20.dp)
    val iconTile = RoundedCornerShape(10.dp)
    val lineItemCard = RoundedCornerShape(16.dp)
    val thumbImage = RoundedCornerShape(12.dp)
}
