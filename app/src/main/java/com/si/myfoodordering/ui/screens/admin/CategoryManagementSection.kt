package com.si.myfoodordering.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.myfoodordering.data.model.Category

/**
 * Carte repliable : liste des catégories et dialogues d’ajout / modification / suppression.
 * Placée au-dessus des chips de filtre sur l’écran gestion du menu.
 */
@Composable
fun CategoryManagementSection(
    categories: List<Category>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isBusy: Boolean,
    onSaveCategory: (Category, (Boolean) -> Unit) -> Unit,
    onDeleteCategory: (Int, (Boolean) -> Unit) -> Unit,
) {
    var categoryEditor by remember { mutableStateOf<Category?>(null) }
    var pendingDelete by remember { mutableStateOf<Category?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val sorted = remember(categories) {
        categories.filter { it.id != null }.sortedBy { it.nom.lowercase() }
    }
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isBusy) { onExpandedChange(!expanded) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Category, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(26.dp))
                    Column {
                        Text("Catégories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = scheme.onSurface)
                        Text(
                            "${sorted.size} catégorie(s)",
                            fontSize = 12.sp,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Replier" else "Déplier",
                    tint = scheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.55f))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            categoryEditor = Category(id = null, nom = "", emoji = null)
                        },
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nouvelle catégorie", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (sorted.isEmpty()) {
                        Text(
                            "Aucune catégorie. Crée-en une pour classer tes plats.",
                            fontSize = 13.sp,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        sorted.forEach { cat ->
                            val id = cat.id!!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.emoji?.ifBlank { "·" } ?: "·",
                                    fontSize = 20.sp,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = cat.nom,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    maxLines = 2
                                )
                                IconButton(
                                    onClick = { categoryEditor = cat },
                                    enabled = !isBusy
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Modifier", tint = scheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { pendingDelete = cat },
                                    enabled = !isBusy
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = scheme.error.copy(alpha = 0.85f))
                                }
                            }
                            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.65f))
                        }
                    }
                }
            }
        }
    }

    categoryEditor?.let { draft ->
        key(draft.id, draft.nom, draft.emoji) {
            CategoryFormDialog(
                title = if (draft.id == null) "Nouvelle catégorie" else "Modifier la catégorie",
                initialNom = draft.nom,
                initialEmoji = draft.emoji.orEmpty(),
                isBusy = isBusy,
                onDismiss = { categoryEditor = null },
                onSave = { nom, emoji, onResult ->
                    val toSave = Category(id = draft.id, nom = nom, emoji = emoji)
                    onSaveCategory(toSave) { ok ->
                        onResult(ok)
                        if (ok) categoryEditor = null
                    }
                }
            )
        }
    }

    pendingDelete?.let { cat ->
        val id = cat.id!!
        AlertDialog(
            onDismissRequest = { if (!isBusy) pendingDelete = null },
            title = { Text("Supprimer « ${cat.nom} » ?") },
            text = {
                Text(
                    "Les plats encore liés à cette catégorie empêchent la suppression. Supprime ou réaffecte ces plats d’abord.",
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(id) { success ->
                            if (success) pendingDelete = null
                            else deleteError = "Suppression impossible : des plats utilisent encore cette catégorie."
                        }
                    },
                    enabled = !isBusy
                ) {
                    Text("Supprimer", color = scheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }, enabled = !isBusy) {
                    Text("Annuler")
                }
            }
        )
    }

    deleteError?.let { msg ->
        AlertDialog(
            onDismissRequest = { deleteError = null },
            title = { Text("Action impossible") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { deleteError = null }) {
                    Text("OK", color = scheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CategoryFormDialog(
    title: String,
    initialNom: String,
    initialEmoji: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (nom: String, emoji: String?, onResult: (Boolean) -> Unit) -> Unit,
) {
    var nom by remember(initialNom) { mutableStateOf(initialNom) }
    var emoji by remember(initialEmoji) { mutableStateOf(initialEmoji) }
    var error by remember { mutableStateOf<String?>(null) }
    val scheme = MaterialTheme.colorScheme
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outlineVariant,
        focusedContainerColor = scheme.surface,
        unfocusedContainerColor = scheme.surface
    )

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it; error = null },
                    label = { Text("Nom") },
                    singleLine = true,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { if (it.length <= 8) emoji = it },
                    label = { Text("Emoji (optionnel)") },
                    placeholder = { Text("ex. 🍕", fontSize = 13.sp) },
                    singleLine = true,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                error?.let {
                    Text(it, color = scheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val n = nom.trim()
                    if (n.isEmpty()) {
                        error = "Le nom est obligatoire."
                        return@TextButton
                    }
                    val em = emoji.trim().ifBlank { null }
                    onSave(n, em) { success ->
                        if (!success) {
                            error = "Enregistrement impossible. Vérifie ta connexion ou tes droits."
                        }
                    }
                },
                enabled = !isBusy
            ) {
                Text("Enregistrer", color = scheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text("Annuler")
            }
        }
    )
}
