
package org.exchange.radhe.platform

import com.russhwolf.settings.Settings

enum class Platform {
    Android, Desktop
}

expect fun createSettings(): Settings

expect fun getPlatform(): Platform
