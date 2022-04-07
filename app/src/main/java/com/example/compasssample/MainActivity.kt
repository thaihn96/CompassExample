package com.example.compasssample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var sensorManager: SensorManager? = null
    private var magneticSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    var azimut: Float = 0F
    var degree: Float = 0F
    var customDrawableView: CustomDrawableView? = null

    inner class CustomDrawableView(context: Context?) : View(context) {
        var paint = Paint()
        override fun onDraw(canvas: Canvas) {
            val width = width
            val height = height
            val centerx = width / 2
            val centery = height / 2
            canvas.drawLine(centerx.toFloat(), 0f, centerx.toFloat(), height.toFloat(), paint)
            canvas.drawLine(0f, centery.toFloat(), width.toFloat(), centery.toFloat(), paint)
            // Rotate the canvas with the azimut
            if (azimut != null) canvas.rotate(
                -azimut * 360 / (2 * Math.PI).toFloat(),
                centerx.toFloat(),
                centery.toFloat()
            )
            paint.color = -0xffff01
            canvas.drawLine(centerx.toFloat(), -1000f, centerx.toFloat(), +1000f, paint)
//            canvas.drawLine(-1000f, centery.toFloat(), 1000f, centery.toFloat(), paint)
            canvas.drawText("N", (centerx + 100).toFloat(), (centery - 100).toFloat(), paint)
            canvas.drawText("S", (centerx - 100).toFloat(), (centery + 150).toFloat(), paint)
            paint.color = -0xff0100
        }

        init {
            paint.color = -0xff0100
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.isAntiAlias = true
        }
    }

    private val listener: SensorEventListener = object : SensorEventListener {
        var mGravity: FloatArray = FloatArray(3)
        var mGeomagnetic: FloatArray = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values
            } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = event.values
            }

            if (mGravity != null && mGeomagnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    azimut = orientation[0] // orientation contains: azimut, pitch and roll
                }
            }
            degree = Math.toDegrees(azimut.toDouble()).toFloat()
            customDrawableView?.invalidate()
        }

        override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customDrawableView = CustomDrawableView(this)
        setContentView(customDrawableView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            listener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager?.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(listener)
    }
}
