package com.si.myfoodordering.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.myfoodordering.data.model.Category

/** Même pastille que sur l’accueil (filtres catégories). */
@Composable
fun CategoryItem(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val accent = scheme.primary
    val shape = RoundedCornerShape(percent = 50)
    val surfaceColor by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.14f) else scheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "categorySurface"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accent else scheme.outlineVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "categoryBorder"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) accent else scheme.onSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "categoryLabel"
    )

    Surface(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = surfaceColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 0.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = category.emoji ?: "🍴",
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = category.nom,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = labelColor,
                fontSize = 12.sp,
                letterSpacing = 0.02.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp)
            )
        }
    }
}
