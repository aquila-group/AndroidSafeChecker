package com.android.cs.checkrom.utils;

import android.os.Parcel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReflectionUtils {
    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        Object result = null;

        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
        }

        return result;
    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        Field field = getDeclaredField(clazz, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + clazz + "]");
        }

        makeAccessible(field);

        Object result = null;

        try {
            result = field.get(null);
        } catch (IllegalAccessException e) {
        }

        return result;
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object
     * @param fieldName
     * @param value
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws IllegalArgumentException {
        Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
        }
    }


    /**
     * 通过反射, 获得定义 Class 时声明的父类的泛型参数的类型
     * 如: public EmployeeDao extends BaseDao<Employee, String>
     *
     * @param clazz
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Class getSuperClassGenricType(Class clazz, int index) {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }

        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * 通过反射, 获得 Class 定义中声明的父类的泛型参数类型
     * 如: public EmployeeDao extends BaseDao<Employee, String>
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getSuperGenericType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                //Method 不在当前类定义, 继续向上转型
            }
        }

        return null;
    }

    public static Method getDeclaredMethod(Class<?> clazz,String methodName, Class<?>[] parameterTypes) {
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                //Method 不在当前类定义, 继续向上转型
            }
        }

        return null;
    }

    /**
     * 使 filed 变为可访问
     *
     * @param field
     */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object
     * @param filedName
     * @return
     */
    public static Field getDeclaredField(Object object, String filedName) {

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                //Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    public static Field getDeclaredField(Class<?> clazz, String filedName) {

        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                //Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected)
     *
     * @param object
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes,
                                      Object[] parameters) throws InvocationTargetException {

        Method method = getDeclaredMethod(object, methodName, parameterTypes);

        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");
        }

        method.setAccessible(true);

        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException e) {
        }

        return null;
    }

    public static boolean setStaticField(Class<?> clazz, String name, Object value) {
        Field field = getDeclaredField(clazz, name);
        if(field == null)
            return false;
        field.setAccessible(true);
        try {
            field.set(null, value);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static <T> T callDefaultConstructor(Class<T> clazz) {
        T result = null;
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(new Class[0]);
            return (T) constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
        }
        return result;
    }

    // c1为对象类型 c2为参数类型
    // 主要是为了防止Integer.class和int.class不相等的情况
    // 由于对象传递时都已经分装为包装类，所以c1一般都是Integer之类的
    private static boolean isTypeEqual(Class<?> c1, Class<?> c2) {
        if(c1.equals(Integer.class) && c2.equals(int.class)) {
            return true;
        } else if(c1.equals(Long.class) && c2.equals(long.class)) {
            return true;
        } else if(c1.equals(Boolean.class) && c2.equals(boolean.class)) {
            return true;
        } else if(c1.equals(Byte.class) && c2.equals(byte.class)) {
            return true;
        } else if(c1.equals(Short.class) && c2.equals(short.class)) {
            return true;
        } else if(c1.equals(Float.class) && c2.equals(float.class)) {
            return true;
        } else if(c1.equals(Double.class) && c2.equals(double.class)) {
            return true;
        } else if(c1.equals(Character.class) && c2.equals(char.class)) {
            return true;
        }
        return false;
    }

    // 自动调用最匹配的构造函数
    // 主要用于对付Android的内部类
    public static <T> T callMatchConstructor(Class<T> clazz, Object... args) {
        try {
            List<Constructor<?>> constructors = new ArrayList<>(Arrays.asList(clazz.getDeclaredConstructors()));
            Collections.sort(constructors, Comparator.comparingInt(Constructor::getParameterCount));
            for (Constructor<?> constructor : constructors) {
                if(constructor.getParameterCount() < args.length) {
                    continue;
                }
                Class<?>[] arg_types = constructor.getParameterTypes();
                boolean is_match = true;
                for(int i = 0; i < arg_types.length; i++) {
                    Class<?> type = arg_types[i];
                    if(type.isAssignableFrom(Parcel.class) || type.equals(Parcel.class)) {
                        is_match = false;
                        break;
                    }
                    if(i < args.length) {
                        if(args[i] != null) {
                            if (!type.isInstance(args[i])
                                    && !type.isAssignableFrom(args[i].getClass())
                                    && !type.equals(args[i].getClass())
                                    && !isTypeEqual(args[i].getClass(), type)
                            ) {
                                is_match = false;
                                break;
                            }
                        }
                    }
                }
                if(!is_match) {
                    continue;
                }

                Object[] match_args = new Object[arg_types.length];
                for(int i = 0; i < match_args.length; i++) {
                    if (i < args.length && args[i] != null) {
                        match_args[i] = args[i];
                    } else {
                        // 填充null或者0
                        Class<?> type = arg_types[i];

                        if(type.equals(Integer.class) || type.equals(int.class)) {
                            match_args[i] = (Integer) 0;
                        } else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
                            match_args[i] = (Boolean) false;
                        } else if(type.equals(Long.class) || type.equals(long.class)) {
                            match_args[i] = (long) 0;
                        } else if(type.equals(Float.class) || type.equals(float.class)) {
                            match_args[i] = (float) 0.0f;
                        } else if(type.equals(Double.class) || type.equals(double.class)) {
                            match_args[i] = (double) 0.0d;
                        } else if(type.equals(Character.class) || type.equals(char.class)) {
                            match_args[i] = (char) 0;
                        } else if(type.equals(Byte.class) || type.equals(byte.class)) {
                            match_args[i] = (byte) 0;
                        } else if(type.equals(Short.class) || type.equals(short.class)) {
                            match_args[i] = (short) 0;
                        } else {
                            match_args[i] = null;
                        }
                    }
                }

                return (T) constructor.newInstance(match_args);
            }
        } catch(Exception e) {
        }
        return null;
    }

}
