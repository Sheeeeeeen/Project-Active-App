package ph.project.active.ui.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidShakeSource(context: Context) : ShakeSource {
    override val shakes: Flow<Unit> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            awaitClose { }
            return@callbackFlow
        }
        val detector = ShakeDetector(
            threshold = 12.0,
            window = 500L,
            minHits = 3,
            cooldown = 1_500L,
        )
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val shook = detector.onSample(
                    x = event.values[0].toDouble(),
                    y = event.values[1].toDouble(),
                    z = event.values[2].toDouble(),
                    timestamp = event.timestamp / 1_000_000L,
                )
                if (shook) trySend(Unit)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
