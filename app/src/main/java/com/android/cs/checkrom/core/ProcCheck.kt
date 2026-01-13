package com.android.cs.checkrom.core

import com.android.cs.checkrom.utils.NativeUtils

fun checkMemInfo(): Boolean {
    return getMemFree() == getMemFree()
}

private fun getMemFree(): Long? {
    return try {
        val result = NativeUtils.runTimeExec("cat /proc/meminfo")
        parseMemFree(result)
    } catch (e: Exception) {
        null
    }
}

private fun parseMemFree(meminfo: String?): Long? {
    if (meminfo.isNullOrEmpty()) return null

    return meminfo.lines()
        .find { it.startsWith("MemFree:") }
        ?.split("\\s+".toRegex())
        ?.getOrNull(1)
        ?.toLongOrNull()
}

