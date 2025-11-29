
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

class WsClient {

    private val client = HttpClient { install(WebSockets) }
    private var session: WebSocketSession? = null

    suspend fun connect(serverUrl: String) {
        session = client.webSocketSession(serverUrl)
    }

    fun observeMessages(): Flow<String> {
        return session?.incoming?.consumeAsFlow()
            ?.filterIsInstance<Frame.Text>()
            ?.map { it.readText() } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    suspend fun send(message: String) {
        session?.send(Frame.Text(message))
    }

    suspend fun disconnect() {
        session?.close()
        session = null
        client.close()
    }
}
