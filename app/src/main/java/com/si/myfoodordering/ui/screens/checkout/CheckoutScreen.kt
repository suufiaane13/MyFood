package com.si.myfoodordering.ui.screens.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.CartItem
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.ui.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    cartItems: List<CartItem>,
    total: Double,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val telephone by viewModel.telephone.collectAsState()
    val adresse by viewModel.adresse.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val hasExistingDetails by viewModel.hasExistingDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        val msg = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeErrorMessage()
    }

    val sortedItems = remember(cartItems) {
        cartItems.sortedBy { it.plat.nom.lowercase() }
    }
    val totalArticles = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Validation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Vérifie ton panier et la livraison",
                            fontSize = 12.sp,
                            color = scheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 16.dp).size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = scheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
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
            if (!isLoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 12.dp,
                    color = scheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total à payer", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    "${total.toInt()} DH",
                                    color = scheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                )
                                Text(
                                    "$totalArticles article(s)",
                                    fontSize = 11.sp,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.submitOrder(cartItems, total, onSuccess) },
                                enabled = telephone.isNotBlank() && adresse.isNotBlank() && !isSubmitting,
                                modifier = Modifier
                                    .height(52.dp)
                                    .widthIn(min = 148.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.primary,
                                    disabledContainerColor = scheme.primary.copy(alpha = 0.45f)
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = scheme.onPrimary,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Commander", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            "Paiement à la livraison (espèces)",
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            fontSize = 11.sp,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Section 1 : Récap panier
                CheckoutSectionHeader(
                    icon = Icons.Outlined.ShoppingBag,
                    title = "Ton panier",
                    subtitle = "${sortedItems.size} plat(s) · $totalArticles article(s)"
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        sortedItems.forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = scheme.outlineVariant
                                )
                            }
                            CheckoutLineRow(item = item)
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = scheme.outlineVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sous-total", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                            Text(
                                "${total.toInt()} DH",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = scheme.primary
                            )
                        }
                    }
                }

                // --- Section 2 : Livraison
                CheckoutSectionHeader(
                    icon = Icons.Outlined.LocalShipping,
                    title = "Livraison",
                    subtitle = if (hasExistingDetails) "Coordonnées enregistrées sur ton profil"
                    else "Indique où et comment te joindre"
                )

                if (hasExistingDetails) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            CheckoutInfoRow(
                                icon = Icons.Outlined.Phone,
                                label = "Téléphone",
                                value = telephone
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            CheckoutInfoRow(
                                icon = Icons.Outlined.Home,
                                label = "Adresse",
                                value = adresse
                            )
                            TextButton(
                                onClick = { viewModel.setEditMode() },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Modifier", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = telephone,
                            onValueChange = { viewModel.onTelephoneChange(it) },
                            label = { Text("Téléphone") },
                            placeholder = { Text("Ex. 06 12 34 56 78", fontSize = 14.sp, color = scheme.onSurfaceVariant) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Phone, contentDescription = null, tint = scheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                unfocusedBorderColor = scheme.outlineVariant,
                                focusedContainerColor = scheme.surface,
                                unfocusedContainerColor = scheme.surface
                            )
                        )
                        OutlinedTextField(
                            value = adresse,
                            onValueChange = { viewModel.onAdresseChange(it) },
                            label = { Text("Adresse complète") },
                            placeholder = {
                                Text("Rue, quartier, ville…", fontSize = 14.sp, color = scheme.onSurfaceVariant)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Home, contentDescription = null, tint = scheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                unfocusedBorderColor = scheme.outlineVariant,
                                focusedContainerColor = scheme.surface,
                                unfocusedContainerColor = scheme.surface
                            )
                        )
                    }
                }

                // --- Section 3 : Paiement (info)
                CheckoutSectionHeader(
                    icon = Icons.Outlined.Payments,
                    title = "Paiement",
                    subtitle = "Tu paieras au livreur à la réception"
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = scheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Payments,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Espèces à la livraison",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Prépare l’appoint ou le montant exact si possible.",
                                fontSize = 12.sp,
                                color = scheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CheckoutSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = scheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CheckoutLineRow(item: CartItem) {
    val scheme = MaterialTheme.colorScheme
    val lineTotal = item.plat.prix * item.quantity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlatCoverAsyncImage(
            imageUrl = item.plat.image_url,
            contentDescription = item.plat.nom,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.plat.nom,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Text(
                "${item.plat.prix.toInt()} DH × ${item.quantity}",
                fontSize = 12.sp,
                color = scheme.onSurfaceVariant
            )
        }
        Text(
            "${lineTotal.toInt()} DH",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = scheme.primary
        )
    }
}

@Composable
private fun CheckoutInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = scheme.primary.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
        }
    }
}
