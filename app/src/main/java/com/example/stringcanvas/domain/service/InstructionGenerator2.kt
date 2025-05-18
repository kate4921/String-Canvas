//package com.example.stringcanvas.domain.service
//
//import android.graphics.Bitmap
//import com.example.stringcanvas.domain.models.CanvasSize
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.utils.initPin
//import com.example.stringcanvas.utils.lineWidth
//import com.example.stringcanvas.utils.toPreview
//import com.example.stringcanvas.utils.toSafeArgb
//import org.opencv.android.Utils
//import org.opencv.core.*
//import org.opencv.imgproc.Imgproc
//import kotlin.math.*
//
//
//class InstructionGenerator2(
//    /* «ручки» */
//    private val clipLimit : Double = 2.0,
//    private val gridSize  : Size   = Size(8.0, 8.0),
//    private val alpha     : Double = 0.35,      // «толщина» нитки по яркости
//    private val cooldown  : Int    = 3          // запрет на последние n пинов
//) {
//    /* константы */
//    private val pxPerPin       = 6.0
//    private val maxCanvasSide  = 4_096
//    private val earlyStopRatio = 0.005          // <0.5 % прироста – стоп
//    private val alphaPixel     = (alpha * 255).coerceIn(1.0, 255.0)
//
//    /* ────────── публичные API ────────── */
//
//    fun execute(size: CanvasSize, mat: Mat): Instruction2 =
//        internalExecute(size, mat.clone())      // не трогаем чужой Mat
//
//    @JvmOverloads
//    fun execute(size: CanvasSize, bmp: Bitmap): Instruction2 {
//        val safe = bmp.safeARGB(maxCanvasSide)
//        val m    = Mat().also { Utils.bitmapToMat(safe, it) }
//        return internalExecute(size, m)
//    }
//
//    /* ────────── ядро ────────── */
//
//    private fun internalExecute(size: CanvasSize, src: Mat): Instruction2 {
//        try {
//            /* 0. PREPROCESS ------------------------------------------------ */
//            var img = resizeForPins(src, size.nailsCount, pxPerPin)
//            img = cropToSquare(img)
//            if (max(img.cols(), img.rows()) > maxCanvasSide)
//                Imgproc.resize(img, img,
//                    Size(maxCanvasSide.toDouble(), maxCanvasSide.toDouble()),
//                    0.0, 0.0, Imgproc.INTER_AREA)
//
//            val gray = Mat()
//            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY)
//            val clahe = Imgproc.createCLAHE(clipLimit, gridSize)
//            val prep  = Mat(); clahe.apply(gray, prep)
//            Core.bitwise_not(prep, prep)
//
//            /* 1. INITIAL DATA ---------------------------------------------- */
//            val radius  = (prep.rows() / 2) - 10
//            val maskImg = applyCircularMask(prep, radius)
//
//            val pins = pinCoords(
//                radius, size.nailsCount,
//                cx = maskImg.width()  / 2.0,
//                cy = maskImg.height() / 2.0
//            )
//
//            val rest     = maskImg.clone()
//            val lineMask = Mat(maskImg.size(), maskImg.type())
//            val result   = Mat(maskImg.rows(), maskImg.cols(), CvType.CV_8UC4).apply {
//                setTo(Scalar(255.0, 255.0, 255.0, 255.0))
//            }
//
//            /* 2. MAIN LOOP -------------------------------------------------- */
//            val steps = IntArray(size.lines + 1)
//            var stepIx = 0
//            var curPin = initPin
//            steps[stepIx++] = curPin + 1
//
//            val recent  = IntArray(cooldown)
//            var recFill = 0; var recPtr = 0
//
//            repeat(size.lines) {
//                val (best, score) =
//                    findBestNextPin(curPin, pins, rest, recent, recFill) ?: return@repeat
//                if (score < earlyStopRatio * Core.mean(rest).`val`[0]) return@repeat
//
//                if (recFill < cooldown) recent[recFill++] = best
//                else { recent[recPtr] = best; recPtr = (recPtr + 1) % cooldown }
//
//                /* ---- фиксируем линию ---- */
//                drawLineMask(lineMask, pins[curPin], pins[best])
//
//                /* мягко «выжигаем» яркость только под линией */
//                val tmp = Mat()
//                Core.multiply(rest, Scalar.all(1.0 - alpha), tmp) // tmp = rest*0.65
//                tmp.copyTo(rest, lineMask)                        // по маске
//
//                updateResultImage(result, lineMask)
//
//                curPin = best
//                steps[stepIx++] = curPin + 1
//            }
//
//            /* 3. RESULT ----------------------------------------------------- */
//            val fullBmp    = matToBitmap(result)
//            val previewBmp = fullBmp.preview(900)
//            val stepList   = steps.copyOfRange(0, stepIx)
//                .map(Int::toString)
//                .toMutableList()
//
//            return Instruction2(stepList, fullBmp, previewBmp)
//        }
//        finally { src.release() }
//    }
//
//
//    /* ───────────────────  HELPERS  ──────────────────── */
//
//    /** поиск следующего пина — смотрим до полуокружности */
//    private fun findBestNextPin(
//        curPin: Int,
//        pins: List<Point>,
//        rest: Mat,
//        recent: IntArray,
//        recentFill: Int
//    ): Pair<Int, Double>? {
//
//        val n          = pins.size
//        val maxOffset  = n / 2                      // ← до полукруга
//
//        var bestIdx   = -1
//        var bestScore = -1.0
//
//        for (off in 1..maxOffset) {
//            for (delta in intArrayOf(off, -off)) {
//
//                val idx = (curPin + delta + n) % n
//
//                // cool-down
//                var skip = false
//                for (i in 0 until recentFill)
//                    if (recent[i] == idx) { skip = true; break }
//                if (skip) continue
//
//                val s = lineScore(pins[curPin], pins[idx], rest)
//                if (s > bestScore) { bestScore = s; bestIdx = idx }
//            }
//        }
//        return if (bestIdx >= 0) bestIdx to bestScore else null
//    }
//
//    /* интеграл яркости по линии */
//    private fun lineScore(p0: Point, p1: Point, img: Mat): Double {
//        var sum = 0.0
//        bresenham(p0, p1) { x, y -> sum += img.get(y, x)[0] }
//        return sum
//    }
//
//    /* маска линии */
//    private fun drawLineMask(mask: Mat, p0: Point, p1: Point) {
//        mask.setTo(Scalar.all(0.0))
//        Imgproc.line(mask, p0, p1, Scalar.all(alphaPixel), lineWidth)
//    }
//
//    /* переносим «чёрную нить» в холст BGRA */
//    private fun updateResultImage(dst: Mat, mask: Mat) =
//        dst.setTo(Scalar(0.0, 0.0, 0.0, 255.0), mask)
//
//    /* ---- утилиты ---- */
//
//    private fun cropToSquare(src: Mat): Mat {
//        val side = min(src.rows(), src.cols())
//        return src.submat(
//            (src.rows() - side) / 2, (src.rows() + side) / 2,
//            (src.cols() - side) / 2, (src.cols() + side) / 2
//        )
//    }
//
//    private fun applyCircularMask(img: Mat, r: Int): Mat =
//        Mat.zeros(img.size(), img.type()).also { mask ->
//            Imgproc.circle(mask, Point(img.width()/2.0, img.height()/2.0),
//                r, Scalar(255.0,255.0,255.0), -1)
//            Core.bitwise_and(img, img, img, mask)   // результат пишем в img
//        }
//
//    private fun pinCoords(
//        radius: Int, count: Int, cx: Double, cy: Double
//    ) = List(count) { i ->
//        val a = 2.0 * Math.PI * i / count
//        Point(cx + radius*cos(a), cy + radius*sin(a))
//    }
//
//    private inline fun bresenham(p0: Point, p1: Point, visit: (Int,Int)->Unit) {
//        var x0=p0.x.toInt(); var y0=p0.y.toInt()
//        val x1=p1.x.toInt(); val y1=p1.y.toInt()
//        val dx=abs(x1-x0);   val sx=if(x0<x1)1 else -1
//        val dy=-abs(y1-y0);  val sy=if(y0<y1)1 else -1
//        var err=dx+dy
//        while(true){
//            visit(x0,y0)
//            if(x0==x1 && y0==y1) break
//            val e2=2*err
//            if(e2>=dy){ err+=dy; x0+=sx }
//            if(e2<=dx){ err+=dx; y0+=sy }
//        }
//    }
//
//    /* Mat ↔ Bitmap */
//
//    private fun matToBitmap(mat: Mat) =
//        Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888).also {
//            Utils.matToBitmap(mat, it)
//        }
//
//    /* resize */
//
//    private fun resizeForPins(src: Mat, nails: Int, pxPerPin: Double): Mat {
//        val targetSide = (nails*pxPerPin).roundToInt().coerceAtMost(maxCanvasSide)
//        val minSide    = min(src.rows(), src.cols())
//        val scale      = targetSide / minSide.toDouble()
//        if (scale in 0.95..1.05) return src
//
//        val dst = Mat()
//        Imgproc.resize(
//            src, dst, Size(), scale, scale,
//            if (scale<1.0) Imgproc.INTER_AREA else Imgproc.INTER_LINEAR
//        )
//        if (scale>1.2) Imgproc.GaussianBlur(dst, dst, Size(), 0.7)
//        return dst
//    }
//
//    /* Bitmap helpers */
//
//    private fun Bitmap.safeARGB(maxSide: Int): Bitmap {
//        val argb = if (config != Bitmap.Config.ARGB_8888)
//            copy(Bitmap.Config.ARGB_8888, false) else this
//
//        val longSide = max(width, height)
//        if (longSide <= maxSide) return argb
//
//        val scale = maxSide.toFloat()/longSide
//        return Bitmap.createScaledBitmap(
//            argb, (width*scale).toInt(), (height*scale).toInt(), true
//        )
//    }
//    private fun Bitmap.preview(side: Int): Bitmap =
//        if (max(width, height) <= side) this
//        else Bitmap.createScaledBitmap(this, side, side, true)
//}
//
//
//
//
//
//
//
////class InstructionGenerator2(
////    private val clipLimit: Double = 2.0,
////    private val gridSize: Size = Size(8.0, 8.0),
////    private val alpha: Double = 0.35,          // «полупрозрачная» нить
////    private val window: Int = 30,              // скользящее окно по пинам
////    private val cooldown: Int = 3              // заморозка уже использованных пинов
////) {
////
////    fun execute(size: CanvasSize, bitmap: Bitmap): Instruction2 {
////
////        /* ---------- 0.  PREPROCESS  ---------- */
////        val img = bitmapToMat(bitmap)
////        val cropped = cropToSquare(img)
////        val gray = Mat()
////        Imgproc.cvtColor(cropped, gray, Imgproc.COLOR_BGR2GRAY)
////
////        // CLAHE (контраст + детали)
////        val clahe = Imgproc.createCLAHE(clipLimit, gridSize)
////        val imgPrep = Mat()
////        clahe.apply(gray, imgPrep)
////
////        // инвертируем, чтобы «тёмное» = «непокрыто»
////        Core.bitwise_not(imgPrep, imgPrep)
////
////        /* ---------- 1.  INITIAL DATA  ---------- */
////        val radius = (imgPrep.rows() / 2) - 10
////        val maskImg = applyCircularMask(imgPrep, radius)
////
////        val pins = pinCoords(radius, size.nailsCount,
////            centerX = maskImg.width() / 2.0, centerY = maskImg.height() / 2.0
////        )
////
////        val rest = maskImg.clone()           // «остаточная яркость»
////        val lineMask = Mat.zeros(maskImg.size(), maskImg.type())
////
////        val resultImage = Mat.ones(maskImg.size(), maskImg.type()).apply {
////            Core.multiply(this, Scalar.all(255.0), this)
////        }
////
////        /* ---------- 2.  MAIN GREEDY LOOP  ---------- */
////        val steps = mutableListOf<String>()
////        val path = mutableListOf<Int>()
////
////        var curPin = initPin
////        val recent = ArrayDeque<Int>()  // очередь последних τ пинов
////
////        steps += (curPin + 1).toString()
////        path += curPin
////
////        repeat(size.lines) {
////
////            val best = findBestNextPin(curPin, pins, rest, recent)
////                ?: return@repeat                 // больше нет улучшения
////
////            // обновляем очередь «cool-down»
////            if (recent.size >= cooldown) recent.removeFirst()
////            recent += best
////
////            // фиксируем линию
////            drawLineMask(lineMask, pins[curPin], pins[best])
////            Core.subtract(rest, Scalar.all(alpha), rest, lineMask)  // α-нить
////
////            updateResultImage(resultImage, lineMask)
////
////            curPin = best
////            steps += (curPin + 1).toString()
////            path += curPin
////        }
////
////        /* ---------- 3.  OPTIONAL 2-OPT SWAP  ---------- */
////        postOptimizeTwoOpt(path, pins, rest)
////
////        val bmp = matToBitmap(resultImage)
////        return Instruction2(steps, bmp)
////    }
////
////    /* ===== HELPER BLOCK ===== */
////
////    private fun findBestNextPin(
////        curPin: Int,
////        pins: List<Point>,
////        rest: Mat,
////        recent: Collection<Int>
////    ): Int? {
////        var bestScore = -1.0
////        var bestIdx: Int? = null
////
////        for (offset in 1..window) {
////            val idx = (curPin + offset) % pins.size
////            if (idx in recent) continue
////
////            val score = lineScore(pins[curPin], pins[idx], rest)
////            if (score > bestScore) {
////                bestScore = score
////                bestIdx = idx
////            }
////        }
////        return bestIdx
////    }
////
////    private fun lineScore(p0: Point, p1: Point, img: Mat): Double {
////        var s = 0.0
////        bresenham(p0, p1) { x, y ->
////            s += img.get(y, x)[0]  // интенсивность остатка
////        }
////        return s
////    }
////
////    private fun drawLineMask(mask: Mat, p0: Point, p1: Point) {
////        mask.setTo(Scalar.all(0.0))
////        Imgproc.line(mask, p0, p1, Scalar.all(alpha), lineWidth)
////    }
////
////    private fun updateResultImage(dst: Mat, mask: Mat) {
////        val inv = Mat.zeros(mask.size(), mask.type())
////        Core.compare(mask, Scalar.all(0.0), inv, Core.CMP_GT)
////        dst.setTo(Scalar.all(0.0), inv)             // визуально рисуем нить
////    }
////
////    /* ----------  UTILITIES  ---------- */
////
////    private fun cropToSquare(src: Mat): Mat {
////        val sz = minOf(src.rows(), src.cols())
////        val y = (src.rows() - sz) / 2
////        val x = (src.cols() - sz) / 2
////        return src.submat(y, y + sz, x, x + sz)
////    }
////
////    private fun applyCircularMask(img: Mat, r: Int): Mat {
////        val mask = Mat.zeros(img.size(), img.type())
////        Imgproc.circle(mask, Point(img.width() / 2.0, img.height() / 2.0), r,
////            Scalar(255.0, 255.0, 255.0), -1)
////        val res = Mat()
////        Core.bitwise_and(img, img, res, mask)
////        return res
////    }
////
////    private fun pinCoords(radius: Int, count: Int, centerX: Double, centerY: Double): List<Point> =
////        List(count) { i ->
////            val θ = 2.0 * Math.PI * i / count
////            Point(centerX + radius * cos(θ), centerY + radius * sin(θ))
////        }
////
////    /* ---  Брезенхэм (int-точки)  --- */
////    private inline fun bresenham(p0: Point, p1: Point, visit: (Int, Int) -> Unit) {
////        var x0 = p0.x.toInt(); var y0 = p0.y.toInt()
////        val x1 = p1.x.toInt(); val y1 = p1.y.toInt()
////        val dx = kotlin.math.abs(x1 - x0); val sx = if (x0 < x1) 1 else -1
////        val dy = -kotlin.math.abs(y1 - y0); val sy = if (y0 < y1) 1 else -1
////        var err = dx + dy
////        while (true) {
////            visit(x0, y0)
////            if (x0 == x1 && y0 == y1) break
////            val e2 = 2 * err
////            if (e2 >= dy) { err += dy; x0 += sx }
////            if (e2 <= dx) { err += dx; y0 += sy }
////        }
////    }
////
////    private fun postOptimizeTwoOpt(path: MutableList<Int>, pins: List<Point>, rest: Mat) {
////        // два прохода 2-opt; быстрый прирост качества
////        repeat(2) {
////            var improved = false
////            for (i in 0 until path.lastIndex - 2) {
////                val a = path[i]; val b = path[i + 1]
////                for (j in i + 2 until path.size - 1) {
////                    val c = path[j]; val d = path[j + 1]
////                    val delta = twoOptDelta(pins[a], pins[b], pins[c], pins[d], rest)
////                    if (delta > 0) {
////                        path.subList(i + 1, j + 1).reverse()
////                        improved = true
////                    }
////                }
////            }
////            if (!improved) return
////        }
////    }
////
////    private fun twoOptDelta(p1: Point, p2: Point, p3: Point, p4: Point, img: Mat): Double {
////        // приблизительная (быстрая) оценка выигрыша по суммарной яркости
////        val old = lineScore(p1, p2, img) + lineScore(p3, p4, img)
////        val newVal = lineScore(p1, p3, img) + lineScore(p2, p4, img)
////        return newVal - old
////    }
////
////    /* ----------  Bitmap / Mat  ---------- */
////
////    private fun bitmapToMat(bmp: Bitmap) = Mat().apply { Utils.bitmapToMat(bmp, this) }
////
////    private fun matToBitmap(mat: Mat) =
////        Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888).also {
////            Utils.matToBitmap(mat, it)
////        }
////}
