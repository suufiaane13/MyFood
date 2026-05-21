package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.si.myfoodordering.data.model.CartItem
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val totalAmount = _cartItems.map { items ->
        items.sumOf { it.plat.prix * it.quantity }
    }

    val itemsCount = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }

    fun addToCart(plat: Plat) {
        if (plat.id == null) return
        val currentList = _cartItems.value
        val existingItem = currentList.find { it.plat.id == plat.id }

        val newList = if (existingItem != null) {
            currentList.map { 
                if (it.plat.id == plat.id) it.copy(quantity = it.quantity + 1) else it 
            }
        } else {
            currentList + CartItem(plat, 1)
        }
        _cartItems.value = newList
        UiMessageBus.toast("Ajouté au panier")
    }

    fun removeFromCart(plat: Plat) {
        val currentList = _cartItems.value
        val existingItem = currentList.find { it.plat.id == plat.id }

        if (existingItem != null) {
            val newList = if (existingItem.quantity > 1) {
                currentList.map { 
                    if (it.plat.id == plat.id) it.copy(quantity = it.quantity - 1) else it 
                }
            } else {
                currentList.filter { it.plat.id != plat.id }
            }
            _cartItems.value = newList
            UiMessageBus.toast(
                if (existingItem.quantity > 1) "Quantité mise à jour" else "Retiré du panier"
            )
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}
