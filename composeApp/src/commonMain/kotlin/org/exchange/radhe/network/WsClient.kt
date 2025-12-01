
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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

class WsClient {

    private val client = HttpClient { install(WebSockets) }
    private var session: WebSocketSession? = null

    suspend fun connect(serverUrl: String, role: String, username: String) {
        session?.close()
        session = null
        session = client.webSocketSession(serverUrl)
        val command = Command(type = "connect", role = role, username = username)
        sendCommand(command)
    }

    fun observeMessages(): Flow<String> {
        return session?.incoming?.consumeAsFlow()
            ?.filterIsInstance<Frame.Text>()
            ?.map { it.readText() } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    suspend fun sendCommand(command: Command) {
        val commandJson = json.encodeToString(command)
        session?.send(Frame.Text(commandJson))
    }

    suspend fun disconnect() {
        session?.close()
        session = null
    }
}
