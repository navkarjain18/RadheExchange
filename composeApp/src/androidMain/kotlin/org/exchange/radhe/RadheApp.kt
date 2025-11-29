
package org.exchange.radhe

import android.app.Application
import org.exchange.radhe.platform.ContextProvider

class RadheApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.context = this
    }
}
