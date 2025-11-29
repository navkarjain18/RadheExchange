package org.exchange.radhe

import android.app.Application


class RadheApp : Application() {

    companion object {
        lateinit var instance: RadheApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
