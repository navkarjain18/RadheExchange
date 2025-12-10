package org.exchange.radhe

import androidx.compose.runtime.Composable

/**
 * The main entry point for the shared UI logic.
 * This Composable is expected to be implemented by each platform (Android/iOS/Desktop)
 * to provide the root content view.
 */
@Composable
expect fun App()
