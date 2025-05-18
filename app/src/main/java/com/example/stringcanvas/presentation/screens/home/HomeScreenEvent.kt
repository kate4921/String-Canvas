package com.example.stringcanvas.presentation.screens.home

import android.content.Context
import android.net.Uri

sealed class HomeScreenEvent {
    data class ImageCropped(val croppedUri: Uri, val context: Context) : HomeScreenEvent()
    data class NailsCountChanged(val newCount: Int) : HomeScreenEvent()
    data class ThreadCountChanged(val newCount: Int) : HomeScreenEvent()
    object GenerateClicked : HomeScreenEvent()
}
