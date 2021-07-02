package com.jio.tesseract.scannersdk

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jio.tesseract.scannersdk.databinding.ActivityMainBinding
import com.jio.tesseract.sensor.aidl.ISensorAidlInterface
import com.jio.tesseract.sensor.aidl.ISensorEventCallback
import com.jio.tesseract.sensor.aidl.SensorInfo

private const val TAG = "MainActivity"
private const val AIDL_SERVICE_ACTION = "Jio.Tesseract.SensorService"
private const val AIDL_SERVICE_PACKAGE = "com.jio.tesseract.scannersdk"

/***
 *
 * @property sensorServiceInterface ISensorAidlInterface?
 * @property binding ActivitySampleBinding
 * @property aidlConnection ServiceConnection
 */
class MainActivity : AppCompatActivity() {

    private var sensorServiceInterface: ISensorAidlInterface? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindSensorService()
    }

    private fun bindSensorService() {
        val intent = Intent().apply {
            action = AIDL_SERVICE_ACTION
            setPackage(AIDL_SERVICE_PACKAGE)
        }
        bindService(intent, aidlConnection, Service.BIND_AUTO_CREATE)
    }

    private val aidlConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: name: $name")
            sensorServiceInterface = ISensorAidlInterface.Stub.asInterface(service)
            sensorServiceInterface?.addOnEventTriggerCallback(object : ISensorEventCallback.Stub() {
                override fun onSensorEventTrigger(sensorInfo: SensorInfo?) {
                    sensorInfo?.apply {
                        binding.valuesTv.post {
                            binding.valuesTv.text = this.toString()
                        }
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    override fun onDestroy() {
        try {
            unbindService(aidlConnection)
        } finally {
            super.onDestroy()
        }
    }


}