package org.exchange.radhe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform