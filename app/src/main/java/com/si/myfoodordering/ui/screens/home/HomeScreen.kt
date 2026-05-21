package com.si.myfoodordering.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.Category
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.components.CategoryItem
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.ui.viewmodel.PlatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlatViewModel = hiltViewModel(),
    favoriIds: Set<Int> = emptySet(),
    onToggleFavori: (Int) -> Unit = {},
    onAddToCart: (Plat) -> Unit = {},
    onNavigateToDetail: (Plat) -> Unit = {}
) {
    val plats by viewModel.plats.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val scheme = MaterialTheme.colorScheme

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlats = remember(plats, selectedCategoryId, searchQuery) {
        plats.filter { plat ->
            (selectedCategoryId == null || plat.categorie_id == selectedCategoryId) &&
                (plat.nom.contains(searchQuery, ignoreCase = true) ||
                    plat.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                showSearch = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        when {
            isLoading && plats.isEmpty() && categories.isEmpty() && loadError == null -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = scheme.primary, strokeWidth = 3.dp)
                }
            }

            loadError != null && plats.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(loadError!!, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Réessayer", fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            CategoryItem(
                                category = Category(id = null, nom = "Tout", emoji = "🍽️"),
                                isSelected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null }
                            )
                        }
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                isSelected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id }
                            )
                        }
                    }

                    if (filteredPlats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = scheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when {
                                    plats.isEmpty() -> "Aucun plat pour le moment."
                                    searchQuery.isNotBlank() -> "Aucun plat ne correspond à ta recherche."
                                    selectedCategoryId != null -> "Aucun plat dans cette catégorie."
                                    else -> "Aucun plat à afficher."
                                },
                                color = scheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                            if (loadError != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.loadData() }) {
                                    Text("Actualiser le menu", color = scheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPlats, key = { it.id ?: it.nom.hashCode() }) { plat ->
                                PlatGridItem(
                                    plat = plat,
                                    isFavori = plat.id != null && plat.id in favoriIds,
                                    onToggleFavori = { plat.id?.let { onToggleFavori(it) } },
                                    onClick = { onNavigateToDetail(plat) },
                                    onAddClick = { onAddToCart(plat) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val PlatCardImageHeight = 118.dp
/** Hauteur fixe grille : image + bloc texte + bouton (+), toutes les cartes alignées. */
private val PlatGridCardHeight = PlatCardImageHeight + 106.dp

@Composable
fun PlatGridItem(
    plat: Plat,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    isFavori: Boolean = false,
    onToggleFavori: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(PlatGridCardHeight)
            .clickable { onClick() },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PlatCardImageHeight)
            ) {
                PlatCoverAsyncImage(
                    imageUrl = plat.image_url,
                    contentDescription = plat.nom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PlatCardImageHeight)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                // Icône cœur favori (haut gauche)
                Surface(
                    onClick = onToggleFavori,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(28.dp),
                    shape = CircleShape,
                    color = scheme.surface.copy(alpha = 0.90f),
                    shadowElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavori) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavori) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (isFavori) scheme.primary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                // Badge prix (bas droite)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = scheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 0.dp
                ) {
                    Text(
                        "${plat.prix.toInt()} DH",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.primary,
                        fontSize = 11.sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        plat.nom,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        plat.description,
                        fontSize = 10.sp,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        minLines = 2,
                        lineHeight = 12.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onAddClick() },
                        shape = CircleShape,
                        color = scheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("+", color = scheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
