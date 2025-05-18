package com.example.stringcanvas.domain.usecase

import android.content.Context
import android.widget.Toast
import com.example.stringcanvas.domain.service.AppStateMonitor
import com.example.stringcanvas.domain.service.NotificationHelper

//package com.example.stringcanvas.domain.usecase

class ShowGenerationNotificationUseCase(
    private val notificationHelper: NotificationHelper,
    private val context: Context,
    private val appStateMonitor: AppStateMonitor
) {
    operator fun invoke() {
        if (appStateMonitor.isAppInForeground) {
            Toast.makeText(context, "Генерация завершена!", Toast.LENGTH_SHORT).show()
        } else {
            notificationHelper.showGenerationCompleteNotification()
        }
    }
}