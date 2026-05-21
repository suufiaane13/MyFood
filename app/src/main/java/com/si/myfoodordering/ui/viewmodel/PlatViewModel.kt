package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.Category
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.data.repository.PlatRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlatViewModel @Inject constructor(
    private val platRepository: PlatRepository
) : ViewModel() {

    private val _plats = MutableStateFlow<List<Plat>>(emptyList())
    val plats: StateFlow<List<Plat>> = _plats.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null
            try {
                val (plats, categories) = platRepository.loadMenu()
                _plats.value = plats
                _categories.value = categories
            } catch (e: Exception) {
                e.printStackTrace()
                _loadError.value = "Impossible de charger le menu. Vérifie ta connexion."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun savePlat(plat: Plat, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = if (plat.id == null) {
                platRepository.addPlat(plat)
            } else {
                platRepository.updatePlat(plat)
            }
            if (success) {
                loadData()
                UiMessageBus.toast(if (plat.id == null) "Plat ajouté" else "Plat modifié")
            } else {
                UiMessageBus.toast("Impossible d’enregistrer le plat")
            }
            onComplete(success)
            _isLoading.value = false
        }
    }

    fun deletePlat(platId: Int) {
        viewModelScope.launch {
            if (platRepository.deletePlat(platId)) {
                loadData()
                UiMessageBus.toast("Plat supprimé")
            } else {
                UiMessageBus.toast("Impossible de supprimer le plat")
            }
        }
    }

    suspend fun uploadPlatCoverImage(bytes: ByteArray, mimeType: String): Result<String> =
        platRepository.uploadPlatCoverImage(bytes, mimeType)

    fun saveCategory(category: Category, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = if (category.id == null) {
                platRepository.addCategory(category)
            } else {
                platRepository.updateCategory(category)
            }
            if (success) {
                loadData()
                UiMessageBus.toast(if (category.id == null) "Catégorie ajoutée" else "Catégorie modifiée")
            } else {
                UiMessageBus.toast("Impossible d’enregistrer la catégorie")
            }
            onComplete(success)
            _isLoading.value = false
        }
    }

    fun deleteCategory(categoryId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = platRepository.deleteCategory(categoryId)
            if (success) {
                loadData()
                UiMessageBus.toast("Catégorie supprimée")
            } else {
                UiMessageBus.toast("Impossible de supprimer la catégorie")
            }
            onComplete(success)
            _isLoading.value = false
        }
    }
}
