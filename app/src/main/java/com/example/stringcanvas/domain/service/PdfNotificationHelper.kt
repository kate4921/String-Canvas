package com.example.stringcanvas.domain.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.stringcanvas.R

class PdfNotificationHelper(private val context: Context) {

    private val channelId = "pdf_download"

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = context.getSystemService(NotificationManager::class.java)
            val ch  = NotificationChannel(
                channelId,
                "PDF downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mgr.createNotificationChannel(ch)
        }
    }

    fun notifyDownloaded(uri: Uri, fileName: String) {
        // ----- ① проверяем разрешение -----
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // нет разрешения – молча выходим (или отправьте event в UI)
            return
        }

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_picture_as_pdf_24)
            .setContentTitle("PDF скачан")
            .setContentText(fileName)
            .setContentIntent(pending)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(uri.hashCode(), builder.build())
    }
}

