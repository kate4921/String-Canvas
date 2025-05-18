package com.example.stringcanvas.domain.service

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.stringcanvas.domain.models.Instruction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator(private val context: Context) {

    /** Генерирует PDF-инструкцию и возвращает её Uri */
    fun generate(instr: Instruction): Uri {

        /* ─── параметры макета ─── */
        val pageW     = 595f            // A4 @72 dpi
        val pageH     = 842f
        val margin    = 40f
        val gutter    = 24f
        val colCount  = 3
        val colW      = (pageW - margin * 2 - gutter * (colCount - 1)) / colCount
        val lineStep  = 18f
        val headGap   = 30f

        val pdf   = PdfDocument()
        var pageN = 1
        var page  = pdf.startPage(
            PdfDocument.PageInfo.Builder(pageW.toInt(), pageH.toInt(), pageN).create()
        )
        var canvas = page.canvas

        /* ─── кисти ─── */
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 24f
            color    = Color.BLACK
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            color    = Color.DKGRAY
        }
        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            color    = Color.BLACK
        }
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color    = Color.GRAY
        }
        val stepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            color    = Color.BLACK
        }

        /* ─── шапка ─── */
        fun drawHeader(startY: Float): Float {
            var y = startY + titlePaint.textSize
            canvas.drawText("StringCanvas", margin, y, titlePaint)
            y += headGap
            canvas.drawText(instr.name ?: "Без названия", margin, y, subtitlePaint)
            y += headGap

            instr.imageUri?.let { uri ->
                val bm = context.contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(it)
                }
                bm?.let {
                    val d = 180f
                    val path = Path().apply {
                        addOval(RectF(margin, y, margin + d, y + d), Path.Direction.CCW)
                    }
                    canvas.save()
                    canvas.clipPath(path)
                    canvas.drawBitmap(
                        bm,
                        Rect(0, 0, bm.width, bm.height),
                        RectF(margin, y, margin + d, y + d),
                        null
                    )
                    canvas.restore()
                    y += d + headGap
                }
            }

            canvas.drawText("Гвоздей: ${instr.nailsCount}", margin, y, countPaint)
            y += lineStep
            canvas.drawText("Линий: ${instr.linesCount}", margin, y, countPaint)
            y += headGap
            return y               // точка, откуда стартуют колонки
        }

        var columnTopY = drawHeader(margin)   // y-отступ первой строки шагов

        /* ─── вывод шагов ─── */
        var col   = 0
        var curX  = margin
        var curY  = columnTopY

        fun newPage() {
            pdf.finishPage(page)
            pageN++
            page = pdf.startPage(
                PdfDocument.PageInfo.Builder(pageW.toInt(), pageH.toInt(), pageN).create()
            )
            canvas = page.canvas
            col = 0
            curX = margin
            curY = drawHeader(margin)        // если не нужен повтор шапки — замените на `margin`
            columnTopY = curY
        }

        fun nextLine() {
            curY += lineStep
            if (curY > pageH - margin) {
                if (col == colCount - 1) {   // колонки кончились → новая страница
                    newPage()
                } else {                     // следующая колонка
                    col++
                    curX = margin + col * (colW + gutter)
                    curY = columnTopY
                }
            }
        }

        instr.textList.forEachIndexed { idx, step ->
            val num = "${idx + 1}."
            canvas.drawText(num, curX, curY, numPaint)
            val numW = numPaint.measureText("$num ")
            canvas.drawText(step, curX + numW + 4f, curY, stepPaint)
            nextLine()
        }

        pdf.finishPage(page)

        /* ─── сохранение ─── */
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val fileName = "instruction_${instr.id}.pdf"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Cannot create MediaStore entry")
            resolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "pdf"
            ).apply { mkdirs() }
            val file = File(dir, "instruction_${instr.id}.pdf")
            FileOutputStream(file).use { pdf.writeTo(it) }
            uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
        }

        pdf.close()
        return uri
    }
}
