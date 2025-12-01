
package org.exchange.radhe

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus

class VolumeKeyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "AccessService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used for this feature
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted.")
        job.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility Service destroyed.")
        job.cancel()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "onKeyEvent received: $event")
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    Log.d(TAG, "Volume Up detected. Sending event to bus.")
                    scope.launch { KeyEventBus.send(KeyAction.VOLUME_UP) }
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.d(TAG, "Volume Down detected. Sending event to bus.")
                    scope.launch { KeyEventBus.send(KeyAction.VOLUME_DOWN) }
                }
            }
        }
        return super.onKeyEvent(event)
    }
}
