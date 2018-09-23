package com.example.reemh.locations.extensions

import android.text.format.DateUtils
import android.util.Log
import java.util.concurrent.TimeUnit

fun Any.logd(msg: String) { Log.d(this.javaClass.simpleName, msg) }

fun Long.timestampMsToHumanReadable(): String {
    return DateUtils.getRelativeTimeSpanString(this).toString()
}

fun Long.durationMsToHumanReadable(): String {
    return String.format("%dH %dM %dS",
            TimeUnit.MILLISECONDS.toHours(this),
            TimeUnit.MILLISECONDS.toMinutes(this) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this)),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this)));

}