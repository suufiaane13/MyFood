package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.Order
import com.si.myfoodordering.data.model.OrderItem
import com.si.myfoodordering.data.model.OrderWithItems
import com.si.myfoodordering.data.repository.OrderRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _selectedOrderItems = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val selectedOrderItems: StateFlow<List<OrderWithItems>> = _selectedOrderItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _orders.value = orderRepository.getMyOrders().sortedByDescending { it.id }
            _isLoading.value = false
        }
    }

    fun loadAllOrdersForAdmin() {
        viewModelScope.launch {
            _isLoading.value = true
            _orders.value = orderRepository.getAllOrdersForAdmin().sortedByDescending { it.id }
            _isLoading.value = false
        }
    }

    fun loadOrderDetails(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedOrderItems.value = orderRepository.getOrderItems(orderId)
            _isLoading.value = false
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            if (orderRepository.updateOrderStatus(orderId, newStatus)) {
                loadAllOrdersForAdmin()
                UiMessageBus.toast("Statut : $newStatus")
            } else {
                UiMessageBus.toast("Impossible de mettre à jour le statut")
            }
        }
    }
}
