package com.si.myfoodordering.ui.screens.admin

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.MainActivity
import com.si.myfoodordering.R
import com.si.myfoodordering.push.NotificationChannels
import com.si.myfoodordering.ui.components.AuthLoadingOverlay
import com.si.myfoodordering.ui.components.ChangePasswordDialog
import com.si.myfoodordering.ui.screens.profile.ProfileMenuItem
import com.si.myfoodordering.ui.screens.profile.ProfileSectionTitle
import com.si.myfoodordering.ui.screens.profile.ProfileStatRow
import com.si.myfoodordering.ui.viewmodel.AuthViewModel
import com.si.myfoodordering.ui.viewmodel.ThemeViewModel

@Composable
fun AdminProfileTab(
    authViewModel: AuthViewModel = hiltViewModel(),
    isEditing: Boolean = false,
    onEditChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit,
    onOpenClientApp: () -> Unit
) {
    val context = LocalContext.current
    val themeViewModel: ThemeViewModel = hiltViewModel(context as ComponentActivity)
    val darkTheme by themeViewModel.isDarkTheme.collectAsState()
    val scheme = MaterialTheme.colorScheme

    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoggingOut by authViewModel.isLoggingOut.collectAsState()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var notifFeedback by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { authViewModel.loadUserProfile() }
    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.nom
            phone = it.telephone.orEmpty()
            address = it.adresse.orEmpty()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isEditing) {
                // — Profil —
                ProfileSectionTitle("Profil")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
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
                            userProfile?.nom ?: "Restaurateur",
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
                            Text(userProfile?.email ?: "", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // — Coordonnées —
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
                // — Mode édition —
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
                            label = { Text("Adresse") },
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
                                onClick = { onEditChange(false) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Annuler") }
                            Button(
                                onClick = {
                                    authViewModel.updateProfile(name, phone, address)
                                    onEditChange(false)
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.primary,
                                    contentColor = scheme.onPrimary
                                )
                            ) { Text("Enregistrer", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // — Affichage —
            ProfileSectionTitle("Affichage")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(26.dp))
                        Column {
                            Text("Mode sombre", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = scheme.onSurface)
                            Text("Clair ou sombre", fontSize = 12.sp, color = scheme.onSurfaceVariant)
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

            Spacer(modifier = Modifier.height(8.dp))

            // — Application & Notifications —
            ProfileSectionTitle("Application")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Storefront,
                        title = "Vue client",
                        subtitle = "Accéder au menu, panier et commandes"
                    ) { onOpenClientApp() }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = scheme.outlineVariant.copy(alpha = 0.7f)
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Tester les notifications",
                        subtitle = notifFeedback ?: "Vérifier le canal admin"
                    ) {
                        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true
                        if (!hasPermission) {
                            notifFeedback = "Permission refusée"
                            return@ProfileMenuItem
                        }
                        val pending = PendingIntent.getActivity(
                            context, 0,
                            Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        NotificationManagerCompat.from(context).notify(
                            9999,
                            NotificationCompat.Builder(context, NotificationChannels.ADMIN)
                                .setSmallIcon(R.drawable.logo)
                                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo))
                                .setContentTitle("MyFood — Test notification")
                                .setContentText("Les notifications admin fonctionnent !")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pending)
                                .setAutoCancel(true)
                                .build()
                        )
                        notifFeedback = "✓ Envoyée"
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // — À propos —
            ProfileSectionTitle("À propos")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("MyFood", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = scheme.primary)
                    Text("Soufiane HAJJI · Ilham EL-ALAOUI", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // — Compte —
            ProfileSectionTitle("Compte")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column {
                    ProfileMenuItem(Icons.Outlined.Lock, "Mot de passe", "Modifier votre mot de passe") {
                        showPasswordDialog = true
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = scheme.outlineVariant.copy(alpha = 0.8f)
                    )
                    ProfileMenuItem(
                        Icons.AutoMirrored.Outlined.Logout, "Déconnexion", "Quitter cette session",
                        color = scheme.error
                    ) { authViewModel.logout(onLogout) }
                }
            }
        }

        AuthLoadingOverlay(visible = isLoggingOut, message = "Déconnexion…")
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(authViewModel = authViewModel, onDismiss = { showPasswordDialog = false })
    }
}
