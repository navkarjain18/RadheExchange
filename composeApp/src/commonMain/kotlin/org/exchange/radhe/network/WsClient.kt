package org.exchange.radhe.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

/**
 * Network Client responsible for managing the WebSocket connection lifecycle.
 * Handles connection establishment, message observation, and command transmission.
 */
class WsClient {

    private val client = HttpClient {
        install(WebSockets) {
            pingIntervalMillis = 20_000 // Keep the connection alive
        }
    }
    
    private var session: WebSocketSession? = null

    /**
     * Establishes a WebSocket connection to the specified server.
     * Initiates a "connect" handshake command immediately upon connection.
     *
     * @param serverUrl The URL of the WebSocket server.
     * @param role The role identifier for this client.
     * @param username Optional username for authentication/identification.
     */
    suspend fun connect(serverUrl: String, role: String, username: String? = null) {
        // Disconnect safely before creating a new session to avoid leaks
        disconnect()
        
        session = client.webSocketSession(serverUrl)
        
        // Handshake
        val command = Command(type = "connect", role = role, username = username)
        sendCommand(command)
    }

    /**
     * Returns a [Flow] of incoming text messages from the WebSocket.
     * 
     * @return A flow of string messages, or an empty flow if not connected.
     */
    fun observeMessages(): Flow<String> {
        return session?.incoming?.consumeAsFlow()
            ?.filterIsInstance<Frame.Text>()
            ?.map { it.readText() } ?: emptyFlow()
    }

    /**
     * Serializes and sends a [Command] object over the WebSocket.
     *
     * @param command The command to send.
     */
    suspend fun sendCommand(command: Command) {
        try {
            val commandJson = json.encodeToString(command)
            session?.send(Frame.Text(commandJson))
        } catch (e: Exception) {
            println("WsClient: Failed to send command: ${e.message}")
        }
    }

    /**
     * Closes the current WebSocket session and releases resources.
     */
    suspend fun disconnect() {
        try {
            session?.close()
        } catch (e: Exception) {
            // Ignore exceptions during close, as the session might already be closing/closed
            println("WsClient: Exception during disconnect (safe to ignore): ${e.message}")
        } finally {
            session = null
        }
    }
}
