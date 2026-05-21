package com.si.myfoodordering.data.repository

import com.si.myfoodordering.data.model.CartItem
import com.si.myfoodordering.data.model.Order
import com.si.myfoodordering.data.model.OrderItem
import com.si.myfoodordering.data.model.OrderWithItems
import com.si.myfoodordering.data.model.Plat
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth

    suspend fun createOrder(order: Order, items: List<CartItem>): Int? = withContext(Dispatchers.IO) {
        return@withContext try {
            val userId = auth.currentUserOrNull()?.id ?: return@withContext null
            val finalOrder = order.copy(user_id = userId)
            
            // 1. Créer la commande
            val createdOrder = postgrest.from("commandes").insert(finalOrder) {
                select()
            }.decodeSingle<Order>()
            
            val orderId = createdOrder.id ?: return@withContext null
            
            // 2. Créer les items de la commande
            val orderItems = items.map { cartItem ->
                OrderItem(
                    commande_id = orderId,
                    plat_id = cartItem.plat.id!!,
                    quantite = cartItem.quantity,
                    prix = cartItem.plat.prix
                )
            }
            
            postgrest.from("commande_items").insert(orderItems)
            
            orderId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getMyOrders(): List<Order> = withContext(Dispatchers.IO) {
        return@withContext try {
            val userId = auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            postgrest.from("commandes")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Order>()
                .sortedByDescending { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllOrdersForAdmin(): List<Order> = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("commandes")
                .select()
                .decodeList<Order>()
                .sortedByDescending { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getOrderItems(orderId: Int): List<OrderWithItems> = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("commande_items").select(Columns.raw("*, plat:plats(*)")) {
                filter { eq("commande_id", orderId) }
            }.decodeList<OrderWithItems>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Tous les items de toutes les commandes — accessible uniquement par l'admin (RLS). */
    suspend fun getAllOrderItems(): List<OrderItem> = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("commande_items").select().decodeList<OrderItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        try {
            postgrest.from("commandes").update(
                {
                    set("statut", newStatus)
                }
            ) {
                filter { eq("id", orderId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
