package com.example.reemh.locations.extensions

import android.text.format.DateUtils
import android.util.Log

fun Any.logd(msg: String) { Log.d(this.javaClass.simpleName, msg) }

fun Long.timestampMsToHumanReadable(): String {
    return DateUtils.getRelativeTimeSpanString(this).toString()
}