package com.example.customcontroller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import java.lang.Integer.min
import kotlin.math.cos
import kotlin.math.sin

private enum class Speed(val label: Int) {
    OFF(R.string.off),
    LOW(R.string.low),
    MEDIUM(R.string.medium),
    HIGH(R.string.high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var radius = 0.0f
    private var speed = Speed.OFF
    private var speedLowColor = 0
    private var speedMediumColor = 0
    private var speedMaxColor = 0

    // position variable which will be used to draw label and indicator circle position
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            speedLowColor = getColor(R.styleable.DialView_color1, 0)
            speedMediumColor = getColor(R.styleable.DialView_color2, 0)
            speedMaxColor = getColor(R.styleable.DialView_color3, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = when (speed) {
            Speed.OFF -> Color.GRAY
            Speed.LOW -> speedLowColor
            Speed.MEDIUM -> speedMediumColor
            Speed.HIGH -> speedMaxColor
        }
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(speed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // Draw the text labels.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in Speed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
        paint.color = when (speed) {
            Speed.OFF -> Color.GRAY
            Speed.LOW -> speedLowColor
            Speed.MEDIUM -> speedMediumColor
            Speed.HIGH -> speedMaxColor
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        speed = speed.next()
        contentDescription = resources.getString(speed.label)
        invalidate()
        return true
    }

    private fun PointF.computeXYForSpeed(pos: Speed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }
}