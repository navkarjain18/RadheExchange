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

class WsClient {

    private val client = HttpClient {
        install(WebSockets) {
            pingIntervalMillis = 20_000 // Keep the connection alive
        }
    }
    private var session: WebSocketSession? = null

    suspend fun connect(serverUrl: String, role: String, username: String) {
        // Disconnect safely before creating a new session
        disconnect()
        session = client.webSocketSession(serverUrl)
        val command = Command(type = "connect", role = role, username = username)
        sendCommand(command)
    }

    fun observeMessages(): Flow<String> {
        return session?.incoming?.consumeAsFlow()
            ?.filterIsInstance<Frame.Text>()
            ?.map { it.readText() } ?: emptyFlow()
    }

    suspend fun sendCommand(command: Command) {
        try {
            val commandJson = json.encodeToString(command)
            session?.send(Frame.Text(commandJson))
        } catch (e: Exception) {
            println("Failed to send command: ${e.message}")
            // Optionally re-throw or handle the error
        }
    }

    suspend fun disconnect() {
        try {
            session?.close()
        } catch (e: Exception) {
            // Ignore exceptions during close, as the session might already be closing
            println("Exception while closing session (might be expected): ${e.message}")
        } finally {
            session = null
        }
    }
}
