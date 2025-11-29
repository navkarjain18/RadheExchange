
package org.exchange.radhe.di

import com.russhwolf.settings.Settings
import org.exchange.radhe.data.LoginRepository
import org.exchange.radhe.platform.createSettings

object DI {
    private val settings: Settings by lazy {
        createSettings()
    }

    val loginRepository: LoginRepository by lazy {
        LoginRepository(settings)
    }
}
