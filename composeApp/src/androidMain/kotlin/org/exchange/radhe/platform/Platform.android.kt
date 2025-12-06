
package org.exchange.radhe.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.exchange.radhe.iMusicApp

actual fun createSettings(): Settings {
    return SharedPreferencesSettings(iMusicApp.instance.getSharedPreferences("RadheExc", Context.MODE_PRIVATE))
}

actual fun getPlatform(): Platform = Platform.Android
