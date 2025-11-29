
package org.exchange.radhe.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class KeyAction { VOLUME_UP, VOLUME_DOWN }

object KeyEventBus {
    private val _events = MutableSharedFlow<KeyAction>()
    val events = _events.asSharedFlow()

    suspend fun send(action: KeyAction) {
        _events.emit(action)
    }
}
