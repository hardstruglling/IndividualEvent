package com.hipad.bluetoothantilost.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.contants.BltContant;

import java.io.IOException;

/**
 * Created by wk
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BltManager {

    private static final int CONNECT_MSG = 4;
    private static final int REQUEST_ENABLE_BLE = 20;

    private BltManager() {
    }

    private static class BltHolder {
        private static BltManager bltManager = new BltManager();
    }

    public static BltManager getInstance() {
        return BltHolder.bltManager;
    }


    private BluetoothManager bluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;


    private BluetoothSocket mBluetoothSocket;


    private OnRegisterBltReceiver onRegisterBltReceiver;

    public interface OnRegisterBltReceiver {
        void onBluetoothDevice(BluetoothDevice device);
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothSocket getmBluetoothSocket() {
        return mBluetoothSocket;
    }

    /**
     * init BluetoothManager
     *
     * @param context
     */
    public void initBltManager(Context context) {
        if (bluetoothManager != null) return;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * register broad
     *
     * @param context
     */
    public void registerBltReceiver(Context context, OnRegisterBltReceiver onRegisterBltReceiver) {
        this.onRegisterBltReceiver = onRegisterBltReceiver;
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(searchDevices, intent);
    }

    /**
     * cancel broad
     *
     * @param context
     */
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(searchDevices);
        if (getmBluetoothAdapter() != null)
            getmBluetoothAdapter().cancelDiscovery();
    }


    /**
     * receive broad
     */
    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        //接收
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
            }
            BluetoothDevice device;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onRegisterBltReceiver.onBluetoothDevice(device);
            }
        }
    };



    /**
     *connect device
     */
    private void connect(BluetoothDevice btDev, Handler handler) {
        try {
            mBluetoothSocket = btDev.createRfcommSocketToServiceRecord(BltContant.SPP_UUID);
            if (mBluetoothSocket != null)
            if (getmBluetoothAdapter().isDiscovering())
                getmBluetoothAdapter().cancelDiscovery();
            if (!getmBluetoothSocket().isConnected()) {
                getmBluetoothSocket().connect();
            }
            if (handler == null) return;
            Message message = new Message();
            message.what = CONNECT_MSG;
            message.obj = btDev;
            handler.sendMessage(message);
        } catch (Exception e) {
            try {
                getmBluetoothSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    /**
     * do about bluetooth
     *
     * @param context
     * @param status
     */
    public void clickBlt(Activity context, int status) {
        switch (status) {
            case BltContant.BLUE_TOOTH_SEARTH:
                startSearthBltDevice(context);
                break;
            case BltContant.BLUE_TOOTH_OPEN:
                if (getmBluetoothAdapter() != null)
                    getmBluetoothAdapter().enable();
                break;
            case BltContant.BLUE_TOOTH_CLOSE:
                if (getmBluetoothAdapter() != null)
                    getmBluetoothAdapter().disable();
                break;
            case BltContant.BLUE_TOOTH_MY_SEARTH:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                context.startActivity(discoverableIntent);
                break;
            case BltContant.BLUE_TOOTH_CLEAR:
                try {
                    if (getmBluetoothSocket() != null)
                        getmBluetoothSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * check is support bluetooth
     */
    public void checkBleDevice(Activity context) {
        if (getmBluetoothAdapter() != null) {
            if (!getmBluetoothAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.phone_not_support_ble),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * search ble
     */
    private boolean startSearthBltDevice(Activity context) {
        checkBleDevice(context);
        if (getmBluetoothAdapter().isDiscovering())
            stopSearthBltDevice();
        getmBluetoothAdapter().startDiscovery();
        return true;
    }

    public boolean stopSearthBltDevice() {
        return getmBluetoothAdapter().cancelDiscovery();
    }

    /**
     * connect GATT Server
     * @param context
     * @param device
     * @param mCallback
     */
    public BluetoothGatt connectBleDevice(Context context, BluetoothDevice device, BluetoothGattCallback mCallback) {
        BluetoothGatt mBluetoothGatt = device.connectGatt(context, false, mCallback);
        return mBluetoothGatt;
    }

}
