package com.jio.tesseract.sensor.aidl;

import com.jio.tesseract.sensor.aidl.SensorInfo;

interface ISensorEventCallback {
    void onSensorEventTrigger(in SensorInfo sensorInfo);
}
