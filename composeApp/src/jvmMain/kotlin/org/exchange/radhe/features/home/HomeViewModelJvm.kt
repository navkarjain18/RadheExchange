package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.json
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.InputEvent

class HomeViewModelJvm : ScreenModel {

    private val wsClient = DI.wsClient
    private val robot = Robot()

    private val _wicketToggle = MutableStateFlow(false)
    val wicketToggle = _wicketToggle.asStateFlow()

    private val _boundaryToggle = MutableStateFlow(false)
    val boundaryToggle = _boundaryToggle.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Connecting...")
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _lastCommand = MutableStateFlow("None")
    val lastCommand = _lastCommand.asStateFlow()

    init {
        println("HomeViewModelJvm initializing...")
        connectAndObserve()
    }

    private fun connectAndObserve() {
        screenModelScope.launch {
            try {
                _connectionStatus.value = "Connecting..."
                println("Attempting to connect to ws://10.81.2.11:8080")
                wsClient.connect("ws://10.81.2.11:8080", "desktop", "navkar")
                _connectionStatus.value = "Connected!"
                println("Connection successful.")
                wsClient.observeMessages()
                    .onEach { msg ->
                        println("Received command: '$msg'")
                        try {
                            val command = json.decodeFromString<Command>(msg)
                            _lastCommand.value = command.payload?.action ?: "Unknown"
                            handleCommand(command)
                        } catch (e: Exception) {
                            println("Error decoding or handling command: ${e.message}")
                        }
                    }
                    .onCompletion { // This will be called on error or completion
                        _connectionStatus.value = "Disconnected. Reconnecting..."
                        println("Connection lost. Reconnecting...")
                        delay(5000) // wait 5 seconds
                        connectAndObserve() // Try to reconnect
                    }
                    .launchIn(screenModelScope)
            } catch (e: Exception) {
                _connectionStatus.value = "Connection failed. Retrying..."
                println("Connection failed: ${e.message}. Retrying...")
                delay(5000) // wait 5 seconds
                connectAndObserve() // Try to reconnect
            }
        }
    }

    private fun handleCommand(command: Command) {
        println("Handling command: '$command'")
        when (command.payload?.action) {
            "wicket" -> {
                if (_wicketToggle.value) {
                    println("Wicket toggle is ON. Performing mouse click.")
                    // Launch click in a background thread
                    screenModelScope.launch(Dispatchers.IO) {
                        performMouseClick()
                    }
                } else {
                    println("Wicket toggle is OFF. Ignoring command.")
                }
            }
            "boundary" -> {
                if (_boundaryToggle.value) {
                    println("Boundary toggle is ON. Performing mouse click.")
                    // Launch click in a background thread
                    screenModelScope.launch(Dispatchers.IO) {
                        performMouseClick()
                    }
                } else {
                    println("Boundary toggle is OFF. Ignoring command.")
                }
            }
            else -> println("Unknown command received: '${command.payload?.action}'")
        }
    }

    fun onWicketToggleChanged(isToggled: Boolean) {
        _wicketToggle.update { isToggled }
        if (isToggled) {
            _boundaryToggle.update { false }
        }
        println("Wicket toggle changed to: $isToggled")
    }

    fun onBoundaryToggleChanged(isToggled: Boolean) {
        _boundaryToggle.update { isToggled }
        if (isToggled) {
            _wicketToggle.update { false }
        }
        println("Boundary toggle changed to: $isToggled")
    }

    private fun performMouseClick() {
        try {
            // Get current mouse location
            val currentLocation = MouseInfo.getPointerInfo().location
            robot.mouseMove(currentLocation.x, currentLocation.y)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            Thread.sleep(50) // Use Thread.sleep in a non-coroutine context
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        } catch (e: Exception) {
            println("Error performing mouse click: ${e.message}")
        }
    }

    override fun onDispose() {
        super.onDispose()
        println("Disposing ViewModel and disconnecting client.")
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
