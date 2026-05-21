package com.si.myfoodordering.ui.screens.orders

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.Order
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.theme.SuccessGreen
import com.si.myfoodordering.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyOrders()
    }

    val scheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            AppHeader(title = "Commandes", compact = true)
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = scheme.primary)
            }
        } else if (orders.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = scheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aucune commande pour le moment", color = scheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderItemCard(
                        order = order,
                        onClick = { onNavigateToDetail(order.id ?: 0) }
                    )
                }
            }
        }
    }
}

/**
 * @param footer Contenu sous le bandeau statut (ex. actions admin). Si non null, seul ce bloc
 *               n’est pas inclus dans la zone cliquable ; le haut de carte ouvre [onClick].
 */
@Composable
fun OrderItemCard(
    order: Order,
    onClick: () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val dateTimeParts = order.date?.split("T")
    val date = dateTimeParts?.firstOrNull() ?: "Récemment"
    val time = dateTimeParts?.getOrNull(1)?.split(".")?.firstOrNull()?.substring(0, 5) ?: ""

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "orderCardPress"
    )

    val scheme = MaterialTheme.colorScheme

    val clickModifier = Modifier.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (footer == null) clickModifier else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (footer != null) clickModifier else Modifier)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = scheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = scheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Commande #${order.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = scheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(date, fontSize = 12.sp, color = scheme.onSurfaceVariant)
                            if (time.isNotEmpty()) {
                                Text(" • ", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                                Icon(
                                    Icons.Outlined.AccessTime,
                                    null,
                                    modifier = Modifier.size(12.dp),
                                    tint = scheme.onSurfaceVariant
                                )
                                Text(" $time", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                            }
                        }
                    }

                    Text(
                        "${order.total.toInt()} DH",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = scheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(thickness = 0.5.dp, color = scheme.outlineVariant.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = order.statut)
                    Text("Détails →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = scheme.primary)
                }
            }

            if (footer != null) {
                Spacer(modifier = Modifier.height(14.dp))
                footer()
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val primary = MaterialTheme.colorScheme.primary
    val (color, icon) = when (status) {
        "En attente" -> Color(0xFFFFA000) to Icons.Outlined.Schedule
        "En préparation" -> primary to Icons.Outlined.Schedule
        "En livraison" -> Color(0xFF2196F3) to Icons.Outlined.LocalShipping
        "Livré" -> SuccessGreen to Icons.Outlined.LocalShipping
        else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Outlined.Schedule
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
