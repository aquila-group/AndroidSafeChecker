package com.android.cs.checkrom.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun checkBluetooth(): Boolean =
    !Runtime.getRuntime().exec("getprop")
        .inputStream.bufferedReader()
        .readText()
        .lines()
        .any { line ->
            val match = """\[([^]]+)]""".toRegex().find(line)
            match != null && match.groupValues[1].contains("bluetooth", true)
        }

fun cameraCheck(context: Context): Boolean {
    return !context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}