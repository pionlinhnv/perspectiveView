package pion.tech.pionbase.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
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

    private val bitmapResult: Bitmap? = null

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
    }

    private fun drawSelectPath(canvas: Canvas) {
        canvas.drawPath(selectPath, selectPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
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