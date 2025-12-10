package org.exchange.radhe

import android.os.Build

/**
 * Android implementation of the [Platform] interface.
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/**
 * Provides the [AndroidPlatform] instance.
 */
actual fun getPlatform(): Platform = AndroidPlatform()