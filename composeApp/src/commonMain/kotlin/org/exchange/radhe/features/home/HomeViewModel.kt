
package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import org.exchange.radhe.di.DI

class HomeViewModel : ScreenModel {
    private val loginRepository = DI.loginRepository

    fun logout() {
        loginRepository.setLoggedIn(false)
    }
}
