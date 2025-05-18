package com.example.stringcanvas.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.stringcanvas.domain.repository.ImageRepository

class ImageUseCase(private val imageRepository: ImageRepository) {

    // Метод для сохранения изображения
    suspend fun saveImage(bitmap: Bitmap, imageName: String): Uri? {
        return imageRepository.saveBitmapToUri(bitmap, imageName)
    }

    // Метод для загрузки изображения по URI
    suspend fun loadImage(uri: Uri): Bitmap? {
        return imageRepository.loadBitmapFromUri(uri)
    }
}
