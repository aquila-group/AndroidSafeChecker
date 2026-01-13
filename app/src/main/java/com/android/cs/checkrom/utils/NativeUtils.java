package com.android.cs.checkrom.utils;

import android.content.Context;

import java.util.Map;

public class NativeUtils {
    static {
        System.loadLibrary("nativelib");
    }
    public static native boolean checkMountinfo();
    public static native boolean isInContainer();
    public static native boolean checkDockerenv();
    public static native boolean checkInitProcess();
    public static native boolean isContainerByNamespace();
    public static native boolean isInContainerV2();
    public static native boolean isInContainerV3();
    public static native boolean isMarketInstalled(Context context);
    public static native boolean checkSystemPropsModified();
    public native static boolean isSuExist();
    public native static boolean checkRootByProcesses();
    public native static boolean checkMagiskFiles();
    public native static boolean checkRWSystem();
    public native static boolean checkBusybox();
    public native static boolean checkSuSymlinks();
    public native static boolean checkRootCloaking();
    public native static boolean isRootDetected();
    public static native boolean isFExists(String path);
    public static native boolean isDExists(String path);
    public static native String runTimeExec(String cmd);
    public static native boolean checkFile(String str, boolean isDir);
    public static native int getUidForPackage(Context context, String packageName);
    public static native Map<String, String> getAllProperties();
    public static native Map<String, String> getAllProperties1();
    public static native String getAllProperties2();
}
