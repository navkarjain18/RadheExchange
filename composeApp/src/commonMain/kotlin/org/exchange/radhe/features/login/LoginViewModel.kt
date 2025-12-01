package org.exchange.radhe.features.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.exchange.radhe.data.LoginRepository
import org.exchange.radhe.di.DI

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(private val loginRepository: LoginRepository = DI.loginRepository) :
    ScreenModel {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // 5 dummy users
    private val dummyUsers = mapOf(
        "navkar" to "pass1",
        "shiv" to "pass2",
        "rohan" to "pass3",
        "rahul" to "pass4",
        "riya" to "pass5"
    )

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        screenModelScope.launch {
            val usernameInput = _uiState.value.username
            val passwordInput = _uiState.value.password

            // Find user entry in a case-insensitive way to prevent crashes
            val userEntry = dummyUsers.entries.find { it.key.equals(usernameInput, ignoreCase = true) }

            if (userEntry != null && userEntry.value == passwordInput) {
                // Use the correct-cased username from the found entry
                loginRepository.setLoggedIn(true, userEntry.key)
                _uiState.update { it.copy(isLoggedIn = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(errorMessage = "Invalid username or password") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
