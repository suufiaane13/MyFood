package com.si.myfoodordering.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.Category
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.components.CategoryItem
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.ui.viewmodel.PlatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlatsScreen(
    viewModel: PlatViewModel = hiltViewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Plat) -> Unit,
    onBack: () -> Unit = {},
    embeddedInTab: Boolean = false
) {
    val plats by viewModel.plats.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categorySectionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(categories, selectedCategoryId) {
        val id = selectedCategoryId ?: return@LaunchedEffect
        if (categories.none { it.id == id }) {
            selectedCategoryId = null
        }
    }

    val filteredPlats = remember(plats, selectedCategoryId) {
        if (selectedCategoryId == null) plats
        else plats.filter { it.categorie_id == selectedCategoryId }
    }
    val scheme = MaterialTheme.colorScheme

    @Composable
    fun CategoryFilterRow() {
        LazyRow(
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CategoryItem(
                    category = Category(id = null, nom = "Tout", emoji = "🍽️"),
                    isSelected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null }
                )
            }
            items(categories, key = { it.id ?: it.nom.hashCode() }) { cat ->
                val cid = cat.id ?: return@items
                CategoryItem(
                    category = cat,
                    isSelected = selectedCategoryId == cid,
                    onClick = { selectedCategoryId = cid }
                )
            }
        }
    }

    @Composable
    fun PlatsList(modifier: Modifier) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CategoryManagementSection(
                        categories = categories,
                        expanded = categorySectionExpanded,
                        onExpandedChange = { categorySectionExpanded = it },
                        isBusy = isLoading,
                        onSaveCategory = { cat, done -> viewModel.saveCategory(cat, done) },
                        onDeleteCategory = { id, done ->
                            viewModel.deleteCategory(id) { success ->
                                if (success && selectedCategoryId == id) {
                                    selectedCategoryId = null
                                }
                                done(success)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Filtrer les plats",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryFilterRow()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            if (filteredPlats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (plats.isEmpty()) "Aucun plat chargé."
                            else "Aucun plat dans cette catégorie.",
                            color = scheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredPlats, key = { it.id ?: it.nom.hashCode() }) { plat ->
                    AdminPlatCard(
                        plat = plat,
                        onEdit = { onNavigateToEdit(plat) },
                        onDelete = {
                            plat.id?.let { viewModel.deletePlat(it) }
                        }
                    )
                }
            }
        }
    }

    if (embeddedInTab) {
        PlatsList(Modifier.padding(top = 8.dp))
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestion du Menu", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) { Text("←", fontSize = 24.sp) }
                            IconButton(onClick = onNavigateToAdd) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "Ajouter un plat",
                                    tint = scheme.primary
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            PlatsList(
                Modifier
                    .padding(padding)
                    .padding(top = 8.dp)
            )
        }
    }
}

private val AdminPlatCardHeight = 104.dp

@Composable
fun AdminPlatCard(plat: Plat, onEdit: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(AdminPlatCardHeight),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlatCoverAsyncImage(
                imageUrl = plat.image_url,
                contentDescription = plat.nom,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    plat.nom,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${plat.prix.toInt()} DH",
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Modifier", tint = scheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = scheme.error.copy(alpha = 0.85f))
                }
            }
        }
    }
}
