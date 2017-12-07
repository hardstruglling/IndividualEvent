package com.hipad.bluetoothantilost.tool;

/**
 * Created by wk
 */

public class RssiUtil {
    private static final double A_Value=50;/**A - The signal strength when the transmitter and receiver are separated by 1 meter*/
    private static final double n_Value=2.5;/** n - Environmental attenuation factor*/

    /**
     * According to Rssi get the return distance, return the data unit m
     * @param rssi
     * @return
     */
    public static double getDistance(int rssi){
        int iRssi = Math.abs(rssi);
        double power = (iRssi-A_Value)/(10*n_Value);
        return Math.pow(10,power);
    }
}
