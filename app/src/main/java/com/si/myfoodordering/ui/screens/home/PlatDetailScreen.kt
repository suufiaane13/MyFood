package com.si.myfoodordering.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.data.model.Avis
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.components.PlatCoverAsyncImage
import com.si.myfoodordering.ui.components.RatingBar
import com.si.myfoodordering.ui.viewmodel.AvisViewModel
import com.si.myfoodordering.ui.viewmodel.FavorisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatDetailScreen(
    plat: Plat,
    isFavori: Boolean,
    onToggleFavori: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onBack: () -> Unit,
    avisViewModel: AvisViewModel = hiltViewModel(),
    favorisViewModel: FavorisViewModel = hiltViewModel(),
) {
    var quantity by remember { mutableStateOf(1) }
    var showAvisDialog by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    val avis by avisViewModel.avis.collectAsState()
    val myAvis by avisViewModel.myAvis.collectAsState()

    LaunchedEffect(plat.id) {
        plat.id?.let { avisViewModel.loadAvis(it) }
    }

    val averageNote = if (avis.isEmpty()) 0f else avis.sumOf { it.note }.toFloat() / avis.size

    if (showAvisDialog) {
        AvisDialog(
            platNom = plat.nom,
            myAvis = myAvis,
            onSubmit = { note ->
                plat.id?.let { avisViewModel.submitAvis(it, note) }
                showAvisDialog = false
            },
            onDelete = {
                plat.id?.let { avisViewModel.deleteMyAvis(it) }
                showAvisDialog = false
            },
            onDismiss = { showAvisDialog = false }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 20.dp,
                color = scheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.surfaceVariant,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp).background(scheme.surface, CircleShape)
                            ) {
                                Icon(Icons.Outlined.Remove, null, modifier = Modifier.size(18.dp), tint = scheme.onSurface)
                            }
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(36.dp).background(scheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Outlined.Add, null, tint = scheme.onPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Button(
                        onClick = { onAddToCart(quantity) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                    ) {
                        Text("Ajouter • ${(plat.prix * quantity).toInt()} DH", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                PlatCoverAsyncImage(
                    imageUrl = plat.image_url,
                    contentDescription = plat.nom,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )

                // Bouton retour
                Surface(
                    onClick = onBack,
                    modifier = Modifier.padding(24.dp).size(44.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surface.copy(alpha = 0.92f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, modifier = Modifier.size(20.dp), tint = scheme.onSurface)
                    }
                }

                // Bouton favori
                Surface(
                    onClick = onToggleFavori,
                    modifier = Modifier.padding(24.dp).size(44.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surface.copy(alpha = 0.92f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavori) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavori) "Retirer des favoris" else "Ajouter aux favoris",
                            modifier = Modifier.size(20.dp),
                            tint = if (isFavori) scheme.primary else scheme.onSurface
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .background(scheme.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(24.dp)
            ) {
                // Titre + note moyenne
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plat.nom, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RatingBar(rating = averageNote, starSize = 16.dp, gap = 1.dp)
                            Text(
                                text = if (avis.isEmpty()) "Aucun avis" else "${avis.size} avis",
                                fontSize = 12.sp,
                                color = scheme.onSurfaceVariant
                            )
                            Text("·", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                            Text(
                                text = if (myAvis != null) "Modifier mon avis" else "Donner mon avis",
                                fontSize = 12.sp,
                                color = scheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = androidx.compose.ui.Modifier.clickable { showAvisDialog = true }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("À propos du plat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (plat.description.isNotBlank()) plat.description
                    else "Un délice préparé avec soin par nos chefs, utilisant des ingrédients frais pour une explosion de saveurs.",
                    color = scheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AvisDialog(
    platNom: String,
    myAvis: Avis?,
    onSubmit: (Int) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var selectedNote by remember { mutableIntStateOf(myAvis?.note ?: 0) }

    val starLabels = listOf("Mauvais", "Passable", "Bien", "Très bien", "Excellent")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = scheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Votre avis",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    platNom,
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Étoiles interactives
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= selectedNote) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Note $star",
                            tint = if (star <= selectedNote) Color(0xFFFF6B01) else scheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedNote = star }
                        )
                    }
                }

                // Label de la note sélectionnée
                Text(
                    text = if (selectedNote > 0) starLabels[selectedNote - 1] else "Touche une étoile pour noter",
                    fontSize = 13.sp,
                    color = if (selectedNote > 0) Color(0xFFFF6B01) else scheme.onSurfaceVariant,
                    fontWeight = if (selectedNote > 0) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                // Supprimer l'avis existant
                if (myAvis != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)
                    ) {
                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Supprimer mon avis", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedNote > 0) onSubmit(selectedNote) },
                enabled = selectedNote > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                    disabledContainerColor = scheme.surfaceVariant,
                    disabledContentColor = scheme.onSurfaceVariant
                )
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = scheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun PlatNotFoundScreen(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Plat introuvable", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Ce plat n'est plus disponible ou le menu a été mis à jour.",
            color = scheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Retour", fontWeight = FontWeight.Bold)
        }
    }
}
