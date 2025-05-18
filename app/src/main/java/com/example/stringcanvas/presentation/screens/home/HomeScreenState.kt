package com.example.stringcanvas.presentation.screens.home

import android.graphics.Bitmap

data class HomeScreenState(
    val bitmap: Bitmap? = null,
    val countNails: Int = 300,
    val countLines: Int = 4000,
    val isGenerating: Boolean = false,
    val generatedIds: List<Long> = emptyList()
)

