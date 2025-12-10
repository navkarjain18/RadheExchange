package org.exchange.radhe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.exchange.radhe.di.ConnectionStatus
import org.exchange.radhe.di.DI
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus
import org.exchange.radhe.di.UplinkStateHolder
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.Payload
import kotlin.math.min
import kotlin.math.pow

/**
 * A persistent Foreground Service responsible for maintaining the WebSocket connection
 * to the desktop server.
 *
 * This service implementation prioritizes network reliability:
 * 1.  **Foreground Execution**: Runs as a foreground service to minimize system kill probability.
 * 2.  **Partial Wake Lock**: Holds a wake lock to ensure the CPU remains active for network I/O.
 * 3.  **Automatic Reconnection**: Implements exponential backoff for connection handling.
 */
class WebSocketService : Service() {

    // SupervisorJob for structured concurrency
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    // Dependencies
    private val wsClient = DI.wsClient

    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "WebSocketService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "WebSocketChannel"
        private const val WAKE_LOCK_TAG = "RadheExchange::WebSocketServiceWakeLock"
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Creating WebSocketService...")
        
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        
        // Acquire wake lock to prevent CPU sleep during critical network operations
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Starting WebSocketService command...")
        
        // Always observe local button events to forward them
        observeKeyEvents()
        
        serviceScope.launch {
             // Main loop: Connect and maintain socket connection
             connectAndObserve()
        }
        
        // START_STICKY ensures the system restarts the service if it's killed
        return START_STICKY
    }

    /**
     * Main loop that attempts to connect to the WebSocket server.
     * Implements exponential backoff retry logic.
     */
    private suspend fun connectAndObserve() {
        var attempt = 0
        while (true) {
            try {
                updateConnectionState(ConnectionStatus.Connecting, "Connecting to desktop app...")
                
                wsClient.connect(AppConstants.WEBSOCKET_URL, AppConstants.ROLE_PHONE)
                
                updateConnectionState(ConnectionStatus.Connected, "Connected to desktop app.")
                Log.i(TAG, "WebSocket Connection established.")
                attempt = 0 

                // Block here while listening to incoming messages
                wsClient.observeMessages()
                    .catch { e -> Log.e(TAG, "Error in message stream", e) }
                    .launchIn(serviceScope)
                    .join()

            } catch (e: Exception) {
                val errorMessage = "Connection Failure: ${e.message}"
                Log.e(TAG, errorMessage)
                UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Error(errorMessage))
            }

            // Connection lost or failed; wait before retrying
            val delayMillis = calculateBackoff(attempt)
            updateNotification("Disconnected. Retrying in ${delayMillis / 1000}s...")
            Log.d(TAG, "Reconnecting in ${delayMillis}ms (Attempt $attempt)")
            
            delay(delayMillis)
            attempt++
        }
    }

    /**
     * Observes the internal Event Bus for key presses (Volume Up/Down)
     * and converts them into network [Command]s.
     */
    private fun observeKeyEvents() {
        KeyEventBus.events.onEach { action ->
            Log.d(TAG, "Processing local key event: $action")
            
            val commandAction = when (action) {
                KeyAction.VOLUME_UP -> AppConstants.PAYLOAD_ACTION_BOUNDARY
                KeyAction.VOLUME_DOWN -> AppConstants.PAYLOAD_ACTION_WICKET
            }
            
            UplinkStateHolder.updateLastCommand(commandAction)
            
            val command = Command(
                type = AppConstants.COMMAND_TYPE_COMMAND, 
                username = AppConstants.COMMAND_USERNAME_ALL, 
                payload = Payload(action = commandAction)
            )

            serviceScope.launch {
                try {
                    wsClient.sendCommand(command)
                } catch (e: Exception) {
                     Log.w(TAG, "Failed to send command (Socket likely disconnected): ${e.message}")
                }
            }
        }.launchIn(serviceScope)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
        Log.d(TAG, "Partial WakeLock acquired.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WebSocket Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status of the connection to the desktop application"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateConnectionState(status: ConnectionStatus, notificationText: String) {
        UplinkStateHolder.updateConnectionStatus(status)
        updateNotification(notificationText)
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radhe Exchange Controller")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun calculateBackoff(attempt: Int): Long {
        return min(
            AppConstants.MAX_RECONNECT_DELAY_MS,
            (AppConstants.BASE_RECONNECT_DELAY_MS * 2.0.pow(attempt.toDouble())).toLong()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "WebSocketService destroying...")
        
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released.")
        }
        
        UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Disconnected)
        serviceScope.cancel() // Cleanly cancel all coroutines
        
        // Fire-and-forget disconnect in a separate scope to ensure it runs even as service dies
        CoroutineScope(Dispatchers.IO).launch {
            wsClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
