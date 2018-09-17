package com.example.reemh.locations.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.example.reemh.locations.background.LocationCaptureService
import com.example.reemh.locations.R
import com.example.reemh.locations.singletons.LocationsRepo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
companion object {
    const val REQUEST_FINE_LOCATION = 1
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = LocationSamplesAdapter(this)

        btnClear.setOnClickListener { LocationsRepo.clearLocations(this) }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_FINE_LOCATION)
            return
        } else {
            ContextCompat.startForegroundService(this, Intent(this, LocationCaptureService::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != REQUEST_FINE_LOCATION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Can't operate without locations permission", Toast.LENGTH_SHORT).show()
            return
        }

        startService(Intent(this, LocationCaptureService::class.java))
    }
}
