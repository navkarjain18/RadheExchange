
package org.exchange.radhe.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import org.exchange.radhe.VolumeKeyAccessibilityService

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val accessibilityEnabled = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
    } catch (e: Settings.SettingNotFoundException) {
        0
    }

    if (accessibilityEnabled == 1) {
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            val stringColonSplitter = TextUtils.SimpleStringSplitter(':')
            stringColonSplitter.setString(settingValue)
            while (stringColonSplitter.hasNext()) {
                val accessibilityService = stringColonSplitter.next()
                if (accessibilityService.equals(
                        "${context.packageName}/${VolumeKeyAccessibilityService::class.java.name}",
                        ignoreCase = true
                    )
                ) {
                    return true
                }
            }
        }
    }
    return false
}
