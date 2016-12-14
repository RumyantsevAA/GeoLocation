package com.example.user.geolocation10


import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import com.example.user.geolocation10.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.example.user.geolocation10.R.layout.activity_maps
import com.google.android.gms.maps.model.*
import android.graphics.Bitmap
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.IntentFilter
import android.graphics.Canvas
import android.view.View.MeasureSpec
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.ImageView
import com.example.user.geolocation10.R.styleable.View
import java.util.*
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLng
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import java.security.Security.getProvider






class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var point: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d("MessageTag","Here5!")
        ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        startService(Intent(baseContext, LocationTrackingService::class.java))
        val mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Get extra data included in the Intent
                val message = intent.getStringExtra("Status")
                val b = intent.getBundleExtra("Location")
                var point2 = b.getParcelable<Parcelable>("LatLng") as LatLng
                if(point!=null) {
                    var options= PolylineOptions()
                            .add(point, LatLng(point2.latitude, point2.longitude))
                            .width(5f)
                            .color(Color.RED)
                    var line = mMap!!.addPolyline(options)
                }
                point=point2
            }
        }
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(
                mMessageReceiver, IntentFilter("GPSLocationUpdates"));
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(point2: LatLng) {
                var filter = KalmanFilter(0.toFloat())
                if (point != null) {
                    filter.SetState(point!!.latitude, point!!.longitude, 20.toFloat(), 1000)
                    filter.Process(point2.latitude, point2.longitude, 20.toFloat(), 1000)
                    var options = PolylineOptions()
                            .add(point, LatLng(filter._lat, filter._lng))
                            .width(5f)
                            .color(Color.RED)
                    point = LatLng(filter._lat, filter._lng)
                    var line = mMap!!.addPolyline(options)
                } else {
                    point = point2
                }
            }
        })
    /*    mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(point2: LatLng) {
                if (point != null) {
                    var options = PolylineOptions()
                            .add(point, point2)
                            .width(5f)
                            .color(Color.RED)
                    point = point2
                    var line = mMap!!.addPolyline(options)
                } else {
                    point = point2
                }
            }
        })  */
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}