package com.example.stringcanvas.domain.service

//package com.example.stringcanvas.domain

interface AppStateMonitor {
    val isAppInForeground: Boolean
    fun onAppForeground()
    fun onAppBackground()
}

class AndroidAppStateMonitor : AppStateMonitor {
    override var isAppInForeground: Boolean = false

    override fun onAppForeground() {
        isAppInForeground = true
    }

    override fun onAppBackground() {
        isAppInForeground = false
    }
}