
package org.exchange.radhe.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.exchange.radhe.RadheApp

actual fun createSettings(): Settings {
    return SharedPreferencesSettings(RadheApp.instance.getSharedPreferences("RadheExc", Context.MODE_PRIVATE))
}

actual fun getPlatform(): Platform = Platform.Android
