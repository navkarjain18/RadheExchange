package org.exchange.radhe

/**
 * Represents the underlying platform (Android, iOS, Desktop) running the application.
 */
interface Platform {
    /** The human-readable name of the platform (e.g., "Android 33"). */
    val name: String
}

/**
 * Retrieves the current [Platform] implementation.
 * Expected to be implemented in the platform-specific source sets.
 */
expect fun getPlatform(): Platform