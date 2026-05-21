package com.si.myfoodordering.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Barre d'étoiles réutilisable.
 *
 * - Mode lecture seule : [onRatingChange] = null
 * - Mode interactif : tap sur une étoile déclenche [onRatingChange] avec la valeur (1-[starCount])
 * - [rating] peut être fractionnaire (ex. 3.7f) pour le mode lecture seule
 */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starSize: Dp = 20.dp,
    gap: Dp = 2.dp,
    onRatingChange: ((Int) -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..starCount) {
            val filled = i <= rating
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "$i étoile${if (i > 1) "s" else ""}",
                tint = if (filled) scheme.primary else scheme.outlineVariant,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onRatingChange(i) }
                        } else Modifier
                    )
            )
        }
    }
}
