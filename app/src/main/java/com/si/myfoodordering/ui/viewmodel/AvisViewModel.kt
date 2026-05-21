package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.Avis
import com.si.myfoodordering.data.repository.AvisRepository
import com.si.myfoodordering.ui.util.UiMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AvisViewModel @Inject constructor(
    private val avisRepository: AvisRepository
) : ViewModel() {

    private val _avis = MutableStateFlow<List<Avis>>(emptyList())
    val avis: StateFlow<List<Avis>> = _avis.asStateFlow()

    private val _myAvis = MutableStateFlow<Avis?>(null)
    val myAvis: StateFlow<Avis?> = _myAvis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Note moyenne calculée depuis la liste (0f si aucun avis). */
    val averageNote: Float
        get() {
            val list = _avis.value
            return if (list.isEmpty()) 0f else list.sumOf { it.note }.toFloat() / list.size
        }

    fun loadAvis(platId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _avis.value = avisRepository.getAvisForPlat(platId)
                _myAvis.value = avisRepository.getUserAvisForPlat(platId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitAvis(platId: Int, note: Int) {
        viewModelScope.launch {
            val success = avisRepository.upsertAvis(platId, note)
            if (success) {
                loadAvis(platId)
                UiMessageBus.toast("Note enregistrée")
            } else {
                UiMessageBus.toast("Impossible d'enregistrer la note")
            }
        }
    }

    fun deleteMyAvis(platId: Int) {
        viewModelScope.launch {
            val id = _myAvis.value?.id ?: return@launch
            val success = avisRepository.deleteAvis(id)
            if (success) {
                loadAvis(platId)
                UiMessageBus.toast("Note supprimée")
            } else {
                UiMessageBus.toast("Impossible de supprimer la note")
            }
        }
    }
}
