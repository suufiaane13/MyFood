package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.UserProfile
import com.si.myfoodordering.data.repository.AuthRepository
import com.si.myfoodordering.data.repository.PushTokenRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pushTokenRepository: PushTokenRepository,
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _userProfile.value = authRepository.getUserProfile()
                if (_userProfile.value != null) {
                    pushTokenRepository.registerCurrentToken()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun login(emailText: String, passwordText: String, onSuccess: (isAdmin: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val signedIn = authRepository.signIn(emailText, passwordText)
                if (!signedIn) {
                    _error.value = "Email ou mot de passe incorrect."
                    return@launch
                }

                // Attendre que le profil soit chargé (plusieurs tentatives car le trigger peut être lent)
                var profile: UserProfile? = null
                var retry = 0
                while (profile == null && retry < 5) {
                    profile = authRepository.getUserProfile()
                    if (profile == null) {
                        delay(1000)
                        retry++
                    }
                }

                if (profile != null) {
                    _userProfile.value = profile
                    val name = profile.nom.trim()
                    UiMessageBus.toast(
                        if (name.isNotEmpty()) "Bienvenue, $name !"
                        else "Bienvenue !"
                    )
                    pushTokenRepository.registerCurrentToken()
                    onSuccess(profile.role == "admin")
                } else {
                    _error.value = "Connexion réussie mais profil introuvable. Veuillez réessayer."
                }
            } catch (e: Exception) {
                _error.value = "Erreur : ${e.message ?: "Identifiants invalides"}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(emailText: String, passwordText: String, name: String, onSuccess: (isAdmin: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                authRepository.signUp(emailText, passwordText, name)?.let { msg ->
                    _error.value = msg
                    return@launch
                }

                // Laisser le temps au trigger SQL `handle_new_user` d'insérer la ligne `profiles`
                delay(1500)
                
                // Vérifier si on peut récupérer le profil (trigger SQL + session valide)
                var profile: UserProfile? = null
                var retryCount = 0
                while (retryCount < 5 && profile == null) {
                    profile = authRepository.getUserProfile()
                    if (profile == null) {
                        delay(1000)
                        retryCount++
                    }
                }
                if (profile == null) {
                    authRepository.insertProfileIfMissing(name, emailText)
                    retryCount = 0
                    while (retryCount < 4 && profile == null) {
                        profile = authRepository.getUserProfile()
                        if (profile == null) {
                            delay(500)
                            retryCount++
                        }
                    }
                }

                if (profile != null) {
                    _userProfile.value = profile
                    pushTokenRepository.registerCurrentToken()
                    onSuccess(profile.role == "admin")
                } else {
                    _error.value =
                        "Compte créé, mais le profil est introuvable. Vérifie dans Supabase : trigger sur auth.users, table profiles, et politiques RLS. Tu peux aussi te connecter depuis l'écran login."
                }
            } catch (e: Exception) {
                // Ici on affiche l'erreur RÉELLE de Supabase
                val errorMsg = e.message ?: ""
                _error.value = when {
                    errorMsg.contains("User already registered", ignoreCase = true) -> "Cet email est déjà utilisé."
                    errorMsg.contains("Password should be", ignoreCase = true) -> "Le mot de passe est trop court."
                    errorMsg.contains("valid email", ignoreCase = true) -> "L'adresse email n'est pas valide."
                    else -> "Erreur d'inscription : ${e.localizedMessage}"
                }
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onResult: (errorMessage: String?) -> Unit
    ) {
        when {
            currentPassword.isBlank() || newPassword.isBlank() -> {
                onResult("Remplis tous les champs.")
                return
            }
            newPassword != confirmPassword -> {
                onResult("Les nouveaux mots de passe ne correspondent pas.")
                return
            }
            newPassword.length < 6 -> {
                onResult("Le nouveau mot de passe doit contenir au moins 6 caractères.")
                return
            }
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val err = authRepository.changePassword(currentPassword, newPassword)
                if (err == null) {
                    UiMessageBus.toast("Mot de passe modifié")
                }
                onResult(err)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (authRepository.updateProfile(name, phone, address)) {
                    loadUserProfile()
                    UiMessageBus.toast("Profil enregistré")
                } else {
                    UiMessageBus.toast("Impossible d’enregistrer le profil")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAuthError() {
        _error.value = null
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                pushTokenRepository.revokeLocalTokens()
                authRepository.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _userProfile.value = null
                try {
                    onComplete()
                } finally {
                    _isLoggingOut.value = false
                }
            }
        }
    }
}
