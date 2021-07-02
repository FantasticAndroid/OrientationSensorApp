package com.jio.tesseract.sensor.aidl;
import com.jio.tesseract.sensor.aidl.ISensorEventCallback;

interface ISensorAidlInterface {
    void addOnEventTriggerCallback(in ISensorEventCallback callback);
}