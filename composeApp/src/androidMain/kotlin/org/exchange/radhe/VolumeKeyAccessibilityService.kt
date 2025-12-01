package org.exchange.radhe

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus

class VolumeKeyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private const val TAG = "AccessService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
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
        if (!isServiceRunning(WebSocketService::class.java)) {
            Log.d(TAG, "WebSocketService not running. Starting it...")
            val intent = Intent(this, WebSocketService::class.java)
            startService(intent)
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    Log.d(TAG, "Volume Up detected.")
                    scope.launch { KeyEventBus.send(KeyAction.VOLUME_UP) }
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.d(TAG, "Volume Down detected.")
                    scope.launch { KeyEventBus.send(KeyAction.VOLUME_DOWN) }
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
