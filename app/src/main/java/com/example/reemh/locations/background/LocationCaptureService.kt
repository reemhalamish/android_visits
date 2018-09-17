package com.example.reemh.locations.background

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import com.example.reemh.locations.extensions.logd
import com.example.reemh.locations.singletons.LocationsRepo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*


class LocationCaptureService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    lateinit var locationClient: GoogleApiClient
    val locationRequest = LocationRequest()
    val isHighAccuracy = true
    var didStart = false


    override fun onCreate() {
        super.onCreate()
        LocationsRepo.addServiceStart(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logd("onStartCommand")
        if (didStart) {
            logd("already started. ignoring placing locations request")
        } else {
            initLocationsCapturing()
        didStart = true
        }

        return START_STICKY_COMPATIBILITY
    }

    override fun onBind(intent: Intent): IBinder? = null

    fun initLocationsCapturing() {
        logd("initLocationsCapturing")
        locationRequest.interval = 1000 * 60 * 5
        locationRequest.fastestInterval = 1000 * 60
        locationRequest.smallestDisplacement = 20f
        locationRequest.maxWaitTime = 1000 * 60 * 60 * 2
        locationRequest.priority =
                if (isHighAccuracy) LocationRequest.PRIORITY_HIGH_ACCURACY
                else LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY


        locationClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        locationClient.connect()
    }

    override fun onConnected(p0: Bundle?) {
        logd("Connected to Google API")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            logd("permission not granted")
            return
        }
        val client = LocationServices.getFusedLocationProviderClient(this) ?: kotlin.run {
            logd("no google location client!")
            return
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                val result = p0 ?: kotlin.run {
                    logd("no result")
                    return
                }

                LocationsRepo.addLocations(result.locations, this@LocationCaptureService)
            }

            // todo needed?
//            override fun onLocationAvailability(p0: LocationAvailability?) {
//                val avail = p0 ?: kotlin.run {
//                    logd("no LocationAvailability")
//                    return
//                }
//
//                avail.isLocationAvailable
//            }
        }

        client.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onConnectionSuspended(p0: Int) = logd("connection suspended")

    override fun onConnectionFailed(p0: ConnectionResult) = logd("connection failed")



    override fun onDestroy() {
        LocationsRepo.addServiceEnd(this)
        this.didStart = false
        super.onDestroy()

    }
}

