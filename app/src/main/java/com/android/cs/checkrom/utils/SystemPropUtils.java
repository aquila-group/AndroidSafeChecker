package com.android.cs.checkrom.utils;

import java.lang.reflect.Method;

public class SystemPropUtils {

    public static void setProp(String key, String string) {
        String value = string;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("set", String.class, String.class);
            get.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProp(String key) {
        String value = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, ""));
//            Log.e("SystemPropUtils","key为："+key+ " ，读取的值为："+value);
        } catch (Exception e) {
        } finally {
            return value;
        }
    }

}
