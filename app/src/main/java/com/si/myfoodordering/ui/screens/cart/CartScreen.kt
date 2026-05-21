package com.si.myfoodordering.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.myfoodordering.ui.components.BrandedCircleIcon
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.data.model.CartItem
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.theme.SecondaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onIncrement: (com.si.myfoodordering.data.model.Plat) -> Unit,
    onDecrement: (com.si.myfoodordering.data.model.Plat) -> Unit,
    onRemove: (com.si.myfoodordering.data.model.Plat) -> Unit,
    onCheckout: () -> Unit
) {
    val totalPrice = cartItems.sumOf { it.plat.prix * it.quantity }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppHeader(title = "Mon Panier", compact = true)
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 100.dp),
                    shadowElevation = 15.dp,
                    color = scheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total", fontSize = 14.sp, color = scheme.onSurfaceVariant)
                                Text("${totalPrice.toInt()} DH", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = scheme.primary)
                            }
                            Button(
                                onClick = onCheckout,
                                modifier = Modifier.height(56.dp).width(160.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            ) {
                                Text("Payer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BrandedCircleIcon(icon = Icons.Outlined.ShoppingCart)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Votre panier est vide",
                    color = scheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Parcours le menu et ajoute tes plats préférés",
                    color = SecondaryOrange.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems, key = { it.plat.id ?: 0 }) { item ->
                    CartItemCard(item, onIncrement, onDecrement, onRemove)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: (com.si.myfoodordering.data.model.Plat) -> Unit,
    onDecrement: (com.si.myfoodordering.data.model.Plat) -> Unit,
    onRemove: (com.si.myfoodordering.data.model.Plat) -> Unit
) {
    val plat = item.plat
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlatCoverAsyncImage(
                imageUrl = plat.image_url,
                contentDescription = plat.nom,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(plat.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    IconButton(
                        onClick = { onRemove(plat) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f))
                    }
                }
                Text("${plat.prix.toInt()} DH", color = scheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Boutons de quantité améliorés
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(scheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Surface(
                        onClick = { onDecrement(plat) },
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Remove, null, modifier = Modifier.size(14.dp), tint = scheme.onSurface)
                        }
                    }
                    
                    Text(
                        "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    
                    Surface(
                        onClick = { onIncrement(plat) },
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Add, null, tint = scheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
