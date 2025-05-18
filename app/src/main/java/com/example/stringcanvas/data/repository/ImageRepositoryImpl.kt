package com.example.stringcanvas.data.repository

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import com.example.stringcanvas.domain.repository.ImageRepository
import com.example.stringcanvas.utils.saveBitmapToUri
import com.example.stringcanvas.utils.loadBitmapFromUri
import java.io.File

class ImageRepositoryImpl(private val context: Context) : ImageRepository {

    override suspend fun saveBitmapToUri(bitmap: Bitmap, imageName: String): Uri? {
        return saveBitmapToUri(context, bitmap, imageName)
    }

    override suspend fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return loadBitmapFromUri(context, uri)
    }

    override suspend fun deleteImage(uri: Uri) {
        // Преобразуем Uri в путь к файлу
        val file = File(uri.path ?: return)  // Путь из Uri, если есть
        if (file.exists()) {
            file.delete()  // Удаляем файл
        }
    }
}