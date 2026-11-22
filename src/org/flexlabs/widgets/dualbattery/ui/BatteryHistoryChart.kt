package org.flexlabs.widgets.dualbattery.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.flexlabs.widgets.dualbattery.R
import org.flexlabs.widgets.dualbattery.storage.BatteryHistory
import org.flexlabs.widgets.dualbattery.storage.ChartPoint
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class BatteryHistoryChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = density
    }
    private val mainPaint = seriesPaint(Color.GREEN)
    private val dockPaint = seriesPaint(Color.CYAN)
    private val dateFormat = DateFormat.getDateInstance()
    private var history = BatteryHistory(emptyList(), emptyList())
    private var fullStart = 0.0
    private var fullEnd = 1.0
    private var visibleStart = 0.0
    private var visibleEnd = 1.0
    private var lastTouchX = 0f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val oldDuration = visibleEnd - visibleStart
                val fullDuration = fullEnd - fullStart
                val newDuration = (oldDuration / detector.scaleFactor)
                    .coerceIn(TimeUnit.HOURS.toMillis(1).toDouble(), fullDuration)
                val anchor = timeAt(detector.focusX)
                val ratio = (anchor - visibleStart) / oldDuration
                visibleStart = anchor - ratio * newDuration
                visibleEnd = visibleStart + newDuration
                clampVisibleRange()
                invalidate()
                return true
            }
        })

    @JvmOverloads
    fun setData(history: BatteryHistory, days: Int, now: Long = System.currentTimeMillis()) {
        this.history = history
        fullEnd = now.toDouble()
        fullStart = now.toDouble() - TimeUnit.DAYS.toMillis(days.toLong())
        visibleStart = fullStart
        visibleEnd = fullEnd
        contentDescription = resources.getQuantityString(
            R.plurals.battery_history_description, days, days)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = 48f * density
        val right = width - 16f * density
        val top = 32f * density
        val bottom = height - 32f * density
        if (right <= left || bottom <= top) return

        for (step in 0..4) {
            val y = bottom - (bottom - top) * step / 4f
            canvas.drawLine(left, y, right, y, gridPaint)
            canvas.drawText("${step * 25}", 4f * density, y + 4f * density, textPaint)
        }

        drawDateLabel(canvas, visibleStart.toLong(), left, bottom + 20f * density, Paint.Align.LEFT)
        drawDateLabel(canvas, (visibleStart + (visibleEnd - visibleStart) / 2).toLong(),
            (left + right) / 2f, bottom + 20f * density, Paint.Align.CENTER)
        drawDateLabel(canvas, visibleEnd.toLong(), right, bottom + 20f * density, Paint.Align.RIGHT)

        textPaint.color = Color.GREEN
        canvas.drawText(getContext().getString(R.string.battery_main), left, 18f * density, textPaint)
        if (history.dock.isNotEmpty()) {
            val mainWidth = textPaint.measureText(getContext().getString(R.string.battery_main))
            textPaint.color = Color.CYAN
            canvas.drawText(getContext().getString(R.string.battery_dock),
                left + mainWidth + 24f * density, 18f * density, textPaint)
        }
        textPaint.color = Color.LTGRAY

        if (history.main.isEmpty() && history.dock.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(getContext().getString(R.string.battery_history_empty),
                (left + right) / 2f, (top + bottom) / 2f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            return
        }

        drawSeries(canvas, history.main, mainPaint, left, right, top, bottom)
        drawSeries(canvas, history.dock, dockPaint, left, right, top, bottom)
    }

    private fun drawSeries(
        canvas: Canvas,
        points: List<ChartPoint>,
        paint: Paint,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float
    ) {
        if (points.isEmpty()) return
        val path = Path()
        var started = false
        for (point in points) {
            val x = left + ((point.time - visibleStart) / (visibleEnd - visibleStart)).toFloat() * (right - left)
            val y = bottom - point.level.coerceIn(0, 100) / 100f * (bottom - top)
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }
        val saveCount = canvas.save()
        canvas.clipRect(left, top, right, bottom)
        canvas.drawPath(path, paint)
        canvas.restoreToCount(saveCount)
    }

    private fun drawDateLabel(canvas: Canvas, time: Long, x: Float, y: Float, align: Paint.Align) {
        textPaint.textAlign = align
        canvas.drawText(dateFormat.format(Date(time)), x, y, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> lastTouchX = event.x
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    val plotWidth = width - 64f * density
                    if (plotWidth > 0) {
                        val offset = (lastTouchX - event.x) * (visibleEnd - visibleStart) / plotWidth
                        visibleStart += offset
                        visibleEnd += offset
                        clampVisibleRange()
                        invalidate()
                    }
                }
                lastTouchX = event.x
            }
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun timeAt(x: Float): Double {
        val left = 48f * density
        val right = width - 16f * density
        val fraction = ((x - left) / (right - left)).coerceIn(0f, 1f)
        return visibleStart + fraction * (visibleEnd - visibleStart)
    }

    private fun clampVisibleRange() {
        val duration = visibleEnd - visibleStart
        if (visibleStart < fullStart) {
            visibleStart = fullStart
            visibleEnd = fullStart + duration
        }
        if (visibleEnd > fullEnd) {
            visibleEnd = fullEnd
            visibleStart = fullEnd - duration
        }
    }

    private fun seriesPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
}
