
package org.exchange.radhe.network

import kotlinx.serialization.json.Json

/**
 * Global shared [Json] configuration for the application.
 * Configured to ignore unknown keys to ensure forward compatibility with API changes.
 */
val json = Json { ignoreUnknownKeys = true }
