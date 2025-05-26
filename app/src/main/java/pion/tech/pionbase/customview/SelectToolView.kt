package pion.tech.pionbase.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import pion.tech.pionbase.util.DeviceDimensionsHelper.convertDpToPixel

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

    //bitmapBg
    private var bitmapBg: Bitmap? = null

    private val selectPaint = Paint()
    private val selectPath = Path()

    //tam cua path
    private val centerPaint = Paint().apply {
        color = Color.parseColor("#CC3A2B")
        style = Paint.Style.FILL
    }

    //bound cua path
    private val boundPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    init {
        selectPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = context.convertDpToPixel(2f)
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }
    }

    fun setBitmapBg(bg: Bitmap) {
        bitmapBg = bg
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawBg(mCanvas)
        drawSelectPath(mCanvas)
        drawCenter(mCanvas)
        drawBound(mCanvas)

        mBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        if (isTouch) {
            getBitmapAroundPoint()?.let {
                listener?.onDrawSelect(it)

            }
        }
    }

    private fun drawBg(canvas: Canvas) {
        bitmapBg?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    private fun drawSelectPath(canvas: Canvas) {
        canvas.drawPath(selectPath, selectPaint)
    }

    private fun drawCenter(canvas: Canvas) {
        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        // Tọa độ tâm
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()

        canvas.drawCircle(centerX, centerY, 10f, centerPaint)
    }

    private fun drawBound(canvas: Canvas) {
        val bounds = RectF()
        selectPath.computeBounds(bounds, true)

        canvas.drawRect(bounds, boundPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouch = true
                selectPath.reset()
                selectPath.moveTo(event.x, event.y)
                currentPoint.set(event.x, event.y)
                checkFirstQuadrant()
            }

            MotionEvent.ACTION_MOVE -> {
                isTouch = true
                selectPath.lineTo(event.x, event.y)
                currentPoint.set(event.x, event.y)
                checkFirstQuadrant()
                postInvalidate()
            }

            MotionEvent.ACTION_UP -> {
                isTouch = false
                selectPath.close()
                postInvalidate()
            }
        }
        return true
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

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

    interface Listener {
        fun onDrawSelect(bitmap: Bitmap)
        fun onPreviewChange(isFirstQuadrant: Boolean)
    }
}