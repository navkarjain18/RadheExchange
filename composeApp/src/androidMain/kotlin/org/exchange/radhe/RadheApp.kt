
package org.exchange.radhe

import android.app.Application

/**
 * The global Application class for the Android app.
 * Maintains a static reference to the application context if needed for non-Android APIs (though usage should be minimized).
 */
class RadheApp : Application() {

    companion object {
        /** Singleton instance of the application. */
        lateinit var instance: RadheApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
