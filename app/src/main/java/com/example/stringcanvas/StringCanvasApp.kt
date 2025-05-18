package com.example.stringcanvas

import android.app.Application
import com.example.stringcanvas.di.AppContainer
import com.example.stringcanvas.di.ServiceLocator

class StringCanvasApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.appContainer = AppContainer(this)
    }
}
