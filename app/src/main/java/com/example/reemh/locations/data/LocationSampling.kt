package com.example.reemh.locations.data

data class LocationSampling(
        val lat: Double,
        val lng: Double,
        val accuracy: Double,
        val timestampMs: Long
)

data class LocationSamplingContainer(
        val samples: MutableList<LocationSampling> = mutableListOf()
)