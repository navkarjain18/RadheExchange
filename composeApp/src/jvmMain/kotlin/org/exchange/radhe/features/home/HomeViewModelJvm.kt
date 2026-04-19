package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.exchange.radhe.AppConstants
import org.exchange.radhe.data.LoginRepository
import org.exchange.radhe.di.DI
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.json
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min
import kotlin.math.pow

enum class CaptureType {
    WICKET, BOUNDARY
}

data class HomeUiState(
    val isWicketToggleOn: Boolean = false,
    val isBoundaryToggleOn: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Connecting,
    val lastReceivedCommand: String = "None",
    val error: String? = null,
    val username: String? = null,
    val isLoggedOut: Boolean = false,
    val wicketCoordinate: Point? = null,
    val boundaryCoordinate: Point? = null,
    val capturingType: CaptureType? = null,
    val captureCountdown: Int? = null,
    val captureDelaySeconds: Int = 3,
    val notification: String? = null
)

sealed interface ConnectionState {
    object Connecting : ConnectionState
    object Connected : ConnectionState
    data class Disconnected(val reason: String) : ConnectionState
}

class HomeViewModelJvm(private val loginRepository: LoginRepository = DI.loginRepository) : ScreenModel {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val wsClient = DI.wsClient
    private val robot = Robot()
    private var connectionJob: Job? = null
    private val clickMutex = Mutex()

    private fun showNotification(title: String, message: String) {
        _uiState.update { it.copy(notification = "$title: $message") }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notification = null) }
    }

    init {
        println("HomeViewModelJvm initializing...")
        _uiState.update { it.copy(username = loginRepository.getUsername()) }
        connectAndObserve()
    }

    private fun connectAndObserve() {
        connectionJob = screenModelScope.launch {
            val username = uiState.value.username ?: return@launch
            var attempt = 0
            while (isActive) { // Use isActive to make the loop cancellable
                try {
                    _uiState.update { it.copy(connectionState = ConnectionState.Connecting, error = null) }
                    println("Attempting to connect (attempt #${attempt + 1})...")
                    wsClient.connect(AppConstants.WEBSOCKET_URL, AppConstants.ROLE_DESKTOP, username)
                    _uiState.update { it.copy(connectionState = ConnectionState.Connected) }
                    println("Connection successful.")
                    attempt = 0 // Reset attempts on successful connection

                    wsClient.observeMessages()
                        .onEach { msg ->
                            println("Received message: '$msg'")
                            try {
                                val command = json.decodeFromString<Command>(msg)
                                _uiState.update { it.copy(lastReceivedCommand = command.payload?.action ?: "Unknown") }
                                handleCommand(command)
                            } catch (e: Exception) {
                                val errorMsg = "Error decoding command: ${e.message}"
                                println(errorMsg)
                                _uiState.update { it.copy(error = errorMsg) }
                            }
                        }
                        .catch { e ->
                            println("Error in WebSocket flow: ${e.message}")
                            if (e is CancellationException) throw e
                        }
                        .launchIn(screenModelScope)
                        .join() // Wait until the flow is complete (i.e., connection is lost)

                } catch (e: CancellationException) {
                    println("Connection job cancelled.")
                    break // Exit the loop when cancelled
                } catch (e: Exception) {
                    val errorMsg = "Connection failed: ${e.message}"
                    println(errorMsg)
                    _uiState.update { it.copy(error = errorMsg) }
                }

                // If we are here, the connection was lost or failed
                val delayMillis = calculateBackoff(attempt)
                _uiState.update { it.copy(connectionState = ConnectionState.Disconnected("Reconnecting in ${delayMillis / 1000}s...")) }
                println("Connection lost. Reconnecting in ${delayMillis / 1000} seconds...")
                delay(delayMillis)
                attempt++
            }
        }
    }

    private fun handleCommand(command: Command) {
        println("Handling command: '$command'")
        val action = command.payload?.action ?: return
        val isEnabled = when (action) {
            AppConstants.PAYLOAD_ACTION_WICKET -> uiState.value.isWicketToggleOn
            AppConstants.PAYLOAD_ACTION_BOUNDARY -> uiState.value.isBoundaryToggleOn
            else -> {
                println("Unknown command action: '$action'")
                false
            }
        }

        if (isEnabled) {
            println("Performing mouse click for action: '$action'")
            screenModelScope.launch(Dispatchers.IO) {
                if (clickMutex.tryLock()) {
                    try {
                        val success = performMouseClick(action)
                        if (success) {
                            showNotification("Event Triggered", "Action '$action' executed. 5-second cooldown active.")
                            delay(5000)
                        } else {
                            showNotification("Event Failed", "Action '$action' coordinate not set.")
                        }
                    } finally {
                        clickMutex.unlock()
                    }
                } else {
                    println("Cooldown active. Overlapping event for '$action' ignored.")
                }
            }
        } else {
            println("Toggle for action '$action' is OFF. Ignoring command.")
            showNotification("Event Ignored", "Action '$action' received but toggle is OFF.")
        }
    }

    fun onWicketToggleChanged(isToggled: Boolean) {
        _uiState.update {
            it.copy(
                isWicketToggleOn = isToggled
            )
        }
        println("Wicket toggle changed to: $isToggled")
    }

    fun onBoundaryToggleChanged(isToggled: Boolean) {
        _uiState.update {
            it.copy(
                isBoundaryToggleOn = isToggled
            )
        }
        println("Boundary toggle changed to: $isToggled")
    }

    fun logout() {
        connectionJob?.cancel()
        screenModelScope.launch {
            wsClient.disconnect()
            loginRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCaptureDelayChanged(delayStr: String) {
        val delaySecs = delayStr.toIntOrNull()
        if (delaySecs != null && delaySecs >= 0) {
            _uiState.update { it.copy(captureDelaySeconds = delaySecs) }
        } else if (delayStr.isEmpty()) {
            _uiState.update { it.copy(captureDelaySeconds = 0) }
        }
    }

    fun startCapturingCoordinate(type: CaptureType) {
        if (uiState.value.capturingType != null) return
        screenModelScope.launch {
            val initialDelay = uiState.value.captureDelaySeconds
            _uiState.update { it.copy(capturingType = type, captureCountdown = initialDelay) }
            for (i in initialDelay downTo 1) {
                _uiState.update { it.copy(captureCountdown = i) }
                delay(1000)
            }
            val pointerInfo = MouseInfo.getPointerInfo()
            if (pointerInfo != null) {
                val point = pointerInfo.location
                _uiState.update { 
                    when (type) {
                        CaptureType.WICKET -> it.copy(wicketCoordinate = point, capturingType = null, captureCountdown = null)
                        CaptureType.BOUNDARY -> it.copy(boundaryCoordinate = point, capturingType = null, captureCountdown = null)
                    }
                }
                println("Captured ${type.name} coordinate: ${pointerInfo.location}")
            } else {
                _uiState.update { it.copy(error = "Could not get mouse pointer info.", capturingType = null, captureCountdown = null) }
            }
        }
    }

    private fun performMouseClick(action: String): Boolean {
        try {
            val point = when (action) {
                AppConstants.PAYLOAD_ACTION_WICKET -> uiState.value.wicketCoordinate
                AppConstants.PAYLOAD_ACTION_BOUNDARY -> uiState.value.boundaryCoordinate
                else -> null
            }
            
            if (point == null) {
                val errorMsg = "Coordinate for $action must be set before clicking."
                println(errorMsg)
                _uiState.update { it.copy(error = errorMsg) }
                return false
            }

            robot.mouseMove(point.x, point.y)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            Thread.sleep(50)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            println("Clicked $action coordinate at [${point.x}, ${point.y}]")
            return true
            
            /* Previous 3-click legacy code
            val coords = uiState.value.coordinates
            if (coords.any { it == null }) return
            for ((index, pt) in coords.withIndex()) {
                if (pt == null) continue
                robot.mouseMove(pt.x, pt.y)
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                Thread.sleep(50)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                if (index == 0) Thread.sleep(uiState.value.delay1to2Ms)
                else if (index == 1) Thread.sleep(uiState.value.delay2to3Ms)
            }
            */
        } catch (e: Exception) {
            val errorMsg = "Error performing mouse click: ${e.message}"
            println(errorMsg)
            _uiState.update { it.copy(error = errorMsg) }
            return false
        }
    }

    private fun calculateBackoff(attempt: Int): Long {
        return min(AppConstants.MAX_RECONNECT_DELAY_MS, (AppConstants.BASE_RECONNECT_DELAY_MS * 2.0.pow(attempt.toDouble())).toLong())
    }

    override fun onDispose() {
        super.onDispose()
        println("Disposing ViewModel and disconnecting client.")
        connectionJob?.cancel()
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
