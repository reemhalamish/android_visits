package com.example.reemh.locations.extensions

import android.content.Context
import android.preference.PreferenceManager

fun Context.spGetString(key: String, default: String) = PreferenceManager.getDefaultSharedPreferences(this).getString(key, default)
fun Context.spPutString(key: String, value: String) = PreferenceManager.getDefaultSharedPreferences(this).edit().putString(key, value).apply()