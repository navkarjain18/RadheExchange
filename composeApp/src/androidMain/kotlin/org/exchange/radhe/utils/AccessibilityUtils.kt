package org.exchange.radhe.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import org.exchange.radhe.VolumeKeyAccessibilityService

/**
 * Checks if the [VolumeKeyAccessibilityService] is currently enabled by the user in System Settings.
 *
 * This involves parsing the secure settings string `ENABLED_ACCESSIBILITY_SERVICES`.
 *
 * @param context The application context.
 * @return `true` if the specific service for this app is enabled, `false` otherwise.
 */
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
