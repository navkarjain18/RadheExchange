package org.exchange.radhe.di

import com.russhwolf.settings.Settings
import org.exchange.radhe.data.LoginRepository
import org.exchange.radhe.network.WsClient
import org.exchange.radhe.platform.createSettings

/**
 * Service Locator / Dependency Injection container for the shared module.
 * Provides lazy-initialized singletons for core application components.
 */
object DI {
    
    /**
     * Local storage settings instance.
     * Uses platform-specific implementation (SharedPreferences on Android, NSUserDefaults on iOS).
     */
    private val settings: Settings by lazy {
        createSettings()
    }

    /**
     * Repository responsible for handling user authentication and session management.
     */
    val loginRepository: LoginRepository by lazy {
        LoginRepository(settings)
    }

    /**
     * WebSocket Client for handling persistent connections to the server.
     */
    val wsClient: WsClient by lazy {
        WsClient()
    }
}
