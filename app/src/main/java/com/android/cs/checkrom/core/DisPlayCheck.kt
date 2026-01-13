package com.android.cs.checkrom.core

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.android.cs.checkrom.utils.SystemPropUtils


private fun getScreenInfo(context: Context): Int {
    val mDisplayMetrics = context.resources.displayMetrics
    val width = mDisplayMetrics.widthPixels
    return width
}

private fun getScreenInfo1(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val wm = windowManager.defaultDisplay.mode
    return wm.physicalWidth
}

fun checkScreenInfo(context: Context): Boolean {
    return getScreenInfo(context) != getScreenInfo1(context)
}

private fun getScreenDensity(): Int {
    val prop = SystemPropUtils.getProp("ro.sf.lcd_density")
    if (prop.isNotEmpty()) {
        return prop.toInt()
    }
    return -1
}

private fun getScreenDensity1(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val mDisplayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
    return mDisplayMetrics.densityDpi
}

fun checkScreenDensity(context: Context): Boolean {
    return getScreenDensity() != getScreenDensity1(context)
}

private fun getCurrentDpi(context: Context): Int {
    return context.resources.displayMetrics.densityDpi
}

private fun getConfigDpi(context: Context): Int {
    return context.resources.configuration.densityDpi
}

private fun getPhysicalDpi(context: Context): Int {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        metrics.densityDpi
    } catch (e: Exception) {
        -1
    }
}

fun checkAllDensityInfo(context: Context): Boolean {
    val currentDpi = getCurrentDpi(context)
    val configDpi = getConfigDpi(context)
    val physicalDpi = getPhysicalDpi(context)
    /*Log.e(
        TAG, """
            |===== 屏幕密度信息 @ Android  =====
            |当前显示DPI (DisplayMetrics): $currentDpi
            |配置DPI (Configuration): $configDpi
            |物理DPI (SystemProperty): $physicalDpi
            |===================================
        """.trimMargin()
    )*/
    return currentDpi != configDpi || configDpi != physicalDpi
}

