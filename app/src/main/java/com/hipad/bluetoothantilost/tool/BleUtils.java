package com.hipad.bluetoothantilost.tool;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wk
 */

public class BleUtils {

    /**
     * Pair with equipment：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    public static boolean createBond(Class btClass,BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * Disassemble bluetooth ：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(Class btClass,BluetoothDevice btDevice){
        Method removeBondMethod = null;
        Boolean returnValue = false;
        try {
            removeBondMethod = btClass.getMethod("removeBond");
            returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     *
     * @param clsShow
     */
    static public void printAllInform(Class clsShow) {
        try {
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            Field[] allFields = clsShow.getFields();
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
