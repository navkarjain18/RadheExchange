package org.exchange.radhe.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current status of the WebSocket connection.
 */
sealed class ConnectionStatus {
    /** The connection is completely disconnected. */
    object Disconnected : ConnectionStatus()
    
    /** A connection attempt is currently in progress. */
    object Connecting : ConnectionStatus()
    
    /** The connection is successfully established. */
    object Connected : ConnectionStatus()
    
    /**
     * The connection encountered an error.
     * @param message A descriptive error message.
     */
    data class Error(val message: String) : ConnectionStatus()
}

/**
 * Global state holder for the application's network uplink status.
 * Exposes observable flows for UI components to react to connection changes.
 */
object UplinkStateHolder {
    
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    /** Observable flow of the current WebSocket connection status. */
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _lastCommand = MutableStateFlow<String?>("None")
    /** Observable flow of the last command payload sending to the server. */
    val lastCommand = _lastCommand.asStateFlow()

    /**
     * Updates the global connection status.
     * @param status The new status to emit.
     */
    fun updateConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    /**
     * Updates the record of the last sentinel command.
     * @param command The command string identifier.
     */
    fun updateLastCommand(command: String) {
        _lastCommand.value = command
    }
}
