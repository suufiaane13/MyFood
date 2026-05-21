package com.si.myfoodordering.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.ui.screens.admin.AdminOrderStatusActions
import com.si.myfoodordering.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Int,
    isAdmin: Boolean = false,
    orderViewModel: OrderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val orders by orderViewModel.orders.collectAsState()
    val items by orderViewModel.selectedOrderItems.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val order = orders.find { it.id == orderId }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(orderId, isAdmin) {
        if (isAdmin) {
            orderViewModel.loadAllOrdersForAdmin()
        }
        orderViewModel.loadOrderDetails(orderId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails Commande", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 16.dp).size(40.dp),
                        shape = OrderRadius.navChip,
                        color = scheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Retour",
                                modifier = Modifier.size(20.dp),
                                tint = scheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.surface)
            )
        },
        bottomBar = {
            if (!isLoading && order != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 20.dp,
                    color = scheme.surface,
                    shape = OrderRadius.bottomBar
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Montant total", color = scheme.onSurfaceVariant, fontSize = 13.sp)
                            Text("Payé via Cash", color = scheme.primary.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${order.total.toInt()} DH",
                            color = scheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = scheme.primary)
            }
        } else if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Commande introuvable", color = scheme.onSurfaceVariant)
            }
        } else {
            val dateTimeParts = order.date?.split("T")
            val date = dateTimeParts?.firstOrNull() ?: "Récemment"
            val time = dateTimeParts?.getOrNull(1)?.split(".")?.firstOrNull()?.substring(0, 5) ?: ""

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Résumé Statut et Date/Heure
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrderRadius.card,
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Statut", fontWeight = FontWeight.Bold, color = scheme.onSurfaceVariant, fontSize = 14.sp)
                                StatusBadge(status = order.statut)
                            }

                            val canMarkNext = isAdmin && order.statut != "Livré"
                            if (canMarkNext) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = scheme.surfaceVariant,
                                    border = BorderStroke(1.dp, scheme.outlineVariant),
                                    shadowElevation = 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            "Marquer comme",
                                            fontWeight = FontWeight.SemiBold,
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.02.sp
                                        )
                                        AdminOrderStatusActions(
                                            statut = order.statut,
                                            modifier = Modifier,
                                            onUpdateStatus = { newStatus ->
                                                order.id?.let { id ->
                                                    orderViewModel.updateOrderStatus(id, newStatus)
                                                }
                                            }
                                        )
                                        Text(
                                            text = "?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = scheme.primary.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = scheme.outlineVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarToday, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                                Text(" $date", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                if (time.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(Icons.Outlined.AccessTime, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                                    Text(" $time", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Infos Livraison
                item {
                    Text("Informations de livraison", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = scheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrderRadius.card,
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = OrderRadius.iconTile,
                                    color = scheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.LocationOn, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Adresse", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                                    Text(order.adresse, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = OrderRadius.iconTile,
                                    color = scheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Phone, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Téléphone", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                                    Text(order.telephone, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Liste des plats
                item {
                    Text("Articles commandés", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = scheme.onSurface)
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrderRadius.lineItemCard,
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlatCoverAsyncImage(
                                imageUrl = item.plat?.image_url,
                                contentDescription = item.plat?.nom,
                                modifier = Modifier.size(64.dp).clip(OrderRadius.thumbImage),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.plat?.nom ?: "Plat #${item.plat_id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Quantité: ${item.quantite}", fontSize = 13.sp, color = scheme.onSurfaceVariant)
                            }
                            Text("${(item.prix * item.quantite).toInt()} DH", fontWeight = FontWeight.Bold, color = scheme.primary, fontSize = 16.sp)
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
