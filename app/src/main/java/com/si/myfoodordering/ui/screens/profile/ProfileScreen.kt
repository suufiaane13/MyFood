package com.si.myfoodordering.ui.screens.profile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.components.AuthLoadingOverlay
import com.si.myfoodordering.ui.components.ChangePasswordDialog
import com.si.myfoodordering.ui.viewmodel.AuthViewModel
import com.si.myfoodordering.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val themeViewModel: ThemeViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val darkTheme by themeViewModel.isDarkTheme.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.nom
            phone = it.telephone.orEmpty()
            address = it.adresse.orEmpty()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = scheme.background,
        topBar = {
            AppHeader(
                title = "Paramètres",
                compact = true,
                actions = {
                    if (!isEditing && userProfile != null) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Modifier", tint = scheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isEditing) {
                ProfileSectionTitle("Profil")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            color = scheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = scheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            userProfile?.nom ?: "Utilisateur",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = scheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                userProfile?.email ?: "",
                                color = scheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileSectionTitle("Coordonnées")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileStatRow(
                            icon = Icons.Outlined.Phone,
                            label = "Téléphone",
                            value = userProfile?.telephone?.takeIf { it.isNotBlank() } ?: "Non renseigné"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            thickness = 0.5.dp,
                            color = scheme.outlineVariant.copy(alpha = 0.7f)
                        )
                        ProfileStatRow(
                            icon = Icons.Outlined.LocationOn,
                            label = "Adresse",
                            value = userProfile?.adresse?.takeIf { it.isNotBlank() } ?: "Non renseignée",
                            multiline = true
                        )
                    }
                }
            } else {
                ProfileSectionTitle("Modifier le profil")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom complet") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                focusedLabelColor = scheme.primary,
                                cursorColor = scheme.primary,
                                unfocusedBorderColor = scheme.outline,
                                unfocusedLabelColor = scheme.onSurfaceVariant,
                                focusedTextColor = scheme.onSurface,
                                unfocusedTextColor = scheme.onSurface,
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                focusedLabelColor = scheme.primary,
                                cursorColor = scheme.primary,
                                unfocusedBorderColor = scheme.outline,
                                unfocusedLabelColor = scheme.onSurfaceVariant,
                                focusedTextColor = scheme.onSurface,
                                unfocusedTextColor = scheme.onSurface,
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Adresse de livraison") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                focusedLabelColor = scheme.primary,
                                cursorColor = scheme.primary,
                                unfocusedBorderColor = scheme.outline,
                                unfocusedLabelColor = scheme.onSurfaceVariant,
                                focusedTextColor = scheme.onSurface,
                                unfocusedTextColor = scheme.onSurface,
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Annuler")
                            }
                            Button(
                                onClick = {
                                    viewModel.updateProfile(name, phone, address)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.primary,
                                    contentColor = scheme.onPrimary,
                                )
                            ) {
                                Text("Enregistrer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionTitle("Affichage")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                "Mode sombre",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = scheme.onSurface
                            )
                            Text(
                                "Clair ou sombre",
                                fontSize = 12.sp,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { themeViewModel.setDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = scheme.primary,
                            checkedTrackColor = scheme.primary.copy(alpha = 0.45f),
                            uncheckedThumbColor = scheme.outline,
                            uncheckedTrackColor = scheme.surfaceVariant,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionTitle("À propos")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "MyFood",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = scheme.primary
                    )
                    Text(
                        "Soufiane HAJJI · Ilham EL-ALAOUI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionTitle("Compte")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column {
                    if (userProfile?.role == "admin") {
                        ProfileMenuItem(
                            Icons.Outlined.AdminPanelSettings,
                            "Espace restaurateur",
                            "Menu, commandes et gestion"
                        ) {
                            onNavigateToAdmin()
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = scheme.outlineVariant.copy(alpha = 0.8f)
                        )
                    }
                    ProfileMenuItem(
                        Icons.Outlined.Lock,
                        "Mot de passe",
                        "Modifier votre mot de passe"
                    ) {
                        showPasswordDialog = true
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = scheme.outlineVariant.copy(alpha = 0.8f)
                    )
                    ProfileMenuItem(
                        Icons.AutoMirrored.Outlined.Logout,
                        "Déconnexion",
                        "Quitter cette session",
                        color = scheme.error
                    ) {
                        viewModel.logout(onLogout)
                    }
                }
            }
        }
    }

    AuthLoadingOverlay(visible = isLoggingOut, message = "Déconnexion…")
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            authViewModel = viewModel,
            onDismiss = { showPasswordDialog = false }
        )
    }
}

@Composable
fun ProfileSectionTitle(text: String) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = scheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun ProfileStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    multiline: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = scheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = if (multiline) 4 else 1
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color? = null,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val titleColor = color ?: scheme.onSurface
    ListItem(
        headlineContent = {
            Text(title, fontWeight = FontWeight.SemiBold, color = titleColor, fontSize = 15.sp)
        },
        supportingContent = {
            Text(subtitle, fontSize = 12.sp, color = scheme.onSurfaceVariant)
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = color ?: scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        },
        modifier = Modifier.clickable { onClick() },
        trailingContent = {
            Icon(
                Icons.Outlined.ChevronRight,
                null,
                tint = scheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
