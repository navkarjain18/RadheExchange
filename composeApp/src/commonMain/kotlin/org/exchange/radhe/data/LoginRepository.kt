package org.exchange.radhe.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.exchange.radhe.AppConstants

class LoginRepository(private val settings: Settings) {

    fun setLoggedIn(isLoggedIn: Boolean, username: String? = null) {
        settings[AppConstants.KEY_IS_LOGGED_IN] = isLoggedIn
        if (isLoggedIn && username != null) {
            settings[AppConstants.KEY_USERNAME] = username
        } else {
            settings.remove(AppConstants.KEY_USERNAME)
        }
    }

    fun isLoggedIn(): Boolean {
        return settings.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return settings.getStringOrNull(AppConstants.KEY_USERNAME)
    }

    fun logout() {
        settings.remove(AppConstants.KEY_IS_LOGGED_IN)
        settings.remove(AppConstants.KEY_USERNAME)
    }
}
