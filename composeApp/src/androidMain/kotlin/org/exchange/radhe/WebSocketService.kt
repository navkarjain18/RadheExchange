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
import org.exchange.radhe.di.DI
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus
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
        const val EXTRA_USERNAME = "username"
        private const val BASE_RECONNECT_DELAY_MS = 1000L
        private const val MAX_RECONNECT_DELAY_MS = 60000L
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")
        val username = intent?.getStringExtra(EXTRA_USERNAME) ?: "navkar" // Fallback to default

        scope.launch {
            connectAndObserve(username)
        }

        return START_STICKY
    }

    private suspend fun connectAndObserve(username: String) {
        var attempt = 0
        while (true) {
            try {
                updateNotification("Connecting to desktop app...")
                wsClient.connect("ws://10.81.2.11:8080", "phone", username)
                updateNotification("Connected to desktop app.")
                Log.d(TAG, "Connection successful.")
                attempt = 0

                observeKeyEvents(username)

                wsClient.observeMessages().catch { e -> Log.e(TAG, "Error observing messages", e) }
                    .launchIn(scope).join()

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
            }

            val delayMillis = calculateBackoff(attempt)
            updateNotification("Connection failed. Retrying in ${delayMillis / 1000}s...")
            Log.d(TAG, "Reconnecting in ${delayMillis / 1000} seconds...")
            delay(delayMillis)
            attempt++
        }
    }

    private fun observeKeyEvents(username: String) {
        KeyEventBus.events.onEach { action ->
            Log.d(TAG, "Key event: $action")
            val commandAction = when (action) {
                KeyAction.VOLUME_UP -> "wicket"
                KeyAction.VOLUME_DOWN -> "boundary"
            }
            val command = Command(
                type = "command", username = username, payload = Payload(action = commandAction)
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
            MAX_RECONNECT_DELAY_MS,
            (BASE_RECONNECT_DELAY_MS * 2.0.pow(attempt.toDouble())).toLong()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        scope.cancel() // Cancel all coroutines
        scope.launch {
            wsClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
