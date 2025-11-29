
package org.exchange.radhe.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class LoginRepository(private val settings: Settings) {

    fun setLoggedIn(isLoggedIn: Boolean) {
        settings[IS_LOGGED_IN] = isLoggedIn
    }

    fun isLoggedIn(): Boolean {
        return settings[IS_LOGGED_IN, false]
    }

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
    }
}
