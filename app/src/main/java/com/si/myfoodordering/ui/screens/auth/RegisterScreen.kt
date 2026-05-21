package com.si.myfoodordering.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.si.myfoodordering.R
import com.si.myfoodordering.ui.theme.LocalAppDarkTheme
import com.si.myfoodordering.ui.theme.PrimaryOrange
import com.si.myfoodordering.ui.theme.SecondaryOrange
import com.si.myfoodordering.ui.viewmodel.AuthViewModel

private fun isValidEmail(value: String): Boolean {
    if (value.isBlank()) return false
    return value.contains("@") && value.contains(".") && value.length > 4
}

@Composable
private fun RegisterStepIndicator(currentStep: Int) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(2) { index ->
            val stepNum = index + 1
            val active = currentStep == stepNum
            Surface(
                modifier = Modifier.size(if (active) 34.dp else 30.dp),
                shape = CircleShape,
                color = if (active) scheme.primary.copy(alpha = 0.15f) else scheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stepNum.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (active) scheme.primary else scheme.onSurfaceVariant
                    )
                }
            }
            if (index == 0) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .background(
                            if (currentStep >= 2) scheme.primary.copy(alpha = 0.5f) else scheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (isAdmin: Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val displayError = localError ?: error

    LaunchedEffect(step) {
        if (step == 1) {
            localError = null
            viewModel.clearAuthError()
        }
    }

    val scheme = MaterialTheme.colorScheme
    val darkUi = LocalAppDarkTheme.current
    val gradientColors = if (darkUi) {
        listOf(Color(0xFF5C2800), Color(0xFF351704))
    } else {
        listOf(PrimaryOrange, SecondaryOrange)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outline,
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        cursorColor = scheme.primary,
        focusedTextColor = scheme.onSurface,
        unfocusedTextColor = scheme.onSurface,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.size(80.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "MyFood Logo",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Text(
                text = "Rejoignez-nous",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = scheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Créer un compte",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterStepIndicator(currentStep = step)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (step == 1) "Étape 1 — Identité" else "Étape 2 — Sécurité",
                        fontSize = 13.sp,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Crossfade(
                        targetState = step,
                        animationSpec = tween(220),
                        label = "registerStep"
                    ) { s ->
                        when (s) {
                            1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nom complet") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Person, contentDescription = null, tint = scheme.primary)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = fieldColors
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Email, contentDescription = null, tint = scheme.primary)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = fieldColors
                                )
                            }
                            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Mot de passe") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = scheme.primary)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    colors = fieldColors
                                )
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirmer le mot de passe") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = scheme.primary)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    colors = fieldColors
                                )
                            }
                        }
                    }

                    if (displayError != null) {
                        Text(
                            text = displayError,
                            color = scheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (step == 1) {
                        Button(
                            onClick = {
                                localError = null
                                when {
                                    name.isBlank() -> localError = "Indique ton nom complet."
                                    !isValidEmail(email.trim()) -> localError = "Adresse email invalide."
                                    else -> step = 2
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.primary,
                                contentColor = scheme.onPrimary,
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Continuer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    localError = null
                                    viewModel.clearAuthError()
                                    step = 1
                                },
                                modifier = Modifier.weight(0.35f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLoading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary),
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = scheme.primary)
                            }
                            Button(
                                onClick = {
                                    localError = null
                                    when {
                                        password.length < 6 ->
                                            localError = "Mot de passe : au moins 6 caractères."
                                        password != confirmPassword ->
                                            localError = "Les mots de passe ne correspondent pas."
                                        else ->
                                            viewModel.register(email.trim(), password, name.trim()) { isAdmin ->
                                                onRegisterSuccess(isAdmin)
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.primary,
                                    contentColor = scheme.onPrimary,
                                ),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = scheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("S'inscrire", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                        Text("Déjà un compte ? Connectez-vous", color = scheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
