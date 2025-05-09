package pion.tech.pionbase.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

class PerspectiveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 6f
        style = Paint.Style.FILL
    }

    private val matrix = Matrix()
    private val cornerPoints = mutableListOf(
        PointF(100f, 100f), PointF(600f, 100f),
        PointF(600f, 800f), PointF(100f, 800f)
    )

    private var selectedPointIndex: Int? = null
    private val touchRadius = 60f

    private var offsetX = 0f
    private var offsetY = 0f
    private var isDraggingView = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var borderRectF = RectF()

    //border
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val borderPath = Path()

    fun setBitmap(bmp: Bitmap) {
        bitmap = bmp
        cornerPoints[0] = PointF(0f, 0f)
        cornerPoints[1] = PointF(bmp.width.toFloat(), 0f)
        cornerPoints[2] = PointF(bmp.width.toFloat(), bmp.height.toFloat())
        cornerPoints[3] = PointF(0f, bmp.height.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { bmp ->
            val src = floatArrayOf(
                0f, 0f,
                bmp.width.toFloat(), 0f,
                bmp.width.toFloat(), bmp.height.toFloat(),
                0f, bmp.height.toFloat()
            )
            val dst = floatArrayOf(
                cornerPoints[0].x, cornerPoints[0].y,
                cornerPoints[1].x, cornerPoints[1].y,
                cornerPoints[2].x, cornerPoints[2].y,
                cornerPoints[3].x, cornerPoints[3].y
            )

            matrix.reset()
            matrix.setPolyToPoly(src, 0, dst, 0, 4)
            canvas.drawBitmap(bmp, matrix, paint)

            for (p in cornerPoints) {
                canvas.drawCircle(p.x, p.y, 15f, pointPaint)
            }
        }

        canvas.drawRect(borderRectF, pointPaint)

        //draw border
        borderPath.reset()
        borderPath.moveTo(cornerPoints[0].x, cornerPoints[0].y)
        borderPath.lineTo(cornerPoints[1].x, cornerPoints[1].y)
        borderPath.lineTo(cornerPoints[2].x, cornerPoints[2].y)
        borderPath.lineTo(cornerPoints[3].x, cornerPoints[3].y)
        borderPath.close()
        canvas.drawPath(borderPath, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                selectedPointIndex = cornerPoints.indexOfFirst {
                    distance(it.x, it.y, event.x, event.y) < touchRadius
                }

                if (selectedPointIndex == -1) {
                    // Kiểm tra nếu người dùng chạm vào bên trong tứ giác
                    isDraggingView = isPointInQuadrilateral(event.x, event.y, cornerPoints)
                } else {
                    isDraggingView = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                if (selectedPointIndex != -1 && selectedPointIndex != null) {
                    val index = selectedPointIndex!!
                    val tempPoints = cornerPoints.mapIndexed { i, point ->
                        if (i == index) PointF(point.x + dx, point.y + dy) else PointF(point.x, point.y)
                    }
                    if (isConvexQuad(tempPoints)) {
                        cornerPoints[index].x += dx
                        cornerPoints[index].y += dy
                        invalidate()
                    }
                } else if (isDraggingView) {
                    for (p in cornerPoints) {
                        p.x += dx
                        p.y += dy
                    }
                    offsetX += dx
                    offsetY += dy
                    invalidate()
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                selectedPointIndex = null
                isDraggingView = false
            }
        }
        return true
    }

    fun getWarpedBitmapAndPoints(): Pair<Bitmap, List<PointF>> {
        val bmp = bitmap ?: return Pair(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), emptyList())

        val dst = floatArrayOf(
            cornerPoints[0].x, cornerPoints[0].y,
            cornerPoints[1].x, cornerPoints[1].y,
            cornerPoints[2].x, cornerPoints[2].y,
            cornerPoints[3].x, cornerPoints[3].y
        )

        // 1. Tìm bounding box
        val xs = listOf(dst[0], dst[2], dst[4], dst[6])
        val ys = listOf(dst[1], dst[3], dst[5], dst[7])
        val minX = xs.minOrNull() ?: 0f
        val maxX = xs.maxOrNull() ?: 1f
        val minY = ys.minOrNull() ?: 0f
        val maxY = ys.maxOrNull() ?: 1f

        val width = maxX - minX
        val height = maxY - minY
        val maxSize = max(width, height)

        // 2. Tính tâm
        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2

        // 3. Tính hình vuông bao ngoài
        val squareLeft = centerX - maxSize / 2
        val squareTop = centerY - maxSize / 2

        val adjustedDst = floatArrayOf(
            dst[0] - squareLeft, dst[1] - squareTop,
            dst[2] - squareLeft, dst[3] - squareTop,
            dst[4] - squareLeft, dst[5] - squareTop,
            dst[6] - squareLeft, dst[7] - squareTop
        )

        val matrix = Matrix()
        val src = floatArrayOf(
            0f, 0f,
            bmp.width.toFloat(), 0f,
            bmp.width.toFloat(), bmp.height.toFloat(),
            0f, bmp.height.toFloat()
        )
        matrix.setPolyToPoly(src, 0, adjustedDst, 0, 4)

        val resultBitmap = Bitmap.createBitmap(maxSize.toInt(), maxSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(bmp, matrix, paint)

        // 4. Tạo 4 điểm vuông
        val squarePoints = listOf(
            PointF(squareLeft, squareTop),
            PointF(squareLeft + maxSize, squareTop),
            PointF(squareLeft + maxSize, squareTop + maxSize),
            PointF(squareLeft, squareTop + maxSize)
        )

        borderRectF = RectF(
            squareLeft,
            squareTop,
            squareLeft + maxSize,
            squareTop + maxSize
        )

        postInvalidate()

        return Pair(resultBitmap, squarePoints)
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return hypot((x1 - x2), (y1 - y2))
    }

    private fun isConvexQuad(points: List<PointF>, maxAngleDeg: Float = 160f): Boolean {
        if (points.size != 4) return false

        fun cross(p1: PointF, p2: PointF, p3: PointF): Float {
            val dx1 = p2.x - p1.x
            val dy1 = p2.y - p1.y
            val dx2 = p3.x - p2.x
            val dy2 = p3.y - p2.y
            return dx1 * dy2 - dy1 * dx2
        }

        fun angleBetween(p1: PointF, p2: PointF, p3: PointF): Float {
            val v1x = p1.x - p2.x
            val v1y = p1.y - p2.y
            val v2x = p3.x - p2.x
            val v2y = p3.y - p2.y

            val dot = v1x * v2x + v1y * v2y
            val mag1 = sqrt(v1x * v1x + v1y * v1y)
            val mag2 = sqrt(v2x * v2x + v2y * v2y)

            if (mag1 == 0f || mag2 == 0f) return 180f // điểm trùng nhau → xem như góc bẹt

            val cosTheta = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
            return acos(cosTheta) * (180f / PI.toFloat())
        }

        val signs = mutableListOf<Boolean>()
        for (i in 0..3) {
            val a = points[i]
            val b = points[(i + 1) % 4]
            val c = points[(i + 2) % 4]
            val crossZ = cross(a, b, c)
            if (crossZ == 0f) return false
            signs.add(crossZ > 0)

            val angle = angleBetween(a, b, c)
            if (angle > maxAngleDeg) return false
        }

        return signs.all { it } || signs.all { !it }
    }

    private fun isPointInQuadrilateral(x: Float, y: Float, quad: List<PointF>): Boolean {
        // Chia tứ giác thành 2 tam giác: (0,1,2) và (0,2,3)
        fun area(a: PointF, b: PointF, c: PointF): Float {
            return kotlin.math.abs((a.x*(b.y-c.y) + b.x*(c.y-a.y) + c.x*(a.y-b.y)) / 2f)
        }

        val p = PointF(x, y)
        val A = quad[0]
        val B = quad[1]
        val C = quad[2]
        val D = quad[3]

        val quadArea = area(A, B, C) + area(A, C, D)
        val pointArea = area(p, A, B) + area(p, B, C) + area(p, C, D) + area(p, D, A)

        // Cho phép sai số nhỏ
        return kotlin.math.abs(quadArea - pointArea) < 1f
    }
}