package com.android.cs.checkrom.core

import android.text.TextUtils
import com.android.cs.checkrom.utils.NativeUtils
import com.android.cs.checkrom.utils.SystemPropUtils


private fun checkTcpPort(): Boolean {
    val prop = SystemPropUtils.getProp("service.adb.tcp.port")
    val prop1 = NativeUtils.runTimeExec("getprop service.adb.tcp.port").trim()
    return "5555" != prop || "5555" != prop1
}

private fun checkRomVersion(): Boolean {
    val prop1 = SystemPropUtils.getProp("ro.build.version.release")
    val prop2 = SystemPropUtils.getProp("ro.product.build.fingerprint")
    val prop3 = NativeUtils.runTimeExec("getprop ro.build.version.release").trim()
    val prop4 = NativeUtils.runTimeExec("getprop ro.product.build.fingerprint").trim()
    return !prop2.contains(prop1) || !prop4.contains(prop3) || prop1 != prop3 || !prop2.equals(prop4)
}

fun checkProp(): Boolean {
    return readProp() || checkTcpPort() || checkRomVersion()
}

fun readProp(): Boolean {
    val propItems: Set<String> = HashSet(
        mutableListOf(
            "ro.build.hardware.version"
        )
    )
    propItems.forEach {
        val prop = SystemPropUtils.getProp(it)
        if (TextUtils.isEmpty(prop)) {
            return true
        }
    }
    return false
}

fun checkVirtual(): Boolean {
    val properties = NativeUtils.getAllProperties1() ?: return false
    val vmKeywords = listOf(
        "rockchip", "rk3588", "virtualbox", "vmware", "qemu",
        "genymotion", "nox", "bluestacks", "ldplayer", "mumu",
        "android-x86", "android_ia", "android_x86", "sdk_google",
        "goldfish", "ranchu", "vbox", "qemu", "bochs"
    )

    for ((_, value) in properties) {
        val lowerValue = value?.lowercase() ?: continue
        for (keyword in vmKeywords) {
            if (lowerValue.contains(keyword)) {
                return true
            }
        }
    }
    return false
}




