package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Conserve l’onglet admin (Accueil / Commandes / Menu / Profil) au-delà des écrans poussés sur la pile (ex. édition plat). */
@HiltViewModel
class AdminMainViewModel @Inject constructor() : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.update { index.coerceIn(0, 3) }
    }
}
