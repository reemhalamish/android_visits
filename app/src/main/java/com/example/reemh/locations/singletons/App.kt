package com.example.reemh.locations.singletons

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LocationsRepo.init(this)
    }
}