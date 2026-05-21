package com.si.myfoodordering.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int? = null,
    val nom: String,
    val emoji: String? = null
)

@Serializable
data class CategoryPatch(
    val nom: String,
    val emoji: String? = null
)

@Serializable
data class Plat(
    val id: Int? = null,
    val nom: String,
    val description: String,
    val prix: Double,
    val image_url: String? = null,
    val categorie_id: Int
)

@Serializable
data class UserProfile(
    val id: String,
    val nom: String,
    val email: String,
    val telephone: String? = null,
    val role: String = "user",
    val adresse: String? = null 
)

@Serializable
data class Order(
    val id: Int? = null,
    val user_id: String,
    val total: Double,
    val statut: String = "En attente",
    val adresse: String,
    val telephone: String,
    val date: String? = null
)

@Serializable
data class OrderItem(
    val id: Int? = null,
    val commande_id: Int? = null,
    val plat_id: Int,
    val quantite: Int,
    val prix: Double
)

@Serializable
data class CartItem(
    val plat: Plat,
    val quantity: Int
)

@Serializable
data class PushTokenRow(
    val user_id: String,
    val token: String,
    val platform: String = "android",
)

@Serializable
data class OrderWithItems(
    val id: Int? = null,
    val commande_id: Int,
    val plat_id: Int,
    val quantite: Int,
    val prix: Double,
    val plat: Plat? = null
)

@Serializable
data class Avis(
    val id: Int? = null,
    val plat_id: Int,
    val user_id: String,
    val note: Int,
    val date: String? = null
)

@Serializable
data class Favori(
    val id: Int? = null,
    val user_id: String,
    val plat_id: Int,
    val created_at: String? = null
)

data class PlatPopularite(
    val plat: Plat,
    val nbCommandes: Int,
    val noteMoyenne: Double,
    val nbAvis: Int
)
