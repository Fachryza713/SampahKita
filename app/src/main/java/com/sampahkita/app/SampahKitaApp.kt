package com.sampahkita.app

import android.app.Application
import com.sampahkita.app.data.AppContainer

class SampahKitaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
