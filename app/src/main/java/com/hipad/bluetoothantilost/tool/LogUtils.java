package com.hipad.bluetoothantilost.tool;

import android.util.Log;

import com.hipad.bluetoothantilost.contants.BleContants;

/**
 * Created by wk
 */

public class LogUtils {

    private static final String TAG = "BleAntiLost";

    public static void i(String str){
        if (BleContants.DEBUG){
            Log.i(TAG,str);
        }
    }

    public static void d(String str){
        if (BleContants.DEBUG){
            Log.d(TAG,str);
        }
    }

    public static void e(String str){
        if (BleContants.DEBUG){
            Log.e(TAG,str);
        }
    }

    public static void v(String str){
        if (BleContants.DEBUG){
            Log.v(TAG,str);
        }
    }

    public static void w(String str){
        if (BleContants.DEBUG){
            Log.w(TAG,str);
        }
    }

}
