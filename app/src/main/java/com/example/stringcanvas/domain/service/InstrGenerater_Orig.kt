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

class InstructionGenerator_000 {
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



//
//import android.graphics.Bitmap
//import android.util.Log
//import com.example.stringcanvas.domain.models.CanvasSize
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.utils.initPin
//import com.example.stringcanvas.utils.lineWidth
//import org.opencv.android.Utils
//import org.opencv.core.*
//import org.opencv.imgproc.Imgproc
//import kotlin.math.*
//
///* ------------------ константы ------------------ */
//private const val TARGET_RADIUS = 512        // px от центра до гвоздя
//private const val PENALTY_K = 0.05           // штраф за повтор ребра
//private const val PREVIEW_THICKNESS = 3      // px, не чётное проще
//
////object MatPool {
////    private val pool = ArrayDeque<Mat>()
////
////    /** выдаёт мат нужного размера/типа или создаёт новый */
////    fun get(rows: Int, cols: Int, type: Int): Mat {
////        val m = pool.firstOrNull { it.rows() == rows && it.cols() == cols && it.type() == type }
////        return if (m != null) {
////            pool.remove(m)
////            m
////        } else Mat(rows, cols, type)
////    }
////
////    /** кладём обратно (после .release() вызывать не нужно) */
////    fun recycle(mat: Mat) = pool.add(mat)
////}
//
//object MatPool {
//    private val pool = ArrayDeque<Mat>()
//
//    fun get(rows: Int, cols: Int, type: Int): Mat {
//        return pool.firstOrNull {
//            it.rows() == rows && it.cols() == cols && it.type() == type && !it.empty()
//        }?.also { pool.remove(it) } ?: Mat(rows, cols, type)
//    }
//
//    fun recycle(mat: Mat) {
//        if (!mat.empty()) {
//            mat.release()
//            pool.add(mat)
//        }
//    }
//
//    fun recycleAll() {
//        pool.forEach { it.release() }
//        pool.clear()
//    }
//}
//
//class InstructionGenerator_minus {
//
////    fun execute(size: CanvasSize, bitmap: Bitmap): Instruction2 {
////        /* ---------- 0. подготовка входа ---------- */
////        val numPins = size.nailsCount
////        val numLines = size.lines
////        val steps = mutableListOf<String>()
////
////        val src = bitmapToMat(bitmap)
////
////        /* ---------- 1. квадрат + gray ---------- */
////        val minEdge = min(src.rows(), src.cols())
////        val imgCrop = src.submat(
////            (src.rows() - minEdge) / 2,
////            (src.rows() + minEdge) / 2,
////            (src.cols() - minEdge) / 2,
////            (src.cols() + minEdge) / 2
////        )
////
////        val imgGray = MatPool.get(imgCrop.rows(), imgCrop.cols(), CvType.CV_8U)
////        Imgproc.cvtColor(imgCrop, imgGray, Imgproc.COLOR_BGR2GRAY)
////
////        /* ---------- 2. resize + invert + float ---------- */
////        val targetSide = 2 * TARGET_RADIUS + 2
////        val imgScaled = MatPool.get(targetSide, targetSide, CvType.CV_8U)
////        val interp = if (imgGray.rows() > targetSide)
////            Imgproc.INTER_AREA else Imgproc.INTER_CUBIC
////        Imgproc.resize(imgGray, imgScaled,
////            Size(targetSide.toDouble(), targetSide.toDouble()),
////            0.0, 0.0, interp)
////
////        Core.bitwise_not(imgScaled, imgScaled)          // uint8
////        val imgFloat = MatPool.get(imgScaled.rows(), imgScaled.cols(), CvType.CV_32F)
////        imgScaled.convertTo(imgFloat, CvType.CV_32F, 1.0 / 255.0)
////
////        /* ---------- 3. маска круга ---------- */
////        val imgMasked = applyCircularMask(imgFloat, TARGET_RADIUS)
////        val maskCircle = Mat.zeros(imgMasked.size(), CvType.CV_32F)
////
////        Imgproc.circle(maskCircle,
////            Point(imgMasked.width() / 2.0, imgMasked.height() / 2.0),
////            TARGET_RADIUS, Scalar.all(1.0), -1)
////
////        /* ---------- 4. геометрия гвоздиков ---------- */
////        val centerX = imgMasked.width() / 2.0
////        val centerY = imgMasked.height() / 2.0
////        val pins = pinCoords(TARGET_RADIUS, numPins, x0 = centerX, y0 = centerY)
////
////        /* ---------- 5. рабочие буферы ---------- */
////        val edgeCount = HashMap<Pair<Int, Int>, Int>()
////        val lineMask = Mat.zeros(imgMasked.size(), CvType.CV_32F)
////
////        // «чистый» белый холст для результата
////        val imgResult = Mat.ones(imgMasked.size(), CvType.CV_8U)
////        Core.multiply(imgResult, Scalar.all(255.0), imgResult)
////
////        /* ---------- 6. главный цикл ---------- */
////        var oldPin = initPin
////        steps += "${oldPin + 1}"
////
////        repeat(numLines) {
////            /* 6.1 поиск лучшего пина */
////            var bestPin = -1
////            var bestScore = Double.NEGATIVE_INFINITY
////            val p0 = pins[oldPin]
////
////            for (shift in 1 until numPins) {
////                val pin = (oldPin + shift) % numPins
////                val p1 = pins[pin]
////
////                val length = hypot((p1.x - p0.x), (p1.y - p0.y))
////                val sum = lineSumBresenham(imgMasked, p0, p1)
////                val norm = sum / length
////                val freq = edgeCount.getOrDefault(oldPin to pin, 0)
////                val score = norm - PENALTY_K * freq
////
////                if (score > bestScore) {
////                    bestScore = score
////                    bestPin = pin
////                }
////            }
////
////            if (bestPin == -1 || bestPin == oldPin) return@repeat
////
////            /* 6.2 фиксируем и рисуем линию */
////            val p1 = pins[bestPin]
////            steps += "${bestPin + 1}"
////            edgeCount[oldPin to bestPin] = edgeCount.getOrDefault(oldPin to bestPin, 0) + 1
////
////            // 6.2.1 вычитаем из карты ошибок
////            lineMask.setTo(Scalar.all(0.0))
////            Imgproc.line(lineMask, p0, p1, Scalar.all(1.0), lineWidth)
////            Core.subtract(imgMasked, lineMask, imgMasked)
////            normalizeInsideMask(imgMasked, maskCircle)
////
////            // 6.2.2 штрихуем на холсте результата
////            drawLineOnResult(imgResult, p0, p1)
////
////            oldPin = bestPin
////        }
////
////        /* ---------- 7. очистка и возврат результата ---------- */
////        MatPool.recycle(imgGray)
////        MatPool.recycle(imgScaled)
////        MatPool.recycle(imgFloat)
////
////        return Instruction2(steps, matToBitmap(imgResult))
////    }
//
//
//    fun execute(size: CanvasSize, bitmap: Bitmap): Instruction2 {
//        // Валидация входных данных
//        require(size.nailsCount > 0) { "Nails count must be positive" }
//        require(size.lines > 0) { "Lines count must be positive" }
//        require(!bitmap.isRecycled) { "Bitmap is recycled" }
//
//        val steps = mutableListOf<String>()
//        val src = try {
//            bitmapToMat(bitmap)
//        } catch (e: Exception) {
//            Log.e("OpenCV", "Bitmap to Mat conversion failed", e)
//            return Instruction2(mutableListOf<String>(), Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
//        }
//
//        try {
//            /* ---------- 1. Подготовка изображения ---------- */
//            val minEdge = min(src.rows(), src.cols())
//            val imgCrop = src.submat(
//                (src.rows() - minEdge) / 2,
//                (src.rows() + minEdge) / 2,
//                (src.cols() - minEdge) / 2,
//                (src.cols() + minEdge) / 2
//            )
//
//            val imgGray = MatPool.get(imgCrop.rows(), imgCrop.cols(), CvType.CV_8U).apply {
//                Imgproc.cvtColor(imgCrop, this, Imgproc.COLOR_BGR2GRAY)
//            }
//
//            /* ---------- 2. Изменение размера и преобразование ---------- */
//            val targetSide = 2 * TARGET_RADIUS + 2
//            val imgScaled = MatPool.get(targetSide, targetSide, CvType.CV_8U).apply {
//                val interp = if (imgGray.rows() > targetSide) Imgproc.INTER_AREA else Imgproc.INTER_CUBIC
//                Imgproc.resize(imgGray, this, Size(targetSide.toDouble(), targetSide.toDouble()), 0.0, 0.0, interp)
//                Core.bitwise_not(this, this)
//            }
//
//            val imgFloat = MatPool.get(imgScaled.rows(), imgScaled.cols(), CvType.CV_32F).apply {
//                imgScaled.convertTo(this, CvType.CV_32F, 1.0 / 255.0)
//            }
//
//            /* ---------- 3. Применение круглой маски ---------- */
//            val imgMasked = applyCircularMask(imgFloat, TARGET_RADIUS)
//            val maskCircle = Mat.zeros(imgMasked.size(), CvType.CV_32F).apply {
//                Imgproc.circle(this, Point(imgMasked.width() / 2.0, imgMasked.height() / 2.0),
//                    TARGET_RADIUS, Scalar.all(1.0), -1)
//            }
//
//            /* ---------- 4. Подготовка данных ---------- */
//            val centerX = imgMasked.width() / 2.0
//            val centerY = imgMasked.height() / 2.0
//            val pins = pinCoords(TARGET_RADIUS, size.nailsCount, centerX, centerY)
//
//            val edgeCount = HashMap<Pair<Int, Int>, Int>()
//            val lineMask = Mat.zeros(imgMasked.size(), CvType.CV_32F)
//
//            // Холст для результата
//            val imgResult = Mat.ones(imgMasked.size(), CvType.CV_8U).apply {
//                Core.multiply(this, Scalar.all(255.0), this)
//            }
//
//            /* ---------- 5. Основной цикл генерации ---------- */
//            var oldPin = initPin
//            steps += "${oldPin + 1}"
//
//            repeat(size.lines) { iteration ->
//                try {
//                    // Поиск следующего лучшего пина
//                    val bestPin = findBestNextPin(oldPin, pins, imgMasked, edgeCount, size.nailsCount)
//                    if (bestPin == -1 || bestPin == oldPin) return@repeat
//
//                    // Фиксация и рисование линии
//                    steps += "${bestPin + 1}"
//                    edgeCount[oldPin to bestPin] = edgeCount.getOrDefault(oldPin to bestPin, 0) + 1
//
//                    // Обновление карты ошибок
//                    updateErrorMap(imgMasked, maskCircle, lineMask, pins[oldPin], pins[bestPin])
//
//                    // Рисование на результате
//                    drawLineOnResult(imgResult, pins[oldPin], pins[bestPin])
//
//                    oldPin = bestPin
//                } catch (e: Exception) {
//                    Log.e("Generation", "Error in iteration $iteration", e)
//                }
//            }
//
//            return Instruction2(steps, matToBitmap(imgResult))
//        } catch (e: Exception) {
//            Log.e("Generation", "Error in generation process", e)
//            return Instruction2(mutableListOf<String>(), Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
//        } finally {
//            // Освобождение ресурсов
//            MatPool.recycleAll()
//        }
//    }
//
//// Вспомогательные функции:
//
//    private fun findBestNextPin(
//        currentPin: Int,
//        pins: List<Point>,
//        imgMasked: Mat,
//        edgeCount: HashMap<Pair<Int, Int>, Int>,
//        numPins: Int
//    ): Int {
//        var bestPin = -1
//        var bestScore = Double.NEGATIVE_INFINITY
//        val p0 = pins[currentPin]
//
//        for (shift in 1 until numPins) {
//            val pin = (currentPin + shift) % numPins
//            val p1 = pins[pin]
//
//            val length = hypot((p1.x - p0.x), (p1.y - p0.y))
//            val sum = lineSumBresenham(imgMasked, p0, p1)
//            val norm = sum / length
//            val freq = edgeCount.getOrDefault(currentPin to pin, 0)
//            val score = norm - PENALTY_K * freq
//
//            if (score > bestScore) {
//                bestScore = score
//                bestPin = pin
//            }
//        }
//        return bestPin
//    }
//
//    private fun updateErrorMap(
//        imgMasked: Mat,
//        maskCircle: Mat,
//        lineMask: Mat,
//        p0: Point,
//        p1: Point
//    ) {
//        lineMask.setTo(Scalar.all(0.0))
//        Imgproc.line(lineMask, p0, p1, Scalar.all(1.0), lineWidth)
//
//        // Создаем временную маску правильного типа
//        val tempMask = Mat()
//        lineMask.convertTo(tempMask, imgMasked.type())
//
//        Core.subtract(imgMasked, tempMask, imgMasked, maskCircle)
//        normalizeInsideMask(imgMasked, maskCircle)
//
//        tempMask.release()
//    }
//
//    private fun normalizeInsideMask(srcDst: Mat, mask: Mat) {
//        // Конвертируем маску в правильный тип
//        val mask8u = Mat()
//        mask.convertTo(mask8u, CvType.CV_8U, 255.0)
//
//        try {
//            val minMax = Core.minMaxLoc(srcDst, mask8u)
//            val min = minMax.minVal
//            val max = if (minMax.maxVal > min + 1e-6) minMax.maxVal else min + 1.0
//
//            // Создаем маску правильного типа для операции
//            val operationMask = Mat()
//            mask.convertTo(operationMask, srcDst.type())
//
//            Core.subtract(srcDst, Scalar.all(min), srcDst, operationMask)
//            Core.multiply(srcDst, Scalar.all(1.0 / (max - min)), srcDst, 1.0, srcDst.type())
//
//            operationMask.release()
//        } finally {
//            mask8u.release()
//        }
//    }
//
//    /* ==================================================================== */
//    /* =====================  ↓  вспомогательные  ↓  ====================== */
//
//    private fun bitmapToMat(bitmap: Bitmap): Mat =
//        Mat().also { Utils.bitmapToMat(bitmap, it) }
//
//    private fun matToBitmap(mat: Mat): Bitmap =
//        Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
//            .also { Utils.matToBitmap(mat, it) }
//
//    /** float-маска: всё вне круга зануляется */
//    private fun applyCircularMask(src: Mat, radius: Int): Mat {
//        val mask = Mat.zeros(src.size(), CvType.CV_32F)
//        Imgproc.circle(mask,
//            Point(src.width() / 2.0, src.height() / 2.0),
//            radius, Scalar.all(1.0), -1)
//        val dst = Mat()
//        Core.multiply(src, mask, dst)
//        return dst
//    }
//
//    /** координаты гвоздиков */
//    private fun pinCoords(radius: Int, n: Int, x0: Double, y0: Double) =
//        List(n) { i ->
//            val a = 2 * Math.PI * i / n
//            Point(x0 + radius * cos(a), y0 + radius * sin(a))
//        }
//
//    /** сумма яркостей вдоль отрезка (Брезенхэм) */
//    private fun lineSumBresenham(mat: Mat, p0: Point, p1: Point): Double {
//        var x0 = p0.x.roundToInt(); var y0 = p0.y.roundToInt()
//        val x1 = p1.x.roundToInt(); val y1 = p1.y.roundToInt()
//        val dx = abs(x1 - x0); val dy = abs(y1 - y0)
//        val sx = if (x0 < x1) 1 else -1
//        val sy = if (y0 < y1) 1 else -1
//        var err = dx - dy
//        var s = 0.0
//        while (true) {
//            s += mat.get(y0, x0)[0]
//            if (x0 == x1 && y0 == y1) break
//            val e2 = 2 * err
//            if (e2 > -dy) { err -= dy; x0 += sx }
//            if (e2 < dx) { err += dx; y0 += sy }
//        }
//        return s
//    }
//
//    /** рисуем чёрную линию на uint8-холсте */
////    private fun drawLineOnResult(mat: Mat, p0: Point, p1: Point) {
////        // Для PREVIEW_THICKNESS=3 получим radius=1 (целочисленное деление)
////        val radius = PREVIEW_THICKNESS / 2
////
////        var x0 = p0.x.roundToInt(); var y0 = p0.y.roundToInt()
////        val x1 = p1.x.roundToInt(); val y1 = p1.y.roundToInt()
////        val dx = abs(x1 - x0); val dy = abs(y1 - y0)
////        val sx = if (x0 < x1) 1 else -1
////        val sy = if (y0 < y1) 1 else -1
////        var err = dx - dy
////
////        while (true) {
////            // Передаём radius как Int (без преобразований)
////            Imgproc.circle(mat, Point(x0.toDouble(), y0.toDouble()),
////                radius, Scalar.all(0.0), -1)
////
////            if (x0 == x1 && y0 == y1) break
////
////            val e2 = 2 * err
////            when {
////                e2 > -dy -> { err -= dy; x0 += sx }
////                e2 < dx ->  { err += dx; y0 += sy }
////            }
////        }
////    }
//
//    private fun drawLineOnResult(mat: Mat, p0: Point, p1: Point) {
//        try {
//            // Ensure we're working with proper type
//            if (mat.type() != CvType.CV_8UC1) {
//                val temp = Mat()
//                mat.convertTo(temp, CvType.CV_8UC1)
//                temp.copyTo(mat)
//                temp.release()
//            }
//
//            // Use OpenCV's line drawing with thickness
//            Imgproc.line(mat, p0, p1, Scalar.all(0.0), PREVIEW_THICKNESS)
//        } catch (e: Exception) {
//            Log.e("OpenCV", "Line drawing error: ${e.message}")
//        }
//    }
//
//    /** Локальная нормализация внутри маски */
////    private fun normalizeInsideMask(srcDst: Mat, mask: Mat) {
////        val minMax = Core.minMaxLoc(srcDst, mask)
////        val min = minMax.minVal
////        val max = minMax.maxVal.takeIf { it > min + 1e-6 } ?: (min + 1.0)
////        Core.subtract(srcDst, Scalar.all(min), srcDst, mask)
////        Core.multiply(srcDst, Scalar.all(1.0 / (max - min)), srcDst, 1.0, CvType.CV_32F)
////    }
////    private fun normalizeInsideMask(srcDst: Mat, mask: Mat) {
////        try {
////            // Create proper mask for minMaxLoc (must be CV_8U)
////            val mask8u = Mat()
////            mask.convertTo(mask8u, CvType.CV_8U, 255.0)
////
////            // Get min/max values
////            val minMax = Core.minMaxLoc(srcDst, mask8u)
////            val min = minMax.minVal
////            val max = if (minMax.maxVal > min + 1e-6) minMax.maxVal else min + 1.0
////
////            // Create proper mask for subtract operation (must match srcDst type)
////            val operationMask = Mat()
////            mask.convertTo(operationMask, srcDst.type())
////
////            // Perform normalization
////            Core.subtract(srcDst, Scalar.all(min), srcDst, operationMask)
////            Core.multiply(srcDst, Scalar.all(1.0 / (max - min)), srcDst, 1.0, srcDst.type())
////
////            // Release temporary mats
////            mask8u.release()
////            operationMask.release()
////        } catch (e: Exception) {
////            Log.e("OpenCV", "Normalization error: ${e.message}")
////        }
////    }
//}
//
//
//
//
//
//
//
////
////import android.graphics.Bitmap
////import com.example.stringcanvas.domain.models.CanvasSize
////import com.example.stringcanvas.domain.models.Instruction2
////import com.example.stringcanvas.utils.initPin
////import com.example.stringcanvas.utils.lineWidth
////import com.example.stringcanvas.utils.lineWeight   // если не нужен – уберите
////import org.opencv.android.Utils
////import org.opencv.core.*
////import org.opencv.imgproc.Imgproc
////import kotlin.math.*
////
/////* ------------------ константы ------------------ */
////private const val TARGET_RADIUS = 512        // px от центра до гвоздя
////private const val PENALTY_K     = 0.05       // штраф за повтор ребра
////
////class InstructionGenerator {
////
////    fun execute(size: CanvasSize, bitmap: Bitmap): Instruction2 {
////
////        /* ---------- 0. подготовка входа ---------- */
////        val numPins  = size.nailsCount
////        val numLines = size.lines
////        val steps    = mutableListOf<String>()
////
////        val src      = bitmapToMat(bitmap)
////
////        /* ---------- 1. квадрат + gray ---------- */
////        val minEdge  = min(src.rows(), src.cols())
////        val imgCrop  = src.submat(
////            (src.rows() - minEdge) / 2,
////            (src.rows() + minEdge) / 2,
////            (src.cols() - minEdge) / 2,
////            (src.cols() + minEdge) / 2
////        )
////
////        val imgGray = Mat()
////        Imgproc.cvtColor(imgCrop, imgGray, Imgproc.COLOR_BGR2GRAY)
////
////        /* ---------- 2. resize + invert + float ---------- */
////        val targetSide = 2 * TARGET_RADIUS + 2
////        val imgScaled  = Mat()
////        val interp     = if (imgGray.rows() > targetSide)
////            Imgproc.INTER_AREA else Imgproc.INTER_CUBIC
////        Imgproc.resize(imgGray, imgScaled,
////            Size(targetSide.toDouble(), targetSide.toDouble()),
////            0.0, 0.0, interp)
////
////        Core.bitwise_not(imgScaled, imgScaled)          // uint8
////        val imgFloat = Mat()
////        imgScaled.convertTo(imgFloat, CvType.CV_32F, 1.0 / 255.0)
////
////        /* ---------- 3. маска круга ---------- */
////        val imgMasked = applyCircularMask(imgFloat, TARGET_RADIUS)
////
////        /* ---------- 4. геометрия гвоздиков ---------- */
////        val centerX = imgMasked.width()  / 2.0
////        val centerY = imgMasked.height() / 2.0
////        val pins = pinCoords(TARGET_RADIUS, numPins, x0 = centerX, y0 = centerY)
////
////        /* ---------- 5. рабочие буферы ---------- */
////        val edgeCount = HashMap<Pair<Int,Int>, Int>()
////        val lineMask  = Mat.zeros(imgMasked.size(), CvType.CV_32F)
////
////        // «чистый» белый холст для результата
////        val imgResult = Mat.ones(imgMasked.size(), CvType.CV_8U)
////        Core.multiply(imgResult, Scalar.all(255.0), imgResult)
////
////        /* ---------- 6. главный цикл ---------- */
////        var oldPin = initPin
////        steps += "${oldPin + 1}"
////
////        repeat(numLines) {
////            /* 6.1 поиск лучшего пина */
////            var bestPin   = -1
////            var bestScore = Double.NEGATIVE_INFINITY
////            val p0        = pins[oldPin]
////
////            for (shift in 1 until numPins) {
////                val pin   = (oldPin + shift) % numPins
////                val p1    = pins[pin]
////
////                val sum   = lineSumBresenham(imgMasked, p0, p1)
////                val freq  = edgeCount.getOrDefault(oldPin to pin, 0)
////                val score = sum - PENALTY_K * freq
////
////                if (score > bestScore) {
////                    bestScore = score
////                    bestPin   = pin
////                }
////            }
////
////            if (bestPin == -1 || bestPin == oldPin) return@repeat
////
////            /* 6.2 фиксируем и рисуем линию */
////            val p1 = pins[bestPin]
////            steps += "${bestPin + 1}"
////            edgeCount[oldPin to bestPin] = edgeCount.getOrDefault(oldPin to bestPin, 0) + 1
////
////            // 6.2.1 вычитаем из карты ошибок
////            lineMask.setTo(Scalar.all(0.0))
////            Imgproc.line(lineMask, p0, p1, Scalar.all(1.0), lineWidth)
////            Core.subtract(imgMasked, lineMask, imgMasked)
////            Core.normalize(imgMasked, imgMasked, 0.0, 1.0, Core.NORM_MINMAX)
////
////            // 6.2.2 штрихуем на холсте результата
////            drawLineOnResult(imgResult, p0, p1)
////
////            oldPin = bestPin
////        }
////
////        /* ---------- 7. готовый bitmap ---------- */
////        return Instruction2(steps, matToBitmap(imgResult))
////    }
////
////    /* ==================================================================== */
////    /* =====================  ↓  вспомогательные  ↓  ====================== */
////
////    private fun bitmapToMat(bitmap: Bitmap): Mat =
////        Mat().also { Utils.bitmapToMat(bitmap, it) }
////
////    private fun matToBitmap(mat: Mat): Bitmap =
////        Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
////            .also { Utils.matToBitmap(mat, it) }
////
////    /** float-маска: всё вне круга зануляется */
////    private fun applyCircularMask(src: Mat, radius: Int): Mat {
////        val mask = Mat.zeros(src.size(), CvType.CV_32F)
////        Imgproc.circle(mask,
////            Point(src.width() / 2.0, src.height() / 2.0),
////            radius, Scalar.all(1.0), -1)
////        val dst = Mat()
////        Core.multiply(src, mask, dst)
////        return dst
////    }
////
////    /** координаты гвоздиков */
////    private fun pinCoords(radius: Int, n: Int, x0: Double, y0: Double) =
////        List(n) { i ->
////            val a = 2 * Math.PI * i / n
////            Point(x0 + radius * cos(a), y0 + radius * sin(a))
////        }
////
////    /** сумма яркостей вдоль отрезка (Брезенхэм) */
////    private fun lineSumBresenham(mat: Mat, p0: Point, p1: Point): Double {
////        var x0 = p0.x.roundToInt(); var y0 = p0.y.roundToInt()
////        val x1 = p1.x.roundToInt(); val y1 = p1.y.roundToInt()
////        val dx = abs(x1 - x0); val dy = abs(y1 - y0)
////        val sx = if (x0 < x1) 1 else -1
////        val sy = if (y0 < y1) 1 else -1
////        var err = dx - dy
////        var s = 0.0
////        while (true) {
////            s += mat.get(y0, x0)[0]
////            if (x0 == x1 && y0 == y1) break
////            val e2 = 2 * err
////            if (e2 > -dy) { err -= dy; x0 += sx }
////            if (e2 <  dx) { err += dx; y0 += sy }
////        }
////        return s
////    }
////
////    /** рисуем чёрную линию на uint8-холсте */
////    private fun drawLineOnResult(mat: Mat, p0: Point, p1: Point) {
////        var x0 = p0.x.roundToInt(); var y0 = p0.y.roundToInt()
////        val x1 = p1.x.roundToInt(); val y1 = p1.y.roundToInt()
////        val dx = abs(x1 - x0); val dy = abs(y1 - y0)
////        val sx = if (x0 < x1) 1 else -1
////        val sy = if (y0 < y1) 1 else -1
////        var err = dx - dy
////        while (true) {
////            mat.put(y0, x0, 0.0)
////            if (x0 == x1 && y0 == y1) break
////            val e2 = 2 * err
////            if (e2 > -dy) { err -= dy; x0 += sx }
////            if (e2 <  dx) { err += dx; y0 += sy }
////        }
////    }
////}
