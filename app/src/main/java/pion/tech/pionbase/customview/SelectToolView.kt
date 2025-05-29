package pion.tech.pionbase.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import pion.tech.pionbase.R
import pion.tech.pionbase.util.DeviceDimensionsHelper.convertDpToPixel
import pion.tech.pionbase.util.convertDpToPx
import kotlin.math.max
import kotlin.math.min

class SelectToolView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var isTouch = false
    private val currentPoint = PointF()
    private var mBitmap: Bitmap? = null
    private var mCanvas = Canvas()

    private var isPreviewInFirstQuadrant = true

    //sticker
    private var bitmapSticker: Bitmap? = null
    private var currentSizeSticker = 0
    private var originStickerSize = 0
    private var matrixSticker = Matrix()
    private var stickerPositionX = 0f
    private var stickerPositionY = 0f

    private var paintCutSticker = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private var subPaintPath = Paint().apply {
        isAntiAlias = true
        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }

    //bitmapBg
    private var bitmapBg: Bitmap? = null

    private val selectPaint = Paint()
    private val selectPath = Path()

    //bound cua path
    private val boundPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    //fill cua select path
    private val fillPaint = Paint().apply {
        color = Color.parseColor("#4DFFFFFF")
        style = Paint.Style.FILL
    }
    private var canDrawFillPath = false

    //bitmap add
    private var bitmapAdd: Bitmap? = null

    private val pathPoints = mutableListOf<PointF>()
    private var isDrawPathDone = false

    //control button
    private var canDrawControlButton = false
    private var bitmapDelete: Bitmap? = null
    private var bitmapRotate: Bitmap? = null
    private var bitmapScale: Bitmap? = null
    private var sizeButtonControl = context.convertDpToPx(24)

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)

        initControlButton()
    }

    private fun initControlButton() {
        bitmapDelete = context.getDrawable(R.drawable.ic_delete)?.toBitmap(
            width = sizeButtonControl, height = sizeButtonControl, config = Bitmap.Config.ARGB_8888
        )

        bitmapScale = context.getDrawable(R.drawable.ic_scale)?.toBitmap(
            width = sizeButtonControl, height = sizeButtonControl, config = Bitmap.Config.ARGB_8888
        )

        bitmapRotate = context.getDrawable(R.drawable.ic_rotate)?.toBitmap(
            width = sizeButtonControl, height = sizeButtonControl, config = Bitmap.Config.ARGB_8888
        )
    }

    init {
        selectPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = context.convertDpToPixel(2f)
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }
    }

    fun setBitmapBg(bg: Bitmap) {
        bitmapBg = bg
        postInvalidate()
    }

    fun setBitmapSticker(bitmapSticker: Bitmap) {
        //k ve fill path nua
        canDrawFillPath = true

        this.bitmapSticker = bitmapSticker
        postInvalidate()
    }

    fun setSizeSticker(zoomPercent: Int) {
        //0 -> size ban dau
        //100 -> size gap doi
        val stickerSize = originStickerSize + originStickerSize * zoomPercent / 100
        currentSizeSticker = stickerSize
        postInvalidate()
    }

    fun setStickerPosition(isX: Boolean, value: Int) {
        //value: 0->100
        //0->50 tru di
        //50-> 100 cong them
        //can quy doi ve 100->originSize

        val resultSize = (value.toFloat() / 100f - 0.5f) * currentSizeSticker * 3

        Log.d("CHECKPOSITION", "resultSize: $resultSize")

        if (isX) {
            stickerPositionX = resultSize
        } else {
            stickerPositionY = resultSize
        }
        postInvalidate()
    }

    fun setCutBlur(value: Int) {
        if (value > 0f) {
            subPaintPath.maskFilter = BlurMaskFilter(value.toFloat(), BlurMaskFilter.Blur.NORMAL)
        } else {
            subPaintPath.maskFilter = null
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawBg(mCanvas)
        drawSticker(mCanvas)
        drawSelectPath(mCanvas)
        drawSelectPathFill(mCanvas)
        drawBound(mCanvas)
        drawButtonControl(mCanvas)

        mBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        if (isTouch) {
            getBitmapAroundPoint()?.let {
                listener?.onDrawSelect(it)

            }
        }
    }

    private fun drawSticker(canvas: Canvas) {
        bitmapSticker ?: return

        val bitmapCut = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val subCanvas = Canvas(bitmapCut)

        subCanvas.drawPath(selectPath, subPaintPath)

        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        val scale = currentSizeSticker.toFloat() / bitmapSticker!!.width.toFloat()

        matrixSticker.setTranslate(bounds.left + stickerPositionX, bounds.top + stickerPositionY)
        matrixSticker.postScale(scale, scale, bounds.left, bounds.top)

        subCanvas.drawBitmap(bitmapSticker!!, matrixSticker, paintCutSticker)

        canvas.drawBitmap(bitmapCut, 0f, 0f, null)
    }

    private fun drawBg(canvas: Canvas) {
        bitmapBg?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    private fun drawSelectPath(canvas: Canvas) {
        canvas.drawPath(selectPath, selectPaint)
    }

    private fun drawSelectPathFill(canvas: Canvas) {
        if (!canDrawFillPath) {
            canvas.drawPath(selectPath, fillPaint)

            val bounds = RectF()
            selectPath.computeBounds(bounds, true)

            bitmapAdd?.let {
                canvas.drawBitmap(
                    it,
                    bounds.centerX() - it.width / 2,
                    bounds.centerY() - it.width / 2,
                    null
                )
            }
        }
    }

    private fun drawBound(canvas: Canvas) {
        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        canvas.drawRect(bounds, boundPaint)
    }

    private fun drawButtonControl(canvas: Canvas) {
        if (canDrawControlButton) {
            val bounds = RectF()
            selectPath.computeBounds(bounds, true)

            bitmapDelete?.let {
                canvas.drawBitmap(
                    it,
                    bounds.left - sizeButtonControl,
                    bounds.bottom,
                    null
                )
            }
            bitmapScale?.let {
                canvas.drawBitmap(
                    it,
                    bounds.right,
                    bounds.bottom,
                    null
                )
            }
            bitmapRotate?.let {
                canvas.drawBitmap(
                    it,
                    bounds.right,
                    bounds.top - sizeButtonControl,
                    null
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //xu ly su kien khi chua ve xong select path
                if (!isDrawPathDone) {
                    isTouch = true

                    pathPoints.clear()
                    val point = PointF(event.x, event.y)
                    pathPoints.add(point)

                    selectPath.reset()
                    selectPath.moveTo(event.x, event.y)
                    currentPoint.set(event.x, event.y)
                    checkFirstQuadrant()
                }

                //xu ly su kien khi bam vao vung select path
                if (isPointInsidePath(event.x, event.y)) {
                    if (bitmapSticker == null) {
                        listener?.onAddSticker()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                //xu ly su kien khi chua ve xong select path
                if (!isDrawPathDone) {
                    isTouch = true

                    // Check if current point intersects with any previous point
                    val currentX = event.x
                    val currentY = event.y
                    val intersectionResult = findIntersectionPoint(currentX, currentY)

                    if (intersectionResult != null && pathPoints.size > 2) {
                        // Found intersection, create a new closed path from the intersection
                        val (intersectionPoint, intersectionIndex) = intersectionResult

                        // Create a new path with only the points that form the closed shape
                        selectPath.reset()
                        selectPath.moveTo(intersectionPoint.x, intersectionPoint.y)

                        // Add points from the intersection index to the end of the current path
                        for (i in intersectionIndex until pathPoints.size) {
                            selectPath.lineTo(pathPoints[i].x, pathPoints[i].y)
                        }

                        // Close the path
                        selectPath.close()
                        isTouch = false
                        isDrawPathDone = true
                        checkSizePath()
                        postInvalidate()
                        return true
                    }

                    // No intersection, continue drawing
                    val point = PointF(currentX, currentY)
                    pathPoints.add(point)
                    selectPath.lineTo(currentX, currentY)
                    currentPoint.set(currentX, currentY)
                    checkFirstQuadrant()
                    postInvalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                //xu ly su kien khi chua ve xong select path
                if (!isDrawPathDone) {
                    isTouch = false
                    selectPath.close()
                    isDrawPathDone = true
                    checkSizePath()
                    postInvalidate()
                }
            }
        }
        return true
    }

    private fun findIntersectionPoint(
        x: Float,
        y: Float,
        threshold: Float = 20f
    ): Pair<PointF, Int>? {
        // Bỏ qua kiểm tra nếu chưa đủ điểm
        if (pathPoints.size <= 50) return null

        // Chỉ kiểm tra các điểm đầu tiên, bỏ qua các điểm gần đây
        // Tăng số lượng điểm bỏ qua ở cuối lên 10 thay vì 3
        for (i in 0 until pathPoints.size - 50) {
            val point = pathPoints[i]
            val distance = Math.sqrt(
                Math.pow((x - point.x).toDouble(), 2.0) +
                        Math.pow((y - point.y).toDouble(), 2.0)
            )

            // Giảm ngưỡng xuống để yêu cầu điểm gần hơn
            if (distance < 15f) {
                return Pair(point, i)
            }
        }
        return null
    }

    private fun checkSizePath() {
        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        val boundWidth = bounds.width()
        val boundHeight = bounds.height()

        if (boundWidth < 100 || boundHeight < 100) {
            listener?.onSelectTooSmall()
            selectPath.reset()
            isDrawPathDone = false
        } else {
            //khi nay moi co bound path oke

            currentSizeSticker = max(bounds.width(), bounds.height()).toInt()
            originStickerSize = max(bounds.width(), bounds.height()).toInt()

            //bitmap add size = 1/5 min stickerSize
            val bitmapAddSize = min(bounds.width(), bounds.height()) / 5
            bitmapAdd = context.getDrawable(R.drawable.ic_add_orange)?.toBitmap(
                width = bitmapAddSize.toInt(),
                height = bitmapAddSize.toInt(),
                config = Bitmap.Config.ARGB_8888
            )

            //control button
            canDrawControlButton = true
        }
    }

    //check diem cham o goc phan tu thu nhat
    private fun checkFirstQuadrant() {
        if (currentPoint.x in 0f..width / 2f && currentPoint.y in 0f..height / 2f) {
            if (!isPreviewInFirstQuadrant) {
                isPreviewInFirstQuadrant = true
                listener?.onPreviewChange(true)
            }
        } else {
            if (isPreviewInFirstQuadrant) {
                isPreviewInFirstQuadrant = false
                listener?.onPreviewChange(false)
            }
        }
    }

    private fun getBitmapAroundPoint(radius: Float = 60f): Bitmap? {
        mBitmap ?: return null

        // Tính toán vùng cần crop
        val left = (currentPoint.x - radius).coerceAtLeast(0f)
        val top = (currentPoint.y - radius).coerceAtLeast(0f)
        val right = (currentPoint.x + radius).coerceAtMost(mBitmap?.width?.toFloat() ?: 0f)
        val bottom = (currentPoint.y + radius).coerceAtMost(mBitmap?.height?.toFloat() ?: 0f)

        val width = right - left
        val height = bottom - top

        // Cắt bitmap
        return Bitmap.createBitmap(
            mBitmap!!,
            left.toInt(),
            top.toInt(),
            width.toInt(),
            height.toInt()
        )
    }

    private fun isPointInsidePath(x: Float, y: Float): Boolean {
        // Bước 1: Tính bounding box của path
        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        // Bước 2: Tạo Region tương ứng với path
        val region = Region()
        val clip = Region(
            bounds.left.toInt(),
            bounds.top.toInt(),
            bounds.right.toInt(),
            bounds.bottom.toInt()
        )
        region.setPath(selectPath, clip)

        // Bước 3: Kiểm tra xem điểm (x, y) có nằm trong region không
        return region.contains(x.toInt(), y.toInt())
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onDrawSelect(bitmap: Bitmap)
        fun onPreviewChange(isFirstQuadrant: Boolean)
        fun onSelectTooSmall()
        fun onAddSticker()
    }
}