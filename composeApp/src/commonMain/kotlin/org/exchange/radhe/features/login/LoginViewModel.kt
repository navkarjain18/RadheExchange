
package org.exchange.radhe.features.login

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.exchange.radhe.di.DI

class LoginViewModel : ScreenModel {
    private val loginRepository = DI.loginRepository

    private val _username = MutableStateFlow("shiv001")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("Abcd1234")
    val password = _password.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onUsernameChange(username: String) {
        _username.value = username
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun login(): Boolean {
        return if (username.value == "shiv001" && password.value == "Abcd1234") {
            loginRepository.setLoggedIn(true)
            _errorMessage.value = null
            true
        } else {
            _errorMessage.value = "Invalid username or password"
            false
        }
    }
}
