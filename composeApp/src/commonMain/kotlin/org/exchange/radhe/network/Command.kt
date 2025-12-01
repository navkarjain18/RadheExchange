package org.exchange.radhe.network

import kotlinx.serialization.Serializable
import org.exchange.radhe.AppConstants

@Serializable
data class Command(
    val type: String,
    val role: String? = null,
    val username: String? = null,
    val payload: Payload? = null
)

@Serializable
data class Payload(
    val action: String? = null
)
