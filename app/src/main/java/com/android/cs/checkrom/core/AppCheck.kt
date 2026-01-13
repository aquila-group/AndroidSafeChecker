package com.android.cs.checkrom.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.util.Log
import com.android.cs.checkrom.utils.ExecUtils
import com.android.cs.checkrom.utils.NativeUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


fun appCheck(context: Context): Boolean {

    val installedPackageInfoList =
        context.packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)

    installedPackageInfoList.forEach {
        val sourceDir = it.applicationInfo.sourceDir
        if (sourceDir?.isEmpty() == true
            || !File(sourceDir).exists()
            || NativeUtils.isFExists(sourceDir)
        ) {
            Log.e("TAG", it.packageName + " appCheck存在虚假App信息 $sourceDir")
            return true
        }
        val dataDir = it.applicationInfo.dataDir
        if (!NativeUtils.isDExists(dataDir)) {
            Log.e("TAG", it.packageName + " aasappCheck存在虚假App信息 $dataDir")
            return true
        }
    }
    return false
}

fun getAppiInfoByExec(): Boolean {
    val appItems =
        ExecUtils.runExec("cmd package resolve-activity -c android.intent.category.HOME -a android.intent.action.MAIN")/*.split("\n")*/
    Log.e("TAG", "run str: $appItems")
    return appItems.isEmpty()
}

fun getDefaultLauncher(): String? {
    return try {
        val process = ProcessBuilder(
            "cmd", "package", "resolve-activity",
            "-c", "android.intent.category.HOME",
            "-a", "android.intent.action.MAIN"
        )
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            // 解析输出
            extractPackageName(output)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun extractPackageName(output: String): String? {
    return when {
        output.contains("package=") -> {
            Regex("package=([a-zA-Z0-9._]+)").find(output)?.groupValues?.get(1)
        }

        output.contains("com.") && output.contains("/") -> {
            Regex("(com\\.[a-zA-Z0-9._]+)/").find(output)?.groupValues?.get(1)
        }

        else -> null
    }
}

private const val TAG = "TAG"
fun getLauncherByCmd(): String? {
    return try {
        // 构建命令
        val cmd = "cmd package resolve-activity " +
                "-c android.intent.category.HOME " +
                "-a android.intent.action.MAIN"

        Log.e(TAG, "执行命令: $cmd")

        val process = Runtime.getRuntime().exec(cmd)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val output = StringBuilder()
        var line: String?

        // 读取标准输出
        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }

        // 读取错误输出
        val errorOutput = StringBuilder()
        while (errorReader.readLine().also { line = it } != null) {
            errorOutput.append(line).append("\n")
        }

        val exitCode = process.waitFor()

        Log.e(TAG, "退出码: $exitCode")
        Log.e(TAG, "输出: ${output.toString().trim()}")
        if (errorOutput.isNotEmpty()) {
            Log.e(TAG, "错误: ${errorOutput.toString().trim()}")
        }

        // 解析输出
        parseLauncherFromOutput(output.toString())
    } catch (e: Exception) {
        Log.e(TAG, "执行命令失败: ${e.message}")
        null
    }
}

private fun parseLauncherFromOutput(output: String): String? {
    val lines = output.lines()

    lines.forEach { line ->
        // 寻找包含包名的行
        if (line.contains("com.") && line.contains("/")) {
            // 示例: "e1cfea1 com.google.android.apps.nexuslauncher/.NexusLauncherActivity filter 1d8e2488"
            val parts = line.trim().split("\\s+".toRegex())

            if (parts.size >= 2) {
                val activityInfo =
                    parts[1]  // com.google.android.apps.nexuslauncher/.NexusLauncherActivity
                val packageName = activityInfo.substringBefore("/")

                if (packageName.isNotEmpty()) {
                    Log.e(TAG, "解析到包名: $packageName")
                    return packageName
                }
            }
        }
    }

    return null
}

fun appCheck1(context: Context): Boolean {
    Log.e(
        "TAG", "反射读取uid===== ${
            NativeUtils.getUidForPackage(
                context,
                "com.example.test"
            )
        }"
    )
    val installedApplications = context.packageManager.getInstalledApplications(
        MATCH_UNINSTALLED_PACKAGES
    )
    val uid = Binder.getCallingUid()
    installedApplications.forEach {
        val packageName = it.packageName
        if (isSystemApp(it)) {
//            Log.e("TAG", it.packageName + " appCheck1存在虚假App信息uid对不上 $uid ${it.uid}")
            return uid < it.uid
        }
        val uidForPackage = NativeUtils.getUidForPackage(
            context,
            packageName
        )
        if (isSystemApp(it) && uidForPackage != it.uid
        ) {
//            Log.e("TAG", packageName + " appCheck1存在虚假App信息 $uidForPackage ${it.uid}")
            return true
        }
    }
    return false
}

private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
    return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
}

fun getLauncherPackageName(context: Context): String {
    val pm = context.packageManager
    val intent = Intent()
    intent.setAction(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val items: List<ResolveInfo> = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    items.forEach {
        Log.e("TAG", "${it.activityInfo.packageName}")
    }
    return if (items.isNotEmpty()) items.get(0).activityInfo.packageName else "afssasaasd"

    /*val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val res = context.packageManager.resolveActivity(intent, 0) ?: return "\$unknown"
    if (res.activityInfo == null) {
        return "\$unknown"
    }
    return res.activityInfo.packageName*/
}

fun isBrandOfficialMarketMissing(context: Context): Boolean {
    val brand = (Build.MANUFACTURER ?: Build.BRAND ?: "unknown").lowercase()

    val expectedMarkets = when (brand) {
        "xiaomi", "redmi", "poco" -> listOf("com.xiaomi.market")
        "huawei", "honor" -> listOf("com.huawei.appmarket")
        "oppo" -> listOf("com.oppo.market")
        "vivo", "bbk", "iqoo" -> listOf("com.bbk.appstore")
        "samsung" -> listOf("com.sec.android.app.samsungapps")
        "google" -> listOf("com.android.vending")
        "oneplus" -> listOf("com.heytap.market", "com.oppo.market")
        "realme" -> listOf("com.heytap.market", "com.oppo.market")
        "meizu" -> listOf("com.meizu.mstore")
        "lenovo" -> listOf("com.lenovo.leos.appstore")
        "zte" -> listOf("com.zte.market")
        "asus" -> listOf("com.asus.appstore")
        else -> {
            Log.e("BrandCheck", "未知品牌: $brand")
            return false
        }
    }

    val isInstalled = expectedMarkets.any { packageName ->
        // 检查包是否存在
        val packageExists = try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        if (!packageExists) return@any false

        // 检查是否有启动Intent
        val hasLaunchIntent = context.packageManager.getLaunchIntentForPackage(packageName) != null
        if (!hasLaunchIntent) return@any false

        // 检查是否能处理市场Intent
        val canHandleMarket = try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=test"))
            intent.setPackage(packageName)
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo != null && resolveInfo.activityInfo.packageName == packageName
        } catch (e: Exception) {
            false
        }

        // 检查是否有市场相关权限
        val hasMarketPermissions = try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions ?: emptyArray()
            permissions.any {
                it.contains("INSTALL_PACKAGES", ignoreCase = true) ||
                        it.contains("DELETE_PACKAGES", ignoreCase = true) ||
                        it.contains("BILLING", ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
        packageExists && hasLaunchIntent && (canHandleMarket || hasMarketPermissions)
    }

    Log.e("BrandCheck", "品牌: $brand, 应有商店: $expectedMarkets, 已安装: $isInstalled")
    return !isInstalled
}

/*fun checkStore(context: Context): Boolean {
    val pm = context.packageManager
    val intent = Intent()
    intent.setAction(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_APP_MARKET)
    val items: List<ResolveInfo> =
        pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    if(items.isNotEmpty()){
        val packageName = items.get(0).activityInfo.packageName
        Log.e("TAG", "packageName = $packageName")
    }

    return items.isEmpty()
}*/
