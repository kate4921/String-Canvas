package com.example.stringcanvas.utils

import android.graphics.Bitmap
import kotlin.math.max

/** ARGB-8888 + ограничение самой длинной стороны. */
fun Bitmap.toSafeArgb(maxSide: Int): Bitmap {
    val argb = if (config != Bitmap.Config.ARGB_8888)
        copy(Bitmap.Config.ARGB_8888, false) else this

    val long = max(argb.width, argb.height)
    if (long <= maxSide) return argb

    val scale = maxSide.toFloat() / long
    val w = (argb.width  * scale).toInt()
    val h = (argb.height * scale).toInt()
    return Bitmap.createScaledBitmap(argb, w, h, true)
}

/** уменьшенная копия для превью */
fun Bitmap.toPreview(side: Int): Bitmap =
    if (max(width, height) <= side) this
    else Bitmap.createScaledBitmap(this, side, side, true)