package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.CartItem
import com.si.myfoodordering.data.model.Order
import com.si.myfoodordering.data.repository.AuthRepository
import com.si.myfoodordering.data.repository.OrderRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _telephone = MutableStateFlow("")
    val telephone: StateFlow<String> = _telephone.asStateFlow()

    private val _adresse = MutableStateFlow("")
    val adresse: StateFlow<String> = _adresse.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _hasExistingDetails = MutableStateFlow(false)
    val hasExistingDetails: StateFlow<Boolean> = _hasExistingDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Après affichage Snackbar, évite de relancer le même message. */
    fun consumeErrorMessage() {
        _errorMessage.value = null
    }

    private var currentUserName = ""

    init {
        prefillUserInfo()
    }

    private fun prefillUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = authRepository.getUserProfile()
                if (profile != null) {
                    currentUserName = profile.nom
                    _telephone.value = profile.telephone ?: ""
                    _adresse.value = profile.adresse ?: ""
                    // On considère qu'il a des détails si les deux ne sont pas vides
                    _hasExistingDetails.value = !profile.telephone.isNullOrBlank() && !profile.adresse.isNullOrBlank()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTelephoneChange(value: String) { _telephone.value = value }
    fun onAdresseChange(value: String) { _adresse.value = value }
    fun setEditMode() { _hasExistingDetails.value = false }

    fun submitOrder(cartItems: List<CartItem>, total: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_telephone.value.isBlank() || _adresse.value.isBlank()) {
                _errorMessage.value = "Veuillez remplir tous les champs."
                return@launch
            }
            
            _isSubmitting.value = true
            _errorMessage.value = null
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _errorMessage.value = "Session expirée. Veuillez vous reconnecter."
                    return@launch
                }
                val userId = user.id
                
                // 1. Tenter de mettre à jour le profil (optionnel pour la commande)
                // Si le nom actuel est vide, on essaie de le récupérer avant
                if (currentUserName.isBlank()) {
                    val profile = authRepository.getUserProfile()
                    if (profile != null) currentUserName = profile.nom
                }
                
                authRepository.updateProfile(currentUserName, _telephone.value, _adresse.value)
                
                // 2. Création de la commande
                val order = Order(
                    user_id = userId,
                    total = total,
                    adresse = _adresse.value,
                    telephone = _telephone.value
                )
                
                val orderId = orderRepository.createOrder(order, cartItems)
                if (orderId != null) {
                    UiMessageBus.toast("Commande enregistrée")
                    onSuccess()
                } else {
                    _errorMessage.value = "Impossible de créer la commande. Vérifiez votre connexion."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Erreur : ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
