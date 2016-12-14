package com.example.user.geolocation10

import android.util.Log

/**
 * Created by User on 12/14/2016.
 */

class KalmanFilter(private val Q_metres_per_second: Float) {
    private val MinAccuracy = 1f
    var _TimeStamp: Long = 0
        private set
    var _lat: Double = 0.toDouble()
        private set
    var _lng: Double = 0.toDouble()
        private set
    private var variance: Float = 0.toFloat()

    init { variance = -1f }

    val _accuracy: Float
        get() = Math.sqrt(variance.toDouble()).toFloat()

    fun SetState(lat: Double, lng: Double, accuracy: Float, TimeStamp_milliseconds: Long) {
        this._lat = lat
        this._lng = lng
        variance = accuracy * accuracy
        this._TimeStamp = TimeStamp_milliseconds
    }

    fun Process(lat_measurement: Double, lng_measurement: Double, accuracy: Float, TimeStamp_milliseconds: Long) {
        var accuracy = accuracy
        if (accuracy < MinAccuracy) accuracy = MinAccuracy
        if (variance < 0) {

            this._TimeStamp = TimeStamp_milliseconds
            _lat = lat_measurement
            _lng = lng_measurement
            variance = accuracy * accuracy
        } else {

            val TimeInc_milliseconds = TimeStamp_milliseconds - this._TimeStamp
            if (TimeInc_milliseconds > 0) {
                variance += TimeInc_milliseconds.toFloat() * Q_metres_per_second * Q_metres_per_second / 1000
                this._TimeStamp = TimeStamp_milliseconds
            }

            val K = variance / (variance + accuracy * accuracy)

            _lat += K * (lat_measurement - _lat)
            _lng += K * (lng_measurement - _lng)

            variance = (1 - K) * variance
        }
    }
}
