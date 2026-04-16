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

data class HomeUiState(
    val isWicketToggleOn: Boolean = false,
    val isBoundaryToggleOn: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Connecting,
    val lastReceivedCommand: String = "None",
    val error: String? = null,
    val username: String? = null,
    val isLoggedOut: Boolean = false,
    val coordinates: List<Point?> = listOf(null, null, null),
    val capturingIndex: Int? = null,
    val captureCountdown: Int? = null,
    val captureDelaySeconds: Int = 3,
    val delay1to2Ms: Long = 500L,
    val delay2to3Ms: Long = 500L
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
                performMouseClick()
            }
        } else {
            println("Toggle for action '$action' is OFF. Ignoring command.")
        }
    }

    fun onWicketToggleChanged(isToggled: Boolean) {
        _uiState.update {
            it.copy(
                isWicketToggleOn = isToggled,
                isBoundaryToggleOn = if (isToggled) false else it.isBoundaryToggleOn
            )
        }
        println("Wicket toggle changed to: $isToggled")
    }

    fun onBoundaryToggleChanged(isToggled: Boolean) {
        _uiState.update {
            it.copy(
                isBoundaryToggleOn = isToggled,
                isWicketToggleOn = if (isToggled) false else it.isWicketToggleOn
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

    fun onDelay1to2Changed(delayStr: String) {
        val delayMs = delayStr.toLongOrNull()
        if (delayMs != null && delayMs >= 0) {
            _uiState.update { it.copy(delay1to2Ms = delayMs) }
        } else if (delayStr.isEmpty()) {
            _uiState.update { it.copy(delay1to2Ms = 0L) }
        }
    }

    fun onDelay2to3Changed(delayStr: String) {
        val delayMs = delayStr.toLongOrNull()
        if (delayMs != null && delayMs >= 0) {
            _uiState.update { it.copy(delay2to3Ms = delayMs) }
        } else if (delayStr.isEmpty()) {
            _uiState.update { it.copy(delay2to3Ms = 0L) }
        }
    }

    fun startCapturingCoordinate(index: Int) {
        if (uiState.value.capturingIndex != null) return
        screenModelScope.launch {
            val initialDelay = uiState.value.captureDelaySeconds
            _uiState.update { it.copy(capturingIndex = index, captureCountdown = initialDelay) }
            for (i in initialDelay downTo 1) {
                _uiState.update { it.copy(captureCountdown = i) }
                delay(1000)
            }
            val pointerInfo = MouseInfo.getPointerInfo()
            if (pointerInfo != null) {
                val newCoords = uiState.value.coordinates.toMutableList()
                newCoords[index] = pointerInfo.location
                _uiState.update { it.copy(coordinates = newCoords, capturingIndex = null, captureCountdown = null) }
                println("Captured coordinate ${index + 1}: ${pointerInfo.location}")
            } else {
                _uiState.update { it.copy(error = "Could not get mouse pointer info.", capturingIndex = null, captureCountdown = null) }
            }
        }
    }

    private fun performMouseClick() {
        try {
            val coords = uiState.value.coordinates
            if (coords.any { it == null }) {
                val errorMsg = "All 3 coordinates must be set before clicking."
                println(errorMsg)
                _uiState.update { it.copy(error = errorMsg) }
                return
            }

            for ((index, point) in coords.withIndex()) {
                if (point == null) continue
                robot.mouseMove(point.x, point.y)
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                Thread.sleep(50)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                println("Clicked coordinate ${index + 1} at [${point.x}, ${point.y}]")
                if (index == 0) {
                    Thread.sleep(uiState.value.delay1to2Ms)
                } else if (index == 1) {
                    Thread.sleep(uiState.value.delay2to3Ms)
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Error performing mouse click: ${e.message}"
            println(errorMsg)
            _uiState.update { it.copy(error = errorMsg) }
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
