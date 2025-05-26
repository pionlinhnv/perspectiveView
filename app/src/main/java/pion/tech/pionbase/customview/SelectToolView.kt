package pion.tech.pionbase.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import pion.tech.pionbase.util.DeviceDimensionsHelper.convertDpToPixel

class SelectToolView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
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

    init {
        selectPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = context.convertDpToPixel(2f)
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSelectPath(canvas)
        drawCenter(canvas)
        drawBound(canvas)
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
                selectPath.reset()
                selectPath.moveTo(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                selectPath.lineTo(event.x, event.y)
                postInvalidate()
            }

            MotionEvent.ACTION_UP -> {
                selectPath.close()
                postInvalidate()
            }
        }
        return true
    }
}