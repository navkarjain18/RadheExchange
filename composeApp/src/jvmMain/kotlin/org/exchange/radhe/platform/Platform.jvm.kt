
package org.exchange.radhe.platform

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
    val preferences = Preferences.userRoot().node("RadheExc")
    return PreferencesSettings(preferences)
}

actual fun getPlatform(): Platform = Platform.Desktop
