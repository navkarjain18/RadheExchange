package org.exchange.radhe.network

import kotlinx.serialization.Serializable
import org.exchange.radhe.AppConstants

/**
 * Represents a command message sent over the WebSocket connection.
 * Used for both handshaking and operational instructions.
 *
 * @property type The type of the command (e.g., "connect", "command").
 * @property role The role of the sender ("phone" or "desktop").
 * @property username The username associated with the command (optional).
 * @property payload The specific instruction payload (optional).
 */
@Serializable
data class Command(
    val type: String,
    val role: String? = null,
    val username: String? = null,
    val payload: Payload? = null
)

/**
 * Represents the data payload within a [Command].
 *
 * @property action The specific action to perform (e.g., "wicket", "boundary").
 */
@Serializable
data class Payload(
    val action: String? = null
)
