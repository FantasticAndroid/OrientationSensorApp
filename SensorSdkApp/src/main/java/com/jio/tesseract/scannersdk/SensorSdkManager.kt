package com.jio.tesseract.scannersdk

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.jio.tesseract.scannersdk.SensorService.SensorAidlBinder
import com.jio.tesseract.sensor.aidl.SensorInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.round

private const val TAG = "SensorSdkManager"
private const val SENSOR_EMIT_INTERVAL = 8000000 // 8 milliseconds in microseconds

/**
 * Service scoped Manager to sense Rotation and dispatch sensorInfo to Callback(ISensorEventCallback)
 * @property context Context
 * @property job Job
 * @property coroutineContext CoroutineContext
 * @property exceptionHandler CoroutineExceptionHandler
 * @property rotationSensor Sensor?
 * @property sensorManager SensorManager?
 * @property sensorAidlBinder SensorAidlBinder?
 * @constructor
 */
@ServiceScoped
class SensorSdkManager @Inject constructor(@ApplicationContext private val context: Context) : SensorEventListener,
    CoroutineScope {

    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + exceptionHandler

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "${exception.message} handled in exceptionHandler!", exception)
    }

    private var rotationSensor: Sensor? = null
    private var sensorManager: SensorManager? = null
    private var sensorAidlBinder: SensorAidlBinder? = null

    @Throws(Exception::class)
    fun initSensorManager(sensorAidlBinder: SensorAidlBinder) {
        this.sensorAidlBinder = sensorAidlBinder
        sensorManager = context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager?
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager?.registerListener(this, rotationSensor, SENSOR_EMIT_INTERVAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "onSensorChanged: event: ${event?.sensor?.name}")

        if ((null != event && event.sensor == rotationSensor) && (null != sensorAidlBinder && sensorAidlBinder!!.callbackList.isNotEmpty())) {
            launch(Dispatchers.Main) {
                val sensorInfo = readSensorEvent(event)
                for (callback in sensorAidlBinder!!.callbackList) {
                    callback?.onSensorEventTrigger(sensorInfo)
                }
            }
        }
    }

    /**
     *
     * @param sensorEvent SensorEvent
     * @return SensorInfo
     */
    private suspend fun readSensorEvent(sensorEvent: SensorEvent): SensorInfo =
        coroutineScope {
            withContext(coroutineContext) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values)
                val adjustedRotationMatrix = FloatArray(9)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    adjustedRotationMatrix
                )
                val orientationAngles = FloatArray(3)

                val orientation = SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles)
                val degree = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
                val angle = round(degree * 100) / 100
                val sensorInfo = SensorInfo(orientation, degree, angle)
                Log.d(TAG, "readSensorEvent: sensorInfo: $sensorInfo")
                sensorInfo
            }
        }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun stop() {
        job.cancel()
        sensorManager?.unregisterListener(this, rotationSensor)
    }
}