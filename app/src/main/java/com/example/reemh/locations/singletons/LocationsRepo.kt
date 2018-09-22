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

        processNewSamples(context, addToSamples(newSamples))
    }

    fun addServiceStart(service: Service) {
        processNewSamples(service, addToSamples(listOf(LocationSampling(0.0,0.0,0.0, Date().time))))
    }

    fun addServiceEnd(service: Service) {
        processNewSamples(service, addToSamples(listOf(LocationSampling(100.0,100.0,100.0, Date().time))))
    }


    fun clearLocations(context: Context) {
        processNewSamples(context, mutableListOf())
    }

    private fun addToSamples(new: List<LocationSampling>): MutableList<LocationSampling> {
        val allSamples = samplesLiveData.value?.toMutableList() ?: mutableListOf() // copy
        allSamples.addAll(new)
        return allSamples
    }

    private fun processNewSamples(context: Context, samples: MutableList<LocationSampling>) {
        context.spPutString("locations", Gson().toJson(LocationSamplingContainer(samples)))
        samplesLiveData.postValue(samples.toMutableList()) // copy
    }

    // todo synchronized
    private fun makeNewVisitsIfExist(context: Context, samples: MutableList<LocationSampling>) {
        // iterate all samples, find samples that include location that were finished, fire "visit end" and delete samples
        // iterate all left samples, find samples of "visit start" and (if not fired already) fire "visit started"
    }
}