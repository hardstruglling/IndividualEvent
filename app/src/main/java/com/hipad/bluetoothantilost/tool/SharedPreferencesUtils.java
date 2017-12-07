package com.hipad.bluetoothantilost.tool;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wk
 */

public class SharedPreferencesUtils{


    private static final String FILE_NAME = "share_date";


    public static void saveInt(Context context,String key,int value) {
        SharedPreferences.Editor sp = getEditor(context);
        sp.putInt(key, value);
        sp.commit();
    }


    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor sp = getEditor(context);
        sp.putBoolean(key, value);
        sp.commit();
    }

    public static void saveString(Context context,String key,String value) {
        SharedPreferences.Editor sp = getEditor(context);
        sp.putString(key, value);
        sp.commit();
    }


    public static void saveFloat(Context context,String key, float value) {
        SharedPreferences.Editor sp = getEditor(context);
        sp.putFloat(key, value);
        sp.commit();
    }


    public static void saveLong(Context context,String key, long value) {
        SharedPreferences.Editor sp = getEditor(context);
        sp.putLong(key, value);
        sp.commit();
    }

    public static int getInt(Context context,String key, int defValue) {
        SharedPreferences sp = getSharedPreferences(context);
        int value = sp.getInt(key, defValue);
        return value;
    }

    public static boolean getBoolean(Context context,String key, boolean defValue) {
        SharedPreferences sp = getSharedPreferences(context);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }


    public static String getString(Context context,String key, String defValue) {
        SharedPreferences sp = getSharedPreferences(context);
        String value = sp.getString(key, defValue);
        return value;
    }


    public static float getFloat(Context context,String key,float defValue) {
        SharedPreferences sp = getSharedPreferences(context);
        float value = sp.getFloat(key, defValue);
        return value;
    }

    public static long getLong(Context context,String key, long defValue) {
        SharedPreferences sp = getSharedPreferences(context);
        long value = sp.getLong(key, defValue);
        return value;
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }
}
