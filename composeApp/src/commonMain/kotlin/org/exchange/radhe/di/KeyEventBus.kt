
package org.exchange.radhe.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Enumeration of physical key actions detected by the platform.
 */
enum class KeyAction {
    /** Represents the Volume Up button press. */
    VOLUME_UP,
    
    /** Represents the Volume Down button press. */
    VOLUME_DOWN 
}

/**
 * Internal Event Bus for propagating hardware key events from platform-specific services
 * (like Android AccessibilityService) to business logic components.
 */
object KeyEventBus {
    private val _events = MutableSharedFlow<KeyAction>()
    
    /** Public flow to observe incoming key events. */
    val events = _events.asSharedFlow()

    /**
     * Emits a new key action to all observers.
     * @param action The physical key action detected.
     */
    suspend fun send(action: KeyAction) {
        _events.emit(action)
    }
}
