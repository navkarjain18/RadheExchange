
package org.exchange.radhe

import android.app.Application

class iMusicApp : Application() {

    companion object {
        lateinit var instance: iMusicApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
