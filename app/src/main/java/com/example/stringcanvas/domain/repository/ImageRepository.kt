package com.example.stringcanvas.domain.repository

import android.net.Uri
import android.graphics.Bitmap

interface ImageRepository {
    suspend fun saveBitmapToUri(bitmap: Bitmap, imageName: String): Uri?
    suspend fun loadBitmapFromUri(uri: Uri): Bitmap?
    suspend fun deleteImage(uri: Uri)  // Добавляем метод для удаления изображения
}