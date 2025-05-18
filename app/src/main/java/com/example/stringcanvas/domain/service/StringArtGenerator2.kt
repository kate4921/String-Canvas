package com.example.stringcanvas.domain.service

import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*
import kotlin.random.Random
import android.graphics.Bitmap

class InstructionGenerator3(
    var pinCount: Int = 200,
    var maxLines: Int = 2000,
    var lineWeight: Float = 0.1f,
    var blurSigma: Double = 2.0
) {
    private data class LineCache(
        val pinPair: Pair<Int, Int>,
        val pixels: List<Point>
    )

    private lateinit var pins: List<Point>
    private lateinit var lineCache: List<LineCache>
    private lateinit var currentImage: Mat
    private lateinit var targetImage: Mat
    private lateinit var residual: Mat
    private var imageSize = 0

    // Результаты
    var pinSequence = mutableListOf<Pair<Int, Int>>()
    private var resultBitmap: Bitmap? = null

    fun generate(bitmap: Bitmap): Result {
        try {
            prepareResources(bitmap)
            runGeneration()
            return Result(getResultBitmap(), pinSequence.toList())
        } catch (e: Exception) {
            throw StringArtException("Generation failed", e)
        }
    }

    private fun prepareResources(bitmap: Bitmap) {
        // Конвертация и предобработка изображения
        val src = Mat().apply {
            Utils.bitmapToMat(bitmap, this)
            Imgproc.cvtColor(this, this, Imgproc.COLOR_RGBA2GRAY)
            Core.bitwise_not(this, this)
            convertTo(this, CvType.CV_32F, 1.0/255.0)
            Imgproc.GaussianBlur(this, this, Size(0.0, 0.0), blurSigma)
        }

        // Обрезка до квадрата
        imageSize = min(src.rows(), src.cols())
        targetImage = Mat(imageSize, imageSize, CvType.CV_32F).apply {
            Imgproc.resize(src, this, Size(imageSize.toDouble(), imageSize.toDouble()))
        }

        // Инициализация пинов по окружности
        pins = List(pinCount) { i ->
            val angle = 2.0 * PI * i / pinCount
            Point(
                imageSize/2.0 + (imageSize/2.0 - 10) * cos(angle),
                imageSize/2.0 + (imageSize/2.0 - 10) * sin(angle)
            )
        }

        // Предвычисление линий
        lineCache = buildList {
            for (i in 0 until pinCount) {
                for (j in i+1 until pinCount) {
                    val linePixels = bresenhamLinePoints(pins[i], pins[j])
                    add(LineCache(i to j, linePixels))
                    add(LineCache(j to i, linePixels))
                }
            }
        }

        // Инициализация рабочих матриц
        currentImage = Mat.zeros(imageSize, imageSize, CvType.CV_32F)
        residual = targetImage.clone()
    }

    private fun runGeneration() {
        var lastPin = Random.nextInt(pinCount)

        repeat(maxLines) { step ->
            // 1. Выбор кандидатов (окрестность + случайные)
            val candidates = selectCandidates(lastPin, step)

            // 2. Поиск лучшей линии
            val bestLine = findBestLine(candidates)

            // 3. Обновление состояния
            applyLineToImage(bestLine)
            pinSequence.add(bestLine.pinPair)
            lastPin = bestLine.pinPair.second
        }

        // Финализация результата
        convertResultToBitmap()
    }

    private fun selectCandidates(lastPin: Int, step: Int): List<LineCache> {
        val dynamicSampleSize = (lineCache.size * (0.1 + 0.4 * (step.toDouble() / maxLines))).toInt()
        val neighborCount = (dynamicSampleSize * 0.7).toInt()

        return buildList {
            // Соседние пины
            val neighbors = getNeighborPins(lastPin, neighborCount)
            addAll(lineCache.filter { it.pinPair.first == lastPin && it.pinPair.second in neighbors })

            // Случайные пины
            val remaining = (dynamicSampleSize - size).coerceAtLeast(1)
            addAll(lineCache.filter { it.pinPair.first == lastPin }.shuffled().take(remaining))
        }
    }

    private fun getNeighborPins(pin: Int, count: Int): Set<Int> {
        require(pin in 0 until pinCount) { "Invalid pin index: $pin" }
        require(count > 0) { "Count must be positive" }

        val half = count / 2
        val start = (pin - half).coerceAtLeast(0)
        val end = (pin + half).coerceAtMost(pinCount - 1)

        return (start..end)
            .map { it % pinCount }
            .toSet()
    }

    private fun findBestLine(candidates: List<LineCache>): LineCache {
        var maxScore = 0f
        lateinit var bestLine: LineCache

        for (line in candidates) {
            var score = 0f
            for (pt in line.pixels) {
                val x = pt.x.toInt().coerceIn(0, imageSize-1)
                val y = pt.y.toInt().coerceIn(0, imageSize-1)
                score += residual.get(y, x)[0].toFloat()
            }

            if (score > maxScore) {
                maxScore = score
                bestLine = line
            }
        }

        return bestLine
    }

    private fun applyLineToImage(line: LineCache) {
        for (pt in line.pixels) {
            val x = pt.x.toInt().coerceIn(0, imageSize-1)
            val y = pt.y.toInt().coerceIn(0, imageSize-1)
            val newValue = min(1.0, currentImage.get(y, x)[0] + lineWeight)
            currentImage.put(y, x, newValue)
            residual.put(y, x, targetImage.get(y, x)[0] - newValue)
        }
    }

    private fun convertResultToBitmap() {
        val resultMat = Mat().apply {
            Core.multiply(currentImage, Scalar(255.0), this)
            convertTo(this, CvType.CV_8U)
            Core.bitwise_not(this, this)
        }

        resultBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888).apply {
            Utils.matToBitmap(resultMat, this)
        }
    }

    private fun getResultBitmap(): Bitmap {
        return resultBitmap ?: throw IllegalStateException("Result not ready")
    }

    private fun bresenhamLinePoints(p0: Point, p1: Point): List<Point> {
        val points = mutableListOf<Point>()
        var x0 = p0.x.toInt()
        var y0 = p0.y.toInt()
        val x1 = p1.x.toInt()
        val y1 = p1.y.toInt()

        val dx = abs(x1 - x0)
        val dy = -abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx + dy

        while (true) {
            points.add(Point(x0.toDouble(), y0.toDouble()))
            if (x0 == x1 && y0 == y1) break
            val e2 = 2 * err
            if (e2 >= dy) {
                err += dy
                x0 += sx
            }
            if (e2 <= dx) {
                err += dx
                y0 += sy
            }
        }
        return points
    }

    data class Result(
        val bitmap: Bitmap,
        val pinSequence: List<Pair<Int, Int>>
    )

    class StringArtException(message: String, cause: Throwable?) : Exception(message, cause)

}
