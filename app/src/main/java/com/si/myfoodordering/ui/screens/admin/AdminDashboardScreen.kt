package com.si.myfoodordering.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.si.myfoodordering.data.model.Order
import com.si.myfoodordering.data.model.PlatPopularite
import com.si.myfoodordering.ui.screens.orders.OrderItemCard
import com.si.myfoodordering.ui.screens.profile.ProfileSectionTitle
import com.si.myfoodordering.ui.theme.PrimaryOrange
import com.si.myfoodordering.ui.theme.SuccessGreen
import com.si.myfoodordering.ui.viewmodel.AdminStatsViewModel
import com.si.myfoodordering.ui.viewmodel.OrderViewModel
import com.si.myfoodordering.ui.viewmodel.PlatViewModel
import com.si.myfoodordering.ui.viewmodel.PopulariteSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    platViewModel: PlatViewModel = hiltViewModel(),
    statsViewModel: AdminStatsViewModel = hiltViewModel(),
    onNavigateToPlats: () -> Unit = {},
    onBack: () -> Unit = {},
    embeddedInTab: Boolean = false,
    onOpenOrderDetail: (Int) -> Unit = {},
) {
    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val plats by platViewModel.plats.collectAsState()
    val categories by platViewModel.categories.collectAsState()
    val topPlats by statsViewModel.topPlats.collectAsState()
    val statsLoading by statsViewModel.isLoading.collectAsState()
    val currentSort by statsViewModel.sort.collectAsState()

    LaunchedEffect(Unit) {
        orderViewModel.loadAllOrdersForAdmin()
        statsViewModel.loadStats()
    }

    val scheme = MaterialTheme.colorScheme

    val stats = remember(orders) {
        val pending = orders.count { it.statut == "En attente" }
        val prep = orders.count { it.statut == "En préparation" }
        val ship = orders.count { it.statut == "En livraison" }
        val done = orders.count { it.statut == "Livré" }
        AdminDashStats(
            pending = pending,
            preparation = prep,
            livraison = ship,
            livre = done
        )
    }

    val priorityOrders = remember(orders) {
        orders
            .filter { it.statut != "Livré" }
            .sortedByDescending { it.id ?: 0 }
            .take(6)
    }

    val totalCa = remember(orders) { orders.sumOf { it.total }.toInt() }

    @Composable
    fun DashboardScroll(modifier: Modifier = Modifier) {
        if (isLoading && orders.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = scheme.primary)
            }
            return
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(scheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Commandes",
                        value = orders.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Chiffre d’affaires",
                        value = "$totalCa DH",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Plats au menu",
                        value = plats.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Catégories",
                        value = categories.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ProfileSectionTitle("Répartition par statut")
                StatusPipelineCard(stats = stats)
            }

            item {
                ProfileSectionTitle("Plats les plus populaires")
                PlatsPopulairesCard(
                    topPlats = topPlats,
                    isLoading = statsLoading,
                    currentSort = currentSort,
                    onSortChange = { statsViewModel.setSort(it) }
                )
            }

            item {
                ProfileSectionTitle(
                    if (embeddedInTab) "À traiter en priorité"
                    else "Liste des commandes"
                )
                if (embeddedInTab && priorityOrders.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen.copy(alpha = 0.8f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                if (orders.isEmpty()) "Aucune commande"
                                else "Rien en attente",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (orders.isEmpty()) {
                                    "Les nouvelles commandes appara\u00eetront ici."
                                } else {
                                    "Toutes les commandes sont livr\u00e9es. Ouvre l'onglet Commandes pour l'historique."
                                },
                                fontSize = 13.sp,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (embeddedInTab) {
                items(priorityOrders, key = { it.id ?: it.hashCode() }) { order ->
                    AdminOrderCard(
                        order = order,
                        onOpenDetail = { order.id?.let(onOpenOrderDetail) },
                        onUpdateStatus = { newStatus ->
                            orderViewModel.updateOrderStatus(order.id ?: 0, newStatus)
                        }
                    )
                }
            } else if (orders.isNotEmpty()) {
                items(orders, key = { it.id ?: it.hashCode() }) { order ->
                    AdminOrderCard(
                        order = order,
                        onOpenDetail = { order.id?.let(onOpenOrderDetail) },
                        onUpdateStatus = { newStatus ->
                            orderViewModel.updateOrderStatus(order.id ?: 0, newStatus)
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.7f))
                    ) {
                        Text(
                            "Aucune commande pour le moment.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            color = scheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (embeddedInTab) {
        DashboardScroll(Modifier)
    } else {
        Scaffold(
            containerColor = scheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Tableau de bord", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Text("←", fontSize = 24.sp) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.surface)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToPlats,
                    icon = { Icon(Icons.Outlined.RestaurantMenu, contentDescription = null) },
                    text = { Text("Gérer les plats") },
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                )
            }
        ) { padding ->
            DashboardScroll(Modifier.padding(padding))
        }
    }
}

private data class AdminDashStats(
    val pending: Int,
    val preparation: Int,
    val livraison: Int,
    val livre: Int,
)

@Composable
private fun StatusPipelineCard(stats: AdminDashStats) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            PipelineCell(Icons.Outlined.Schedule, "Attente", stats.pending, Color(0xFFFFA000))
            PipelineCell(Icons.Outlined.RestaurantMenu, "Préparation", stats.preparation, scheme.primary)
            PipelineCell(Icons.Outlined.LocalShipping, "Livraison", stats.livraison, Color(0xFF2196F3))
            PipelineCell(Icons.Outlined.CheckCircle, "Livré", stats.livre, SuccessGreen)
        }
    }
}

@Composable
private fun PipelineCell(
    icon: ImageVector,
    label: String,
    count: Int,
    accent: Color,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 72.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
        Text(
            label,
            fontSize = 10.sp,
            color = scheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = PrimaryOrange,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        }
    }
}

/** Actions de workflow (même logique que les cartes liste admin). */
@Composable
fun AdminOrderStatusActions(
    statut: String,
    modifier: Modifier = Modifier,
    onUpdateStatus: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (statut == "En attente") {
            ActionButton("Préparer", Icons.Outlined.Timer, scheme.primary) { onUpdateStatus("En préparation") }
        }
        if (statut == "En préparation") {
            ActionButton("Livrer", Icons.Outlined.DeliveryDining, Color(0xFF2196F3)) { onUpdateStatus("En livraison") }
        }
        if (statut == "En livraison") {
            ActionButton("Terminer", Icons.Outlined.CheckCircle, SuccessGreen) { onUpdateStatus("Livré") }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onOpenDetail: () -> Unit = {},
    onUpdateStatus: (String) -> Unit,
) {
    OrderItemCard(
        order = order,
        onClick = onOpenDetail,
        footer = null
    )
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlatsPopulairesCard(
    topPlats: List<PlatPopularite>,
    isLoading: Boolean,
    currentSort: PopulariteSort,
    onSortChange: (PopulariteSort) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Toggle tri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    label = "Commandes",
                    icon = Icons.Outlined.ShoppingBag,
                    selected = currentSort == PopulariteSort.PAR_COMMANDES,
                    onClick = { onSortChange(PopulariteSort.PAR_COMMANDES) }
                )
                SortChip(
                    label = "Note",
                    icon = Icons.Filled.Star,
                    selected = currentSort == PopulariteSort.PAR_NOTE,
                    onClick = { onSortChange(PopulariteSort.PAR_NOTE) }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = scheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (topPlats.isEmpty()) {
                Text(
                    "Aucune donnée disponible pour le moment.",
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                topPlats.forEachIndexed { index, item ->
                    PlatPopulariteRow(rank = index + 1, item = item, sort = currentSort)
                    if (index < topPlats.lastIndex) {
                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) scheme.primary.copy(alpha = 0.12f) else scheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) scheme.primary else scheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) scheme.primary else scheme.onSurfaceVariant
            )
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) scheme.primary else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlatPopulariteRow(rank: Int, item: PlatPopularite, sort: PopulariteSort) {
    val scheme = MaterialTheme.colorScheme
    val rankColor = when (rank) {
        1 -> Color(0xFFFFB300)
        2 -> Color(0xFF9E9E9E)
        3 -> Color(0xFFCD7F32)
        else -> scheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badge classement
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$rank",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = rankColor
            )
        }

        // Image du plat
        AsyncImage(
            model = item.plat.image_url,
            contentDescription = item.plat.nom,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        // Nom + stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.plat.nom,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${item.plat.prix.toInt()} DH",
                fontSize = 11.sp,
                color = scheme.onSurfaceVariant
            )
        }

        // Métrique principale mise en avant
        if (sort == PopulariteSort.PAR_COMMANDES) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${item.nbCommandes}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.primary
                )
                Text(
                    "cmdés",
                    fontSize = 10.sp,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        if (item.noteMoyenne > 0) "%.1f".format(item.noteMoyenne) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )
                }
                Text(
                    "${item.nbAvis} avis",
                    fontSize = 10.sp,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}
