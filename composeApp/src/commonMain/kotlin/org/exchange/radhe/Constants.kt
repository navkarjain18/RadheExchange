package org.exchange.radhe

object AppConstants {
    // WebSocket
    const val WEBSOCKET_URL = "ws://43.204.218.161:8080"
    const val ROLE_PHONE = "phone"
    const val ROLE_DESKTOP = "desktop"
    const val BASE_RECONNECT_DELAY_MS = 1000L
    const val MAX_RECONNECT_DELAY_MS = 60000L

    // Commands
    const val COMMAND_TYPE_CONNECT = "connect"
    const val COMMAND_TYPE_COMMAND = "command"
    const val PAYLOAD_ACTION_WICKET = "wicket"
    const val PAYLOAD_ACTION_BOUNDARY = "boundary"
    const val COMMAND_USERNAME_ALL = "all"

    // User Login
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_USERNAME = "username"

    val DUMMY_USERS = mapOf(
        "Shiv001" to "Shiv@1234",
        "Radhe001" to "Radhe@1234",
        "Hari001" to "Hari@1234",
        "Sai001" to "Sai@1234",
    )
}
