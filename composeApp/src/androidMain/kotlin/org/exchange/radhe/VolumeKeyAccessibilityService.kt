
package org.exchange.radhe

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus

class VolumeKeyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // This method is required but we are using onKeyEvent for this use case.
    }

    override fun onInterrupt() {
        // Handle interruptions
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    GlobalScope.launch { KeyEventBus.send(KeyAction.VOLUME_UP) }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    GlobalScope.launch { KeyEventBus.send(KeyAction.VOLUME_DOWN) }
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }
}
