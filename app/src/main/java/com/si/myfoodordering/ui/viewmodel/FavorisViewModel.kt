package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.Plat
import com.si.myfoodordering.data.repository.FavoriRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavorisViewModel @Inject constructor(
    private val favoriRepository: FavoriRepository
) : ViewModel() {

    private val _favoris = MutableStateFlow<List<Plat>>(emptyList())
    val favoris: StateFlow<List<Plat>> = _favoris.asStateFlow()

    /** IDs des plats en favori — utilisé pour afficher l'icône cœur partout dans l'app. */
    private val _favoriIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriIds: StateFlow<Set<Int>> = _favoriIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavoris()
    }

    fun loadFavoris() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _favoriIds.value = favoriRepository.getFavoriIds()
                _favoris.value = favoriRepository.getFavoris()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Bascule le favori pour un plat.
     * Met à jour [favoriIds] de façon optimiste avant la réponse réseau.
     */
    fun toggleFavori(platId: Int) {
        viewModelScope.launch {
            val isFavori = platId in _favoriIds.value

            // Mise à jour optimiste de l'icône cœur
            _favoriIds.value = if (isFavori) {
                _favoriIds.value - platId
            } else {
                _favoriIds.value + platId
            }

            val newState = favoriRepository.toggleFavori(platId, isFavori)

            // Si l'opération réseau a échoué, on rétablit l'état
            if (newState != !isFavori) {
                _favoriIds.value = if (isFavori) {
                    _favoriIds.value + platId
                } else {
                    _favoriIds.value - platId
                }
                UiMessageBus.toast("Erreur lors de la mise à jour des favoris")
            } else {
                UiMessageBus.toast(if (newState) "Ajouté aux favoris" else "Retiré des favoris")
                // Rafraîchir la liste de la page Favoris si on vient de retirer
                if (!newState) {
                    _favoris.value = _favoris.value.filter { it.id != platId }
                } else {
                    loadFavoris()
                }
            }
        }
    }
}
