package org.exchange.radhe.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class LoginRepository(private val settings: Settings) {

    fun setLoggedIn(isLoggedIn: Boolean, username: String? = null) {
        settings[IS_LOGGED_IN] = isLoggedIn
        if (isLoggedIn && username != null) {
            settings[USERNAME] = username
        } else {
            settings.remove(USERNAME)
        }
    }

    fun isLoggedIn(): Boolean {
        return settings.getBoolean(IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return settings.getStringOrNull(USERNAME)
    }

    fun logout() {
        settings.remove(IS_LOGGED_IN)
        settings.remove(USERNAME)
    }

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val USERNAME = "username"
    }
}
