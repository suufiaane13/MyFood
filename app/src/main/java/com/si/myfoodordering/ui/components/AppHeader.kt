package com.si.myfoodordering.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.myfoodordering.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String? = null,
    showSearch: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onBack: (() -> Unit)? = null,
    /** En-tête plus bas pour les onglets principaux (plus de place au contenu). */
    compact: Boolean = false,
    /** Actions à gauche (ex. bouton Ajouter), avant retour / logo. */
    leadingActions: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val topPad = if (compact) 10.dp else 16.dp
    val bottomPad = if (compact) 8.dp else 12.dp
    val logoSize = if (compact) 32.dp else 38.dp
    val titleSp = if (compact) 18.sp else 20.sp

    Column(
        modifier = Modifier
            .background(scheme.surface)
            .padding(top = topPad, bottom = bottomPad)
            .fillMaxWidth()
    ) {
        if (showSearch) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    leadingActions()
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp).padding(end = 4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = scheme.onSurface
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(logoSize)
                    )
                }
                val searchInteraction = remember { MutableInteractionSource() }
                val isFocused by searchInteraction.collectIsFocusedAsState()

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceVariant,
                    border = if (isFocused)
                        BorderStroke(1.dp, scheme.primary)
                    else
                        BorderStroke(1.dp, Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isFocused) scheme.primary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            interactionSource = searchInteraction,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = scheme.onSurface
                            ),
                            cursorBrush = SolidColor(scheme.primary),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Rechercher un plat…",
                                            fontSize = 13.sp,
                                            color = scheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    leadingActions()
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = scheme.onSurface
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(logoSize)
                    )
                }

                if (title != null) {
                    Text(
                        text = title,
                        fontSize = titleSp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
        }
    }
}
