package com.jio.tesseract.sensor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SensorInfo(val orientation: FloatArray, val degree: Double, val angle: Double) : Parcelable {

    override fun toString(): String {
        return "SensorInfo (orientation = ${orientation.contentToString()}, degree = $degree, angle = $angle)"
    }
}
