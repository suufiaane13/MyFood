package com.si.myfoodordering.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.ui.viewmodel.PlatViewModel
import kotlinx.coroutines.launch

private const val MaxImageBytes = 6 * 1024 * 1024

private fun formatImageUploadError(e: Throwable): String {
    val m = e.message ?: e.javaClass.simpleName
    return when {
        m.contains("401", ignoreCase = true) ||
            m.contains("JWT", ignoreCase = true) ||
            m.contains("not authenticated", ignoreCase = true) ->
            "Session expirée ou absente. Reconnecte-toi (compte admin) puis réessaie."

        m.contains("403", ignoreCase = true) ||
            m.contains("Forbidden", ignoreCase = true) ||
            m.contains("row-level security", ignoreCase = true) ||
            m.contains("RLS", ignoreCase = true) ||
            m.contains("violates row-level security", ignoreCase = true) ->
            "Accès refusé au stockage. Dans Supabase : exécute le script SQL « supabase_storage_plats.sql » " +
                "(bucket « plats » + politiques). Vérifie aussi que ton profil a bien role = admin."

        m.contains("404", ignoreCase = true) ||
            m.contains("Bucket not found", ignoreCase = true) ||
            (m.contains("not found", ignoreCase = true) && m.contains("bucket", ignoreCase = true)) ->
            "Bucket « plats » introuvable. Crée-le dans Storage (nom exact : plats) ou exécute le script SQL du projet."

        else -> "Envoi impossible : ${m.take(180)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlatScreen(
    platId: Int? = null,
    viewModel: PlatViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val plats by viewModel.plats.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val existingPlat = remember(platId, plats) { plats.find { it.id == platId } }

    var nom by remember(platId) { mutableStateOf("") }
    var description by remember(platId) { mutableStateOf("") }
    var prix by remember(platId) { mutableStateOf("") }
    var remoteImageUrl by remember(platId) { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var userClearedImage by remember { mutableStateOf(false) }
    var selectedCategoryId by remember(platId) { mutableStateOf(1) }

    var formError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedUri = uri
            userClearedImage = false
            formError = null
        }
    }

    LaunchedEffect(platId) {
        if (platId == null) {
            nom = ""
            description = ""
            prix = ""
            remoteImageUrl = ""
            pickedUri = null
            userClearedImage = false
        }
    }

    LaunchedEffect(platId, existingPlat) {
        val p = existingPlat ?: return@LaunchedEffect
        if (platId == null) return@LaunchedEffect
        nom = p.nom
        description = p.description
        prix = p.prix.toString()
        remoteImageUrl = p.image_url.orEmpty()
        pickedUri = null
        userClearedImage = false
        selectedCategoryId = p.categorie_id
    }

    LaunchedEffect(platId, categories) {
        if (platId == null && categories.isNotEmpty() && nom.isEmpty() && description.isEmpty()) {
            selectedCategoryId = categories.first().id ?: 1
        }
    }

    val previewModel: Any? = when {
        pickedUri != null -> pickedUri
        userClearedImage -> null
        remoteImageUrl.isNotBlank() -> remoteImageUrl
        else -> null
    }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    val categoryFieldValue = selectedCategory?.nom.orEmpty()
    val scheme = MaterialTheme.colorScheme
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outlineVariant,
        focusedContainerColor = scheme.surfaceVariant,
        unfocusedContainerColor = scheme.surfaceVariant
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (platId == null) "Ajouter un plat" else "Modifier le plat", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = scheme.onSurface) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = categoryFieldValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Catégorie") },
                    placeholder = {
                        Text(
                            if (categories.isEmpty()) "Chargement…" else "Sélectionner une catégorie",
                            color = scheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, contentDescription = null, tint = scheme.primary)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    buildString {
                                        if (!cat.emoji.isNullOrBlank()) {
                                            append(cat.emoji)
                                            append(' ')
                                        }
                                        append(cat.nom)
                                    }
                                )
                            },
                            onClick = {
                                cat.id?.let { selectedCategoryId = it }
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom du plat") },
                leadingIcon = { Icon(Icons.Outlined.Restaurant, null, tint = scheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Outlined.Description, null, tint = scheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = prix,
                onValueChange = { prix = it },
                label = { Text("Prix (DH)") },
                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null, tint = scheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            Text("Photo du plat", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = scheme.onSurface)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Aperçu du plat",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = scheme.outlineVariant, modifier = Modifier.size(48.dp))
                        Text("Aucune image", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        pickImageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary)
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choisir une image", fontWeight = FontWeight.SemiBold)
                }
            }

            if (pickedUri != null || (!userClearedImage && remoteImageUrl.isNotBlank())) {
                TextButton(
                    onClick = {
                        pickedUri = null
                        userClearedImage = true
                        formError = null
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Supprimer la photo", color = scheme.error.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                }
            }

            formError?.let { err ->
                Text(err, color = scheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    formError = null
                    scope.launch {
                        val finalUrl: String? = when {
                            pickedUri != null -> {
                                val uri = pickedUri!!
                                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                if (bytes == null) {
                                    formError = "Impossible de lire l’image."
                                    return@launch
                                }
                                if (bytes.size > MaxImageBytes) {
                                    formError = "Image trop volumineuse (max. 6 Mo)."
                                    return@launch
                                }
                                val uploadResult = viewModel.uploadPlatCoverImage(bytes, mime)
                                val url = uploadResult.getOrElse { err ->
                                    formError = formatImageUploadError(err)
                                    return@launch
                                }
                                url
                            }
                            userClearedImage -> null
                            remoteImageUrl.isNotBlank() -> remoteImageUrl
                            else -> null
                        }

                        val plat = Plat(
                            id = platId,
                            nom = nom,
                            description = description,
                            prix = prix.toDoubleOrNull() ?: 0.0,
                            image_url = finalUrl,
                            categorie_id = selectedCategoryId
                        )
                        viewModel.savePlat(plat) { success ->
                            if (success) onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                enabled = !isLoading && nom.isNotBlank() && prix.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = scheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (platId == null) "Créer le plat" else "Enregistrer les modifications", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
