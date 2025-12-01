package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.exchange.radhe.di.DI

data class HomeUiState(
    val isLoggedOut: Boolean = false,
    val username: String? = null
)

class HomeViewModel : ScreenModel {
    private val loginRepository = DI.loginRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(username = loginRepository.getUsername()) }
    }

    fun getUsername(): String? {
        return loginRepository.getUsername()
    }

    fun logout() {
        loginRepository.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
