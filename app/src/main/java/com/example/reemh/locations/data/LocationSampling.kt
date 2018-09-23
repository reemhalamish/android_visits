package com.example.reemh.locations.data

import java.lang.Math.*

data class LocationSampling(
        val lat: Double,
        val lng: Double,
        val accuracy: Double,
        val timestampMs: Long
)

data class Visit(
        val lat: Double,
        val lng: Double,
        val accuracy: Double,
        val timestampStartMs: Long,
        val timestampEndMs: Long?,
        val samples: MutableList<LocationSampling>
)

data class Event(
        val timestampMs: Long,
        val msg: String
)


data class LocationsContainer(
        var samples: MutableList<LocationSampling> = mutableListOf(),
        var visits: MutableList<Visit> = mutableListOf(),
        var events: MutableList<Event> = mutableListOf()
)




fun LocationSampling.distanceTo(other: LocationSampling): Double {
    val dLat = other.lat - this.lat
    val dLng = other.lng - this.lng


    return sqrt(pow(abs(dLat), 2.0) + pow(abs(dLng), 2.0))
}


// it will give 55 meters accuracy to the locations, according to
//   https://gis.stackexchange.com/questions/8650/measuring-accuracy-of-latitude-and-longitude
const val DELTA_SAME_LOCATION: Double = 0.0005

fun LocationSampling.isSamePlace(other: LocationSampling, delta: Double = DELTA_SAME_LOCATION)
 = distanceTo(other) <= delta

val Visit.isFinished get() = timestampEndMs != null

val Visit.durationMs get() = if (isFinished) timestampEndMs!! - timestampStartMs else Long.MAX_VALUE
internal val Visit.maxTimestamp get() = samples.map { it.timestampMs }.max()!!
internal val Visit.minTimestamp get() = samples.map { it.timestampMs }.min()!!