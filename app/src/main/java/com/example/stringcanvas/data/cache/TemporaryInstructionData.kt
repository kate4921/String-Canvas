package com.example.stringcanvas.data.cache

import android.graphics.Bitmap

data class TemporaryInstructionData(
    val id: Long,
    val steps: List<String>,
    val bitmap: Bitmap,
    val nailsCount: Int = 0,
    val linesCount: Int = 0
)