
package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI
import org.exchange.radhe.network.Command
import org.exchange.radhe.network.json
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
        screenModelScope.launch {
            try {
                println("Attempting to connect to ws://10.81.2.11:8080")
                wsClient.connect("ws://10.81.2.11:8080", "desktop", "navkar")
                _connectionStatus.value = "Connected!"
                println("Connection successful.")
                wsClient.observeMessages()
                    .onEach { msg ->
                        println("Received command: '$msg'")
                        val command = json.decodeFromString<Command>(msg)
                        _lastCommand.value = command.payload?.action ?: "Unknown"
                        handleCommand(command)
                    }
                    .launchIn(screenModelScope)
            } catch (e: Exception) {
                _connectionStatus.value = "Connection failed"
                println("Connection failed: ${e.message}")
            }
        }
    }

    private fun handleCommand(command: Command) {
        println("Handling command: '$command'")
        when (command.payload?.action) {
            "wicket" -> {
                if (_wicketToggle.value) {
                    println("Wicket toggle is ON. Performing mouse click.")
                    performMouseClick()
                } else {
                    println("Wicket toggle is OFF. Ignoring command.")
                }
            }
            "boundary" -> {
                if (_boundaryToggle.value) {
                    println("Boundary toggle is ON. Performing mouse click.")
                    performMouseClick()
                } else {
                    println("Boundary toggle is OFF. Ignoring command.")
                }
            }
            else -> println("Unknown command received: '${command.payload?.action}'")
        }
    }

    fun onWicketToggleChanged(isToggled: Boolean) {
        _wicketToggle.update { isToggled }
        println("Wicket toggle changed to: $isToggled")
    }

    fun onBoundaryToggleChanged(isToggled: Boolean) {
        _boundaryToggle.update { isToggled }
        println("Boundary toggle changed to: $isToggled")
    }

    private fun performMouseClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    override fun onDispose() {
        super.onDispose()
        println("Disposing ViewModel and disconnecting client.")
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
