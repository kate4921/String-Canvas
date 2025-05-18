package com.example.stringcanvas.utils


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun saveBitmapToUri(context: Context, bitmap: Bitmap, imageName: String): Uri? {
    // Получаем директорию для хранения файлов
    val imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File(imageDir, "$imageName.png")

    try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        // Создаем и возвращаем Uri для файла
        return Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return null
}

