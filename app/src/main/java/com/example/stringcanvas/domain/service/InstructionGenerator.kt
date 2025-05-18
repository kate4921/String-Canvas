package com.example.stringcanvas.domain.service

import android.graphics.Bitmap
import com.example.stringcanvas.domain.models.CanvasSize
import com.example.stringcanvas.domain.models.Instruction2
import com.example.stringcanvas.utils.initPin
import com.example.stringcanvas.utils.lineWeight
import com.example.stringcanvas.utils.lineWidth
import com.example.stringcanvas.utils.minLoop
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class InstructionGenerator {
    fun execute(size: CanvasSize, bitmap: Bitmap): Instruction2 {
        // Преобразовать Bitmap в Mat
        val imageMat = bitmapToMat(bitmap)

        val numPins = size.nailsCount       // Количество штырьков на круглом станке
        val numLines = size.lines      // Максимальное количество линий

        val instructionSteps = mutableListOf<String>()

        // Обрезать изображение
        val minEdge = minOf(imageMat.rows(), imageMat.cols())
        val topEdge = (imageMat.rows() - minEdge) / 2
        val leftEdge = (imageMat.cols() - minEdge) / 2
        val imgCropped = imageMat.submat(topEdge, topEdge + minEdge, leftEdge, leftEdge + minEdge)

        // Преобразовать в оттенки серого
        val imgGray = Mat()
        Imgproc.cvtColor(imgCropped, imgGray, Imgproc.COLOR_BGR2GRAY)

        // Изменить размер изображения
        val imgSized = Mat()
        Imgproc.resize(imgGray, imgSized, imgSized.size(), 1.0, 1.0, Imgproc.INTER_LINEAR)

        // Инвертировать изображение
        val imgInverted = invertImage(imgSized)

        // Количество пикселей, на которое радиус изображения изменяется
        val imgRadius = (imgInverted.height()/2-10).toInt()

        // Применить маску к изображению
        val imgMasked = maskImage(imgInverted, imgRadius)

        // Определить координаты штырьков
        val centerX = imgMasked.width() / 2.0
        val centerY = imgMasked.height() / 2.0
        val coords = pinCoords(radius = imgRadius, numPins = numPins, x0=centerX, y0=centerY )

        // Подготовка переменных
        var i = 0
        val lines = mutableListOf<Pair<Int, Int>>()
        val previousPins = mutableListOf<Int>()
        var oldPin = initPin
        val lineMask = Mat.zeros(imgMasked.size(), imgMasked.type())

        val imgResult = Mat.ones(imgMasked.size(), imgMasked.type())
        Core.multiply(imgResult, Scalar.all(255.0), imgResult)


        instructionSteps.add("${oldPin+1}")
//     Цикл по линиям до достижения критерия остановки
        for (line in 0 until numLines) {
            i++
            var bestLine = 0.0
            val oldCoord = coords[oldPin]

            var bestPin = 0

            // Цикл по возможным линиям
            for (index in 1 until numPins) {
                val pin = (oldPin + index) % numPins

                val coord = coords[pin]

                val linePixels = linePixels(oldCoord, coord)
                // Функция приспособленности
                val lineSum = linePixels.sumOf { imgMasked[it.y.toInt(), it.x.toInt()][0] }

                if (lineSum > bestLine && pin !in previousPins) {
                    bestLine = lineSum
                    bestPin = pin
                }

            }

            // Обновить предыдущие штырьки
            if (previousPins.size >= minLoop) {
                previousPins.removeAt(0)
            }
            previousPins.add(bestPin)

            //!!!
            instructionSteps.add("${bestPin+1}")

            // Вычесть новую линию из изображения
            lineMask.setTo(Scalar.all(0.0))
            Imgproc.line(lineMask, oldCoord, coords[bestPin], Scalar.all(lineWeight.toDouble()), lineWidth)
            Core.subtract(imgMasked, lineMask, imgMasked)

            // Сохранить линию в результатах
            lines.add(oldPin to bestPin)

            // Обновить изображение результата
            val points = linePixels(coords[bestPin], oldCoord)
            for (point in points) {
                imgResult.put(point.y.toInt(), point.x.toInt(), 0.0) // Установка значения черного цвета для пикселя
            }

            // Прервать, если больше нет возможных линий
            if (bestPin == oldPin) {
                break
            }
            // Подготовка к следующей итерации
            oldPin = bestPin
        }

        val resultBitmap = matToBitmap(imgResult)
        return Instruction2(
            instructionSteps
            ,resultBitmap
        )
    }

    // Преобразовать Bitmap в Mat
    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    // Преобразовать Mat в Bitmap
    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    // Инвертировать изображение в оттенки серого
    private fun invertImage(image: Mat): Mat {
        val inverted = Mat()
        Core.bitwise_not(image, inverted)
        return inverted
    }

    // Применить к изображению круговую маску
    private fun maskImage(image: Mat, radius: Int): Mat {
        val mask = Mat.zeros(image.size(), image.type())
        val center = Point(image.width() / 2.0, image.height() / 2.0)
        Imgproc.circle(mask, center, (radius+1).toInt(), Scalar(255.0, 255.0, 255.0), -1)
        val result = Mat()
        Core.bitwise_and(image, image, result, mask)
        return result
    }

    // Вычислить координаты штырьков станка
    private fun pinCoords(radius: Int, numPins: Int = 200, offset: Double = 0.0, x0: Double? = null, y0: Double? = null): List<Point> {
        val coords = mutableListOf<Point>()
        val alpha = DoubleArray(numPins) { i -> (2 * Math.PI * i / numPins + offset) }
        val centerX = x0 ?: (radius + 1.0)
        val centerY = y0 ?: (radius + 1.0)
        for (angle in alpha) {
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            coords.add(Point(x, y))
        }
        return coords
    }


    // Вычислить маску линии
    private fun linePixels(pin0: Point, pin1: Point): List<Point> {
        val length = hypot((pin1.x - pin0.x), (pin1.y - pin0.y))
        val x = DoubleArray(length.toInt()) { i -> pin0.x + i * (pin1.x - pin0.x) / length }
        val y = DoubleArray(length.toInt()) { i -> pin0.y + i * (pin1.y - pin0.y) / length }
        return List(length.toInt()) { Point(x[it], y[it]) }
    }
}


