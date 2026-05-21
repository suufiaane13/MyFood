package com.si.myfoodordering.ui.screens.favoris

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.screens.home.PlatGridItem
import com.si.myfoodordering.ui.viewmodel.FavorisViewModel

@Composable
fun FavorisScreen(
    viewModel: FavorisViewModel = hiltViewModel(),
    favoriIds: Set<Int>,
    onToggleFavori: (Int) -> Unit,
    onNavigateToDetail: (Plat) -> Unit,
    onAddToCart: (Plat) -> Unit,
) {
    val favoris by viewModel.favoris.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.loadFavoris()
    }

    Scaffold(
        topBar = { AppHeader(title = "Favoris", compact = true) },
        containerColor = Color.Transparent
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = scheme.primary, strokeWidth = 3.dp)
                }
            }

            favoris.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = scheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aucun favori pour le moment",
                        color = scheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Appuie sur le cœur d'un plat pour l'ajouter ici.",
                        color = scheme.outlineVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = padding.calculateTopPadding() + 12.dp,
                        end = 16.dp,
                        bottom = 120.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoris, key = { it.id ?: it.nom.hashCode() }) { plat ->
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
