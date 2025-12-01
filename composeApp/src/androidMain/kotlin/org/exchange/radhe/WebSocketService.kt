
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.Payload

class WebSocketService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val wsClient = DI.wsClient
    private val loginRepository = DI.loginRepository

    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val TAG = "WebSocketService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "WebSocketChannel"
        const val EXTRA_USERNAME = "username"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radhe Exchange Controller")
            .setContentText("Connecting to desktop app...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            try {
                // Use 10.0.2.2 for Android emulator to connect to host's localhost.
                // For a physical device, use the host's network IP address.
                val username = intent?.getStringExtra(EXTRA_USERNAME) ?: "navkar"
                wsClient.connect("ws://10.81.2.11:8080", "phone", username)
                Log.d(TAG, "Connection successful. Starting key event observer.")
                // If connection is successful, update notification
                val successNotification = NotificationCompat.Builder(this@WebSocketService, CHANNEL_ID)
                    .setContentTitle("Radhe Exchange Controller")
                    .setContentText("Connected to desktop app.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()
                notificationManager.notify(NOTIFICATION_ID, successNotification)
                observeKeyEvents(username)
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                // On failure, update notification and stop the service
                val failureNotification = NotificationCompat.Builder(this@WebSocketService, CHANNEL_ID)
                    .setContentTitle("Radhe Exchange Controller")
                    .setContentText("Connection failed. Please check server.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()
                notificationManager.notify(NOTIFICATION_ID, failureNotification)
                stopSelf() // Stop the service cleanly
            }
        }

        return START_STICKY
    }

    private fun observeKeyEvents(username: String) {
        Log.d(TAG, "Observing key events...")
        KeyEventBus.events.onEach { action ->
            Log.d(TAG, "Key event received from bus: $action")
            val commandAction = when (action) {
                KeyAction.VOLUME_UP -> "wicket"
                KeyAction.VOLUME_DOWN -> "boundary"
            }
            val command = Command(
                type = "command",
                username = username,
                payload = Payload(action = commandAction)
            )
            scope.launch {
                Log.d(TAG, "Sending command to server: $command")
                wsClient.sendCommand(command)
            }
        }.launchIn(scope)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "WebSocket Connection",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d(TAG, "Service destroyed")
        scope.launch {
            wsClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
