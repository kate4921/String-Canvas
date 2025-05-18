package com.example.stringcanvas.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.graphics.ImageDecoder
import android.provider.MediaStore

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // API Level 28 или выше
            // Используем ImageDecoder для получения Bitmap
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {  // Для старых версий (API Level < 28)
            // Используем MediaStore для получения Bitmap
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

