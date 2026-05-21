package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Login : SplashDestination()
    object Home : SplashDestination()
    object Admin : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Durée minimale d'affichage du splash pour une expérience fluide
            val minDisplayJob = launch { delay(1800L) }

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                minDisplayJob.join()
                _destination.value = SplashDestination.Login
                return@launch
            }

            val profile = try {
                authRepository.getUserProfile()
            } catch (e: Exception) {
                null
            }

            minDisplayJob.join()

            _destination.value = when {
                profile == null -> SplashDestination.Login
                profile.role == "admin" -> SplashDestination.Admin
                else -> SplashDestination.Home
            }
        }
    }
}
