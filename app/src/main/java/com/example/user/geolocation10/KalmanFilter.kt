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
    private var variance: Float = 0.toFloat() // P matrix.  Negative means object uninitialised.  NB: units irrelevant, as long as same units used throughout

    init {
        variance = -1f
    }

    val _accuracy: Float
        get() = Math.sqrt(variance.toDouble()).toFloat()

    fun SetState(lat: Double, lng: Double, accuracy: Float, TimeStamp_milliseconds: Long) {
        this._lat = lat
        this._lng = lng
        variance = accuracy * accuracy
        this._TimeStamp = TimeStamp_milliseconds
    }

    /// <summary>
    /// Kalman filter processing for lattitude and longitude
    /// </summary>
    /// <param name="lat_measurement_degrees">new measurement of lattidude</param>
    /// <param name="lng_measurement">new measurement of longitude</param>
    /// <param name="accuracy">measurement of 1 standard deviation error in metres</param>
    /// <param name="TimeStamp_milliseconds">time of measurement</param>
    /// <returns>new state</returns>
    fun Process(lat_measurement: Double, lng_measurement: Double, accuracy: Float, TimeStamp_milliseconds: Long) {
        var accuracy = accuracy
        if (accuracy < MinAccuracy) accuracy = MinAccuracy
        if (variance < 0) {
            // if variance < 0, object is unitialised, so initialise with current values
            this._TimeStamp = TimeStamp_milliseconds
            _lat = lat_measurement
            _lng = lng_measurement
            variance = accuracy * accuracy
        } else {
            // else apply Kalman filter methodology

            //    Log.d("Geo Filter","filter")
            val TimeInc_milliseconds = TimeStamp_milliseconds - this._TimeStamp
            if (TimeInc_milliseconds > 0) {
                // time has moved on, so the uncertainty in the current position increases
                variance += TimeInc_milliseconds.toFloat() * Q_metres_per_second * Q_metres_per_second / 1000
                this._TimeStamp = TimeStamp_milliseconds
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
            val K = variance / (variance + accuracy * accuracy)
            // apply K
            _lat += K * (lat_measurement - _lat)
            _lng += K * (lng_measurement - _lng)
            // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance
            variance = (1 - K) * variance
        }
    }
}
