
package org.exchange.radhe.features.details

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI
import org.exchange.radhe.platform.Platform
import org.exchange.radhe.platform.getPlatform

class DetailsViewModel : ScreenModel {

    private val wsClient = DI.wsClient

    private val _wicketToggle = MutableStateFlow(false)
    val wicketToggle = _wicketToggle.asStateFlow()

    private val _boundaryToggle = MutableStateFlow(false)
    val boundaryToggle = _boundaryToggle.asStateFlow()

    private val _message = MutableStateFlow("No message yet")
    val message = _message.asStateFlow()

    // Robot is a JVM class, so we need to handle it carefully in multiplatform.
    private val robot: Any? = if (getPlatform() == Platform.Desktop) {
        java.awt.Robot()
    } else null

    init {
        screenModelScope.launch {
            wsClient.connect("ws://localhost:8080")
            wsClient.observeMessages()
                .onEach { msg ->
                    _message.value = msg
                    handleCommand(msg)
                }
                .launchIn(screenModelScope)
        }
    }

    private fun handleCommand(command: String) {
        when (command) {
            "wicket_click" -> if (_wicketToggle.value) performMouseClick()
            "boundary_click" -> if (_boundaryToggle.value) performMouseClick()
        }
    }

    fun onWicketToggleChanged(isToggled: Boolean) {
        _wicketToggle.update { isToggled }
    }

    fun onBoundaryToggleChanged(isToggled: Boolean) {
        _boundaryToggle.update { isToggled }
    }

    private fun performMouseClick() {
        (robot as? java.awt.Robot)?.apply {
            mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
            mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
        }
    }

    override fun onDispose() {
        super.onDispose()
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
