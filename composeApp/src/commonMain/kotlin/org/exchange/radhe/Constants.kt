package org.exchange.radhe

/**
 * Global application constants used across the project logic.
 * Contains configuration for WebSocket connections, Command protocols, and authentication keys.
 */
object AppConstants {

    // region WebSocket Configuration
    /** The primary WebSocket URL for the backend server. */
    const val WEBSOCKET_URL = "ws://43.204.218.161:8080"
    
    /** Role identifier for the mobile application client. */
    const val ROLE_PHONE = "phone"
    
    /** Role identifier for the desktop application client. */
    const val ROLE_DESKTOP = "desktop"
    
    /** Initial delay in milliseconds before attempting a reconnection. */
    const val BASE_RECONNECT_DELAY_MS = 1000L
    
    /** Maximum delay in milliseconds for reconnection backoff. */
    const val MAX_RECONNECT_DELAY_MS = 60000L
    // endregion

    // region Command Protocol
    /** Command type for initiating a connection handshake. */
    const val COMMAND_TYPE_CONNECT = "connect"
    
    /** Command type for standard operational commands. */
    const val COMMAND_TYPE_COMMAND = "command"
    
    /** Payload action representing a "Wicket" event (typically Volume Down). */
    const val PAYLOAD_ACTION_WICKET = "wicket"
    
    /** Payload action representing a "Boundary" event (typically Volume Up). */
    const val PAYLOAD_ACTION_BOUNDARY = "boundary"
    
    /** Target username to broadcast commands to all connected clients. */
    const val COMMAND_USERNAME_ALL = "all"
    // endregion

    // region Authentication & Storage
    /** Key used to persist login status in local storage. */
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    /** Key used to persist the current username in local storage. */
    const val KEY_USERNAME = "username"

    /**
     * Hardcoded dummy credentials for testing/demo purposes.
     * Maps Username -> Password.
     */
    val DUMMY_USERS = mapOf(
        "Shiv001" to "Shiv@1234",
        "Radhe001" to "Radhe@1234",
        "Hari001" to "Hari@1234",
        "Sai001" to "Sai@1234",
    )
    // endregion
}
