package com.example.user.geolocation10

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log


import com.google.android.gms.maps.model.LatLng


class LocationTrackingService : Service() {

    var locationManager: LocationManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i("MessageTag","Service started")
        return START_STICKY
    }

    override fun onCreate() {
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    val TAG = "LocationTrackingService"

    val INTERVAL = 1000.toLong() // In milliseconds
    val DISTANCE = 10.toFloat() // In meters

    val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER, this),
            LTRLocationListener(LocationManager.NETWORK_PROVIDER, this)
    )

    class LTRLocationListener(provider: String, context: Context) : android.location.LocationListener {

        val lastLocation = Location(provider)
        val thisContext =context

        override fun onLocationChanged(location: Location?) {
            //    Log.i("MessageTag","Location changed")
            var latitude=location!!.latitude
            var longitude=location!!.longitude
            var speed=location!!.speed
            var accuracy=location!!.accuracy
            var time=location!!.time
            //    Log.d("Time Speed Accuracy", time.toString()+" "+speed.toString()+" "+accuracy.toString())
            var filter= KalmanFilter(speed)
            filter.SetState(lastLocation.latitude, lastLocation.longitude, lastLocation.accuracy, lastLocation.time)
            filter.Process(location!!.latitude, location!!.longitude, location!!.accuracy, location!!.time)
            sendMessageToActivity(LatLng(filter._lat, filter._lng), LatLng(latitude, longitude), "Location")
            lastLocation.set(location)
        }

        override fun onProviderDisabled(provider: String?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
        fun sendMessageToActivity(l1: LatLng, l2: LatLng, msg: String) {
            val intent = Intent("GPSLocationUpdates")
            // You can also include some extra data.
            intent.putExtra("Status", msg)
            val b = Bundle()
            b.putParcelable("LatLng 1", l1)
            b.putParcelable("LatLng 2", l2)
            intent.putExtra("Location", b)
            LocalBroadcastManager.getInstance(thisContext).sendBroadcast(intent)
        }
    }


}