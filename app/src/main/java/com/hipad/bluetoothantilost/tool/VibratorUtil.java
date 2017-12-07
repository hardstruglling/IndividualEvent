package com.hipad.bluetoothantilost.tool;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by wk
 */

public class VibratorUtil {
    /**
     * To achieve mobile phone vibration
     * @param activity
     * @param milliseconds
     */
    public static void Vibrate(final Context activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * Overloaded method
     * To achieve mobile phone vibration
     * @param activity
     * @param pattern
     * @param isRepeat
     */
    public static void Vibrate(final Activity activity, long[] pattern,boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }
}
