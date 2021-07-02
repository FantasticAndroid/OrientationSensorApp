package com.jio.tesseract.scannersdk

import android.app.Service
import android.content.Intent
import android.util.Log
import com.jio.tesseract.sensor.aidl.ISensorAidlInterface
import com.jio.tesseract.sensor.aidl.ISensorEventCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "SensorService"

/**
 * An AIDL service
 * @property sensorSdkManager SensorSdkManager
 * @property sensorAidlBinder SensorAidlBinder
 */
@AndroidEntryPoint
class SensorService : Service() {

    @Inject lateinit var sensorSdkManager: SensorSdkManager
    private lateinit var sensorAidlBinder: SensorAidlBinder

    override fun onCreate() {
        super.onCreate()
        sensorAidlBinder = SensorAidlBinder()
        try {
            sensorSdkManager.initSensorManager(sensorAidlBinder)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "SensorService: error: ${e.message}", e)
            // Hardware not compatible
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?) = sensorAidlBinder

    /**
     * Binder having Callbacks to provide sensor date to other apps.
     * @property callbackList HashSet<ISensorEventCallback>
     */
    class SensorAidlBinder : ISensorAidlInterface.Stub() {

        val callbackList: HashSet<ISensorEventCallback?> = HashSet()

        /**
         * Add Callback
         * @param callback ISensorEventCallback
         */
        override fun addOnEventTriggerCallback(callback: ISensorEventCallback?) {
            callback?.apply {
                callbackList.add(callback)
            }
        }
    }

    override fun onDestroy() {
        sensorSdkManager.stop()
        super.onDestroy()
    }
}