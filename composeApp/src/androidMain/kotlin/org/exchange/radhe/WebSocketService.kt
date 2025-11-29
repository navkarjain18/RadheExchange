
package org.exchange.radhe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
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

class WebSocketService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val wsClient = DI.wsClient

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "WebSocketChannel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radhe Exchange Controller")
            .setContentText("Connected to desktop app.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            wsClient.connect("ws://10.0.2.2:8080")
        }

        observeKeyEvents()

        return START_STICKY
    }

    private fun observeKeyEvents() {
        KeyEventBus.events.onEach { action ->
            val command = when (action) {
                KeyAction.VOLUME_UP -> "wicket_click"
                KeyAction.VOLUME_DOWN -> "boundary_click"
            }
            scope.launch {
                wsClient.send(command)
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
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        scope.launch {
            wsClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
