
package org.exchange.radhe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * The single Activity entry point for the Android application.
 * Responsible for setting up the Compose content view and enabling edge-to-edge display.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

/**
 * Preview for the main application entry point (Android-specific).
 */
@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
