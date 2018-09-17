package com.example.reemh.locations.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.reemh.locations.singletons.LocationsRepo
import com.example.reemh.locations.data.LocationSampling
import com.example.reemh.locations.R
import com.example.reemh.locations.extensions.timestampMsToHumanReadable
import kotlinx.android.synthetic.main.item_location.view.*

class LocationSamplesAdapter(lifecycle: LifecycleOwner): RecyclerView.Adapter<LocationSamplesAdapter.SamplesHolder>() {

    var list: List<LocationSampling> = listOf()
    init {
        LocationsRepo.samples.observe(lifecycle, Observer { it?.let { newList ->
            val oldList = this.list
            this.list = newList
            val diff = DiffUtil.calculateDiff(DiffCallback(oldList, newList))
            diff.dispatchUpdatesTo(this)
        } })
    }


    inner class SamplesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val text = itemView.mainText
    }

    inner class DiffCallback(val oldList: List<LocationSampling>, val newList: List<LocationSampling>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
            return oldList[p0].timestampMs == newList[p1].timestampMs
        }

        override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
            return oldList[p0] == newList[p1]
        }
    }

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int)
    = SamplesHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_location, p0, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: SamplesHolder, p1: Int) {
        val sample = list[p1]
        p0.text.text = """
            accuracy: ${sample.accuracy}
            lat: ${sample.lat}
            lng: ${sample.lng}
            time: ${sample.timestampMs.timestampMsToHumanReadable()}
            timestamp: ${sample.timestampMs}
            """.trimIndent()
    }
}