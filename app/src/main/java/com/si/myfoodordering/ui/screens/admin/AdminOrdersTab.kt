package com.si.myfoodordering.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.si.myfoodordering.ui.viewmodel.OrderViewModel

@Composable
fun AdminOrdersTab(
    orderViewModel: OrderViewModel,
    onOpenOrderDetail: (Int) -> Unit
) {
    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        orderViewModel.loadAllOrdersForAdmin()
    }

    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = scheme.primary)
        }
        orders.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune commande", color = scheme.onSurfaceVariant)
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders, key = { it.id ?: it.hashCode() }) { order ->
                AdminOrderCard(
                    order = order,
                    onOpenDetail = { order.id?.let(onOpenOrderDetail) },
                    onUpdateStatus = { newStatus ->
                        orderViewModel.updateOrderStatus(order.id ?: 0, newStatus)
                    }
                )
            }
        }
    }
}
