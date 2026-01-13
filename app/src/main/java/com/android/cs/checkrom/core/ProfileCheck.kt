package com.android.cs.checkrom.core

import android.os.Build
import android.util.Log
import com.android.cs.checkrom.utils.NativeUtils
import com.android.cs.checkrom.utils.ReflectionUtils

fun checkEf(): Boolean {
    val str = NativeUtils.runTimeExec("ls /dev/socket")
//    val str1 = EmulatorUtils.runTimeExec("getprop gsm.version.baseband")
    Log.e("TAG", "str: ${str} ${isAdbEnabledBySettings()}")
    return str.contains("system_server") ||
            str.contains("root") ||
            str.contains("zygote") ||
            str.contains("init") ||
            str.contains("keystore")
}

fun checkSystemFeature(): Boolean {
    val str = NativeUtils.runTimeExec("ls /system/etc/permissions")
//    Log.e("TAG", "str: ${str}")
    //rockchip.software.display.xml
    return str.contains("rockchip") ||
            str.contains("rk3588")
}

fun isAdbEnabledBySettings(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("settings get global adb_enabled")
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        output == "1"
    } catch (e: Exception) {
        false
    }
}

fun checkIsRomUnsecure(): Boolean {
    try {
        val obj: Any = ReflectionUtils.getStaticFieldValue(Build::class.java, "IS_ENG")
        if (obj is Boolean) {
            if (obj) {
                return true
            }
        }
    } catch (e: java.lang.Exception) {
    }

    try {
        val obj: Any = ReflectionUtils.getStaticFieldValue(Build::class.java, "IS_USERDEBUG")
        if (obj is Boolean) {
            if (obj) {
                return true
            }
        }
    } catch (e: java.lang.Exception) {
    }

    try {
        val obj: Any = ReflectionUtils.getStaticFieldValue(Build::class.java, "IS_DEBUGGABLE")
        if (obj is Boolean) {
            if (obj) {
                return true
            }
        }
    } catch (e: java.lang.Exception) {
    }

    return false
}