package com.example.reemh.locations.singletons

import android.app.Application
import android.app.Service
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import com.example.reemh.locations.data.LocationSampling
import com.example.reemh.locations.data.LocationSamplingContainer
import com.example.reemh.locations.extensions.spGetString
import com.example.reemh.locations.extensions.spPutString
import com.google.gson.Gson
import java.util.*

object LocationsRepo {
    private val samplesLiveData = MutableLiveData<MutableList<LocationSampling>>()

    val samples: LiveData<List<LocationSampling>>
    get() = samplesLiveData as LiveData<List<LocationSampling>>

    fun init(context: Application) {
        val json = context.spGetString("locations", "{}")
        val container = Gson().fromJson(json, LocationSamplingContainer::class.java)
        samplesLiveData.postValue(container.samples)
    }

    fun addLocations(locations: List<Location>, context: Context) {
        val newSamples = locations
                .map {
                    LocationSampling(
                            lat = it.latitude,
                            lng = it.longitude,
                            accuracy = it.accuracy.toDouble(),
                            timestampMs = it.time
                    )
                }
                .filter { it.accuracy <= 100 }

        val allSamples = samplesLiveData.value ?: mutableListOf()
        allSamples.addAll(newSamples)

        processNewSamples(context, allSamples)
    }

    fun addServiceStart(service: Service) {
        val allSamples = samplesLiveData.value ?: mutableListOf()
        allSamples.add(LocationSampling(.0,.0,.0, Date().time))
        processNewSamples(service, allSamples)
    }

    fun addServiceEnd(service: Service) {
        val allSamples = samplesLiveData.value ?: mutableListOf()
        allSamples.add(LocationSampling(100.0,100.0,.0, Date().time))
        processNewSamples(service, allSamples)
    }


    fun clearLocations(context: Context) {
        processNewSamples(context, mutableListOf())
    }

    private fun processNewSamples(context: Context, samples: MutableList<LocationSampling>) {
        context.spPutString("locations", Gson().toJson(LocationSamplingContainer(samples)))
        samplesLiveData.postValue(samples)
    }
}