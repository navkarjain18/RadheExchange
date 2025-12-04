package org.exchange.radhe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
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
import org.exchange.radhe.AppConstants
import org.exchange.radhe.di.ConnectionStatus
import org.exchange.radhe.di.DI
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus
import org.exchange.radhe.di.UplinkStateHolder
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.Payload
import kotlin.math.min
import kotlin.math.pow

class WebSocketService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val wsClient = DI.wsClient

    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val TAG = "WebSocketService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "WebSocketChannel"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")
        scope.launch {
            connectAndObserve()
        }
        return START_STICKY
    }

    private suspend fun connectAndObserve() {
        var attempt = 0
        while (true) {
            try {
                UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Connecting)
                updateNotification("Connecting to desktop app...")
                wsClient.connect(AppConstants.WEBSOCKET_URL, AppConstants.ROLE_PHONE)
                UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Connected)
                updateNotification("Connected to desktop app.")
                Log.d(TAG, "Connection successful.")
                attempt = 0

                observeKeyEvents()

                wsClient.observeMessages().catch { e -> Log.e(TAG, "Error observing messages", e) }
                    .launchIn(scope).join()

            } catch (e: Exception) {
                val errorMessage = "Connection failed: ${e.message}"
                UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Error(errorMessage))
                Log.e(TAG, errorMessage)
            }

            val delayMillis = calculateBackoff(attempt)
            updateNotification("Connection failed. Retrying in ${delayMillis / 1000}s...")
            Log.d(TAG, "Reconnecting in ${delayMillis / 1000} seconds...")
            delay(delayMillis)
            attempt++
        }
    }

    private fun observeKeyEvents() {
        KeyEventBus.events.onEach { action ->
            Log.d(TAG, "Key event: $action")
            val commandAction = when (action) {
                KeyAction.VOLUME_UP -> AppConstants.PAYLOAD_ACTION_BOUNDARY
                KeyAction.VOLUME_DOWN -> AppConstants.PAYLOAD_ACTION_WICKET
            }
            UplinkStateHolder.updateLastCommand(commandAction)
            val command = Command(
                type = AppConstants.COMMAND_TYPE_COMMAND, username = AppConstants.COMMAND_USERNAME_ALL, payload = Payload(action = commandAction)
            )
            scope.launch {
                wsClient.sendCommand(command)
            }
        }.launchIn(scope)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WebSocket Connection",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radhe Exchange Controller").setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground).build()
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
        UplinkStateHolder.updateConnectionStatus(ConnectionStatus.Disconnected)
        Log.d(TAG, "Service destroyed")
        scope.cancel() // Cancel all coroutines
        scope.launch {
            wsClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
