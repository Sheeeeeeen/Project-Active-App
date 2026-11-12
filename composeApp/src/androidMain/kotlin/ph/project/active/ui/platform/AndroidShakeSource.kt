package ph.project.active.ui.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AndroidShakeSource(context: Context) : ShakeSource {
    private val detector = ShakeDetector(
        threshold = 12.0,
        window = 500L,
        minHits = 3,
        cooldown = 1_500L,
    )
    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    override val shakes: Flow<Unit> = events.asSharedFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val shook = detector.onSample(
                x = event.values[0].toDouble(),
                y = event.values[1].toDouble(),
                z = event.values[2].toDouble(),
                timestamp = event.timestamp / 1_000_000L,
            )
            if (shook) events.tryEmit(Unit)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    init {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }
}
