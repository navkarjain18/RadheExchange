
package org.exchange.radhe.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

object ContextProvider {
    lateinit var context: Context
}

actual fun createSettings(): Settings {
    return SharedPreferencesSettings(ContextProvider.context.getSharedPreferences("RadheExc", Context.MODE_PRIVATE))
}

actual fun getPlatform(): Platform = Platform.Android
