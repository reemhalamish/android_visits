package com.example.reemh.locations.singletons

import android.app.Application
import android.app.Service
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import com.example.reemh.locations.data.*
import com.example.reemh.locations.extensions.spGetString
import com.example.reemh.locations.extensions.spPutString
import com.google.gson.Gson
import java.util.*
import kotlin.math.min

object LocationsRepo {
    private val samplesLiveData = MutableLiveData<List<LocationSampling>>()
    val samples: LiveData<List<LocationSampling>>
        get() = samplesLiveData

    private val eventsLiveData = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = eventsLiveData

    private val visitsLiveData = MutableLiveData<List<Visit>>()
    val visits: LiveData<List<Visit>>
        get() = visitsLiveData


    fun init(context: Application) {
        val container = currentContainer(context)
        samplesLiveData.postValue(container.samples)
        visitsLiveData.postValue(container.visits)
        eventsLiveData.postValue(container.events)

        samplesToVisits(context, container, container.samples)
        updateContainer(context, container)
        visitsLiveData.postValue(container.visits.toList()) // copy
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
        val container = currentContainer(service)
        container.events.add(Event(Date().time, "started service"))
        updateContainer(service, container)
        eventsLiveData.postValue(container.events)
    }

    fun addServiceEnd(service: Service) {
        val container = currentContainer(service)
        container.events.add(Event(Date().time, "destroyed service"))
        updateContainer(service, container)
        eventsLiveData.postValue(container.events)
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
        val container = currentContainer(context)
        samplesToVisits(context, container, samples)

        updateContainer(context, container)

        samplesLiveData.postValue(container.samples.toList()) // copy
        visitsLiveData.postValue(container.visits.toList()) // copy
    }

    // todo synchronized
    private fun samplesToVisits(context: Context, container: LocationsContainer, samples: MutableList<LocationSampling>) {
        if (samples.isEmpty()) return

        samples.sortBy { it.timestampMs }

        // iterate all samples, find samples that include location that were finished, fire "visit end" and delete samples
        // iterate all left samples, find samples of "visit start" and (if not fired already) fire "visit started"
        val newVisits = mutableListOf<Visit>()

        val firstSample = samples.firstOrNull() ?: return // nothing to do if no sasmples
        var currentVisit = Visit(firstSample.lat, firstSample.lng, firstSample.accuracy, firstSample.timestampMs, null, mutableListOf(firstSample))
        var previousSample = firstSample

        samples.drop(1).forEach { nextSample ->

            if (previousSample.isSamePlace(nextSample)) {
                currentVisit.samples.add(nextSample)
                currentVisit = currentVisit.copy(accuracy = min(currentVisit.accuracy, nextSample.accuracy))
            }


            else { // a sample from a different place
                newVisits.add(currentVisit)
                currentVisit = Visit(nextSample.lat, nextSample.lng, nextSample.accuracy, nextSample.timestampMs, null, mutableListOf(nextSample))
            }

            previousSample = nextSample // for next iteration
        }

        newVisits.add(currentVisit)
        val firstUpdate = newVisits.map { it.copy(timestampEndMs = it.maxTimestamp) }.toMutableList()
        val lastVisit = firstUpdate.last()
        firstUpdate.remove(lastVisit)
        firstUpdate.add(lastVisit.copy(timestampEndMs = null)) // last visit is always ongoing

        val DELTA_MINIMUM_VISIT: Long = 1000 * 60 * 3
        val DELTA_MINIMUM_VISIT_FINISHED: Long = 1000 * 60 * 10
        val updated = firstUpdate
                .filter {
                    it.maxTimestamp - it.minTimestamp > DELTA_MINIMUM_VISIT
                }.toMutableList()


        container.visits = updated.toMutableList()
    }

    private fun currentContainer(context: Context) = Gson().fromJson(context.spGetString("locations", "{}"), LocationsContainer::class.java)
    private fun updateContainer(context: Context, container: LocationsContainer) = context.spPutString("locations", Gson().toJson(container))
}