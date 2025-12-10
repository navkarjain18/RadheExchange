package org.exchange.radhe

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.media.VolumeProviderCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus

/**
 * A hybrid [AccessibilityService] that ensures reliable volume key detection in all states:
 * - **Foreground/Background**: Handled via standard [onKeyEvent].
 * - **Screen Off/Lock Screen**: Handled via an internal [MediaSessionCompat].
 *
 * This service is "System Managed," meaning the Android System will attempt to restart it
 * automatically if the application process is killed, providing high persistence.
 */
class VolumeKeyAccessibilityService : AccessibilityService() {

    // SupervisorJob allows children to fail independently without cancelling the parent
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var audioManager: AudioManager

    companion object {
        private const val TAG = "VolumeAccessService"
    }

    /**
     * Called when the Accessibility Service is connected to the system.
     * This is the entry point for our initialization.
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Accessibility Service connected and ready.")
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Initialize the MediaSession to handle "Screen Off" events
        setupMediaSession()
        
        // Ensure our networking service is running to propagate events
        startWebSocketService()
    }

    /**
     * Sets up a [MediaSessionCompat] with a [VolumeProviderCompat].
     *
     * This is the critical workaround for Android's limitation where Accessibility Services
     * stop receiving key events when the screen is off. By pretending to be an active
     * media session, we continue to receive volume updates.
     */
    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setPlaybackToRemote(object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, 100, 50) {
                override fun onAdjustVolume(direction: Int) {
                    // Map volume direction to our domain actions
                    when (direction) {
                        1 -> handleVolumePress(KeyAction.VOLUME_UP)
                        -1 -> handleVolumePress(KeyAction.VOLUME_DOWN)
                    }
                }
            })
            
            // Set state to Playing. This signals the system to prioritize this session/provider
            // for media button events (including volume) even when the screen is off.
            val state = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                .build()
            setPlaybackState(state)
            
            isActive = true
        }
        Log.d(TAG, "MediaSession created and active for background detection.")
    }
    
    /**
     * Processes a detected volume key action.
     *
     * @param action The specific [KeyAction] (Volume Up/Down) detected.
     */
    private fun handleVolumePress(action: KeyAction) {
        Log.d(TAG, "Event Detected: $action")
        
        // Broadcast event to the application bus
        serviceScope.launch { 
            KeyEventBus.send(action) 
        }
        
        // Manually adjust the system stream volume to maintain expected user behavior.
        // We act as a "pass-through" interceptor.
        val direction = if (action == KeyAction.VOLUME_UP) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * Intercepts key events when the screen is ON and the user is interacting with the device.
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    handleVolumePress(KeyAction.VOLUME_UP)
                    return true // Return true to indicate we consumed the event
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    handleVolumePress(KeyAction.VOLUME_DOWN)
                    return true // Return true to indicate we consumed the event
                }
            }
        }
        // Delegate non-volume events back to the system
        return super.onKeyEvent(event)
    }

    /**
     * Starts the [WebSocketService] as a Foreground Service to handle networking
     * reliable properties.
     */
    private fun startWebSocketService() {
        val intent = Intent(this, WebSocketService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: We only care about KeyEvents, not UI interaction events.
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service interrupted by system.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Accessibility Service destroying. Releasing resources.")
        mediaSession?.release()
        mediaSession = null
        serviceJob.cancel() // Cancel all coroutines started by this service
    }
}
