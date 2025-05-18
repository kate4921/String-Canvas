//package com.example.stringcanvas.domain.service
//
//import android.graphics.Bitmap
////import com.example.stringcanvas.domain.models.CanvasSize
//import com.example.stringcanvas.domain.models.Instruction2
////import com.example.stringcanvas.utils.limitPixel
//import com.example.stringcanvas.utils.lineWidth
//import com.example.stringcanvas.utils.initPin
//import org.opencv.android.Utils
//import org.opencv.core.*
//import org.opencv.imgproc.Imgproc
//import kotlin.math.*
//
//private const val PADDING      = 30           // px
//private const val NAIL_RADIUS  = 3
//private const val NAIL_COLOR   = "#888888"
//
//private const val CIRCLE_FORM   = "circle"
//private const val RECT_FORM     = "rect"
//private const val ALBUM_FORM    = "album"
//private const val PORTRAIT_FORM = "portrait"
//private const val IMAGE_FORM    = "image"
//
//private const val BORDER_MODE = "border"
//private const val GRID_MODE   = "grid"
//private const val RANDOM_MODE = "random"
//
///** Публичный API — аналог .generate() в JS */
//class StringArtGenerator(
//    /** режим расположения гвоздей */
//    private val nailsMode: String = BORDER_MODE,
//    /** форма ограничивающей области */
//    private val formType : String = CIRCLE_FORM,
//    /** количество гвоздей */
//    private val nailsCount: Int,
//    /** количество линий */
//    private val linesCount: Int,
//    /** толщина условной «нитки» 0‒1 (как в JS linesWeight%) */
//    private val alpha: Double = 0.35,
//    /** CLAHE контраст */
//    private val clipLimit: Double = 2.0,
//    private val gridSize : Size   = Size(8.0, 8.0)
//) {
//
//    private val alphaPixel = (alpha * 255).coerceIn(1.0, 255.0)
//    private val pxPerPin   = 6.0
//    private val maxCanvas  = 4_096    // OpenGL / Compose limit
//
//    /** Главный метод: берёт Bitmap, выдаёт Instruction2 */
//    fun execute(bitmapSrc: Bitmap): Instruction2 {
//
//        /* ---------- 0. PREPROCESS ---------- */
//        val safeBmp = bitmapSrc.safeARGB(maxCanvas)
//        var img     = Bitmap.createBitmap(safeBmp)
//        val srcMat  = Mat().also { Utils.bitmapToMat(img, it) }
//
//        var mat = resizeForPins(srcMat, nailsCount, pxPerPin)
//        mat = cropToSquare(mat)
//
//        if (max(mat.cols(), mat.rows()) > maxCanvas) {
//            Imgproc.resize(
//                mat, mat,
//                Size(maxCanvas.toDouble(), maxCanvas.toDouble()),
//                0.0, 0.0, Imgproc.INTER_AREA
//            )
//        }
//
//        val gray   = Mat(); Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
//        val clahe  = Imgproc.createCLAHE(clipLimit, gridSize)
//        val prep   = Mat(); clahe.apply(gray, prep)
//        Core.bitwise_not(prep, prep)
//
//        /* ---------- 1. NAILS & INITIAL DATA ---------- */
//        val radius  = (prep.rows() / 2) - PADDING
//        val centerX = prep.width()  / 2.0
//        val centerY = prep.height() / 2.0
//
//        val nails = when (nailsMode) {
//            BORDER_MODE -> buildBorderNails(radius, centerX, centerY)
//            GRID_MODE   -> buildGridNails(prep, nailsCount)
//            RANDOM_MODE -> buildRandomNails(prep, nailsCount)
//            else        -> buildBorderNails(radius, centerX, centerY)
//        }
//
//        val rest     = prep.clone()                    // остаточная яркость
//        val lineMask = Mat(prep.size(), prep.type())   // переисп. маска
//        val result   = Mat(prep.rows(), prep.cols(), CvType.CV_8UC4).apply {
//            setTo(Scalar(255.0, 255.0, 255.0, 255.0))
//        }
//
//        /* ---------- 2. MAIN LOOP ---------- */
//        val steps  = IntArray(linesCount + 1)
//        var stepIx = 0
//        var curPin = initPin % nails.size
//        steps[stepIx++] = curPin + 1
//
//        val recent   = IntArray(3)
//        var recFill  = 0; var recPtr = 0
//        val window   = nails.size / 4      // «дуга» ±45°
//
//        repeat(linesCount) {
//            val (best, score) = findBestNextPin(curPin, nails, rest, recent, recFill, window)
//                ?: return@repeat
//
//            if (recFill < 3) recent[recFill++] = best
//            else { recent[recPtr] = best; recPtr = (recPtr + 1) % 3 }
//
//            /* фиксируем линию */
//            drawLineMask(lineMask, nails[curPin], nails[best])
//            // multiplicative burn
//            val tmp = Mat()
//            Core.multiply(rest, Scalar.all(1.0 - alpha), tmp)
//            tmp.copyTo(rest, lineMask)
//            tmp.release()
//
//            updateResultImage(result, lineMask)
//
//            curPin = best
//            steps[stepIx++] = curPin + 1
//        }
//
//        /* ---------- 3. RESULT ---------- */
//        val fullBmp = result.toBitmap() // mat.toBitmap()
//        val preview = fullBmp.preview(900)
//        val stepList = steps.copyOfRange(0, stepIx).map(Int::toString).toMutableList()
//
//        // освобождаем промежуточные
//        listOf(srcMat, gray, prep, rest, lineMask, mat).forEach { it.release() }
//
//        return Instruction2(stepList, fullBmp, preview)
//    }
//
//    /* ───────────────────── helper-блок ───────────────────── */
//
//    private fun buildBorderNails(r: Int, cx: Double, cy: Double) =
//        List(nailsCount) { i ->
//            val a = 2 * Math.PI * i / nailsCount
//            Point(cx + r * cos(a), cy + r * sin(a))
//        }
//
//    private fun buildGridNails(src: Mat, target: Int): List<Point> {
//        val bbox = Rect(PADDING, PADDING, src.width() - 2*PADDING, src.height() - 2*PADDING)
//        val aspect = bbox.width.toDouble() / bbox.height
//        val rows = sqrt(target / aspect).roundToInt()
//        val cols = sqrt(target * aspect).roundToInt()
//
//        val list = mutableListOf<Point>()
//        repeat(rows) { i ->
//            repeat(cols) { j ->
//                val x = bbox.x + j.toDouble() / (cols-1) * bbox.width
//                val y = bbox.y + i.toDouble() / (rows-1) * bbox.height
//                list += Point(x, y)
//            }
//        }
//        return list
//    }
//
//    private fun buildRandomNails(src: Mat, target: Int): List<Point> {
//        val rnd = java.util.Random()
//        val bbox = Rect(PADDING, PADDING, src.width()-2*PADDING, src.height()-2*PADDING)
//        return List(target) {
//            Point(
//                bbox.x + rnd.nextDouble() * bbox.width,
//                bbox.y + rnd.nextDouble() * bbox.height
//            )
//        }
//    }
//
//    private fun findBestNextPin(
//        cur: Int,
//        nails: List<Point>,
//        rest: Mat,
//        recent: IntArray,
//        recentFill: Int,
//        window: Int
//    ): Pair<Int, Double>? {
//        var best = -1; var bestScore = -1.0
//        val n = nails.size
//        for (off in 1..window) {
//            for (delta in intArrayOf(off, -off)) {
//                val i = (cur + delta + n) % n
//                if (recent.take(recentFill).contains(i)) continue
//                val score = lineScore(nails[cur], nails[i], rest)
//                if (score > bestScore) { bestScore = score; best = i }
//            }
//        }
//        return if (best >= 0) best to bestScore else null
//    }
//
//    private fun lineScore(p0: Point, p1: Point, img: Mat): Double {
//        var sum = 0.0
//        bresenham(p0, p1) { x, y -> sum += img.get(y, x)[0] }
//        return sum
//    }
//
//    private fun drawLineMask(mask: Mat, p0: Point, p1: Point) {
//        mask.setTo(Scalar.all(0.0))
//        Imgproc.line(mask, p0, p1, Scalar.all(alphaPixel), lineWidth)
//    }
//    private fun updateResultImage(dst: Mat, mask: Mat) =
//        dst.setTo(Scalar(0.0, 0.0, 0.0, 255.0), mask)
//
//    /* geometry helpers ------------------------------------------------ */
//    private fun cropToSquare(src: Mat): Mat {
//        val s = min(src.rows(), src.cols())
//        return src.submat(
//            (src.rows()-s)/2, (src.rows()+s)/2,
//            (src.cols()-s)/2, (src.cols()+s)/2
//        )
//    }
//    private inline fun bresenham(a: Point, b: Point, visit: (Int,Int)->Unit) {
//        var x0=a.x.toInt(); var y0=a.y.toInt()
//        val x1=b.x.toInt(); val y1=b.y.toInt()
//        val dx=abs(x1-x0); val sx=if(x0<x1)1 else -1
//        val dy=-abs(y1-y0); val sy=if(y0<y1)1 else -1
//        var err=dx+dy
//        while(true){
//            visit(x0,y0)
//            if(x0==x1 && y0==y1) break
//            val e2=2*err
//            if(e2>=dy){ err+=dy; x0+=sx }
//            if(e2<=dx){ err+=dx; y0+=sy }
//        }
//    }
//    /* Mat ↔ Bitmap ---------------------------------------------------- */
//    private fun Mat.toBitmap(): Bitmap =
//        Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888).also {
//            Utils.matToBitmap(this, it)
//        }
//    /* resize ---------------------------------------------------------- */
//    private fun resizeForPins(src: Mat, nails: Int, px: Double): Mat {
//        val target = (nails*px).roundToInt().coerceAtMost(maxCanvas)
//        val minSide = min(src.rows(), src.cols())
//        val scale = target / minSide.toDouble()
//        if (scale in 0.95..1.05) return src
//        val dst = Mat()
//        Imgproc.resize(
//            src, dst, Size(), scale, scale,
//            if (scale<1.0) Imgproc.INTER_AREA else Imgproc.INTER_LINEAR
//        )
//        if (scale>1.2) Imgproc.GaussianBlur(dst, dst, Size(), 0.7)
//        return dst
//    }
//    /* Bitmap helpers -------------------------------------------------- */
//    private fun Bitmap.safeARGB(maxSide: Int): Bitmap {
//        val argb = if (config != Bitmap.Config.ARGB_8888)
//            copy(Bitmap.Config.ARGB_8888, false) else this
//        val long = max(width, height)
//        if (long <= maxSide) return argb
//        val scale = maxSide.toFloat() / long
//        return Bitmap.createScaledBitmap(argb, (width*scale).toInt(), (height*scale).toInt(), true)
//    }
//    private fun Bitmap.preview(side: Int): Bitmap =
//        if (max(width, height) <= side) this
//        else Bitmap.createScaledBitmap(this, side, side, true)
//}
