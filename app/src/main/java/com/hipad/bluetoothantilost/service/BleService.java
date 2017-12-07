package com.hipad.bluetoothantilost.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.contants.BleContants;
import com.hipad.bluetoothantilost.tool.AppUtils;
import com.hipad.bluetoothantilost.tool.RssiUtil;
import com.hipad.bluetoothantilost.tool.SharedPreferencesUtils;
import com.hipad.bluetoothantilost.tool.VibratorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/5/21.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.charon.www.NewBluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.charon.www.NewBluetooth.ACTION_DATA_AVAILABLE";
    public final static String READ_RSSI = "com.charon.www.NewBluetooth.READ_RSSI";

    public List<UUID> writeUuid = new ArrayList<>();
    public List<UUID> readUuid = new ArrayList<>();
    public List<UUID> notifyUuid = new ArrayList<>();

    private final IBinder mBinder = new LocalBinder();
    private int constantlyRssi;
    private int count = 0;
    public ScheduledExecutorService scheduler;
    private int[] array = new int[10];
    private MediaPlayer mp;
    private BluetoothDevice device;
    public static final String CONNECTED = "connected";
    public static final String DISCONNECTED = "disconnected";
    private int sum = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        scheduler = Executors.newScheduledThreadPool(1);
        mp = MediaPlayer.create(this, R.raw.bibibi);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }


    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    public void disconnect() {
        if (mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    private double distance;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {//5
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                SharedPreferencesUtils.saveString(BleService.this, BleContants.BLE_CONNECT_FLAG,
                        CONNECTED);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = BluetoothProfile.STATE_CONNECTED;
                broadcastUpdate(intentAction);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                SharedPreferencesUtils.saveString(BleService.this, BleContants.BLE_CONNECT_FLAG,
                        DISCONNECTED);
                if (!AppUtils.isAppOnForeground(BleService.this)) {
                    if (SharedPreferencesUtils.getBoolean(BleService.this,
                            BleContants.SET_LOST_HINT, true)) {
                        VibratorUtil.Vibrate(BleService.this, 1000);
                        mp.start();
                    }
                }
                scheduler.shutdownNow();
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (canGetRssi()) {
                        if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                            int bleRssi = Math.abs(constantlyRssi);
                            count = count % 10;
                            array[count] = bleRssi;
                            count++;
                            if (count == 9) {
                                for (int i = 0; i < array.length; i++) {
                                    sum += array[i];
                                }
                                double distance = RssiUtil.getDistance(sum / 10);
                                if (distance > 10) {
                                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                                }
                            }
                        } else {
                            scheduler.shutdownNow();
                        }
                    } else {
                        if (device != null) {
                            mBluetoothGatt = device.connectGatt(BleService.this, false, mGattCallback);
                        }
                    }
                }
            }, 3000, 800, TimeUnit.MILLISECONDS);
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> supportedGattServices = gatt.getServices();

                for (BluetoothGattService gattService : supportedGattServices) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            readUuid.add(gattCharacteristic.getUuid());
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                            writeUuid.add(gattCharacteristic.getUuid());
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            notifyUuid.add(gattCharacteristic.getUuid());
                        }
                    }
                }

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            constantlyRssi = rssi;
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                          int status) {
        }
    };

    public boolean canGetRssi() {
        if (mBluetoothGatt == null || mConnectionState == BluetoothProfile.STATE_DISCONNECTED)
            return false;
        return mBluetoothGatt.readRemoteRssi();
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        } else mBluetoothGatt.writeCharacteristic(characteristic);

    }
}
