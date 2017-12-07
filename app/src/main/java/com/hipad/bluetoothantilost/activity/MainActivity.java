package com.hipad.bluetoothantilost.activity;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.base.BaseActivity;
import com.hipad.bluetoothantilost.contants.BleContants;
import com.hipad.bluetoothantilost.manager.BltManager;
import com.hipad.bluetoothantilost.service.BleService;
import com.hipad.bluetoothantilost.tool.SharedPreferencesUtils;
import com.hipad.bluetoothantilost.tool.VibratorUtil;
import com.hipad.bluetoothantilost.tool.ViewUtils;
import com.hipad.bluetoothantilost.view.InsLoadingView;
import com.hipad.bluetoothantilost.view.RotateLoading;
import com.hipad.bluetoothantilost.view.SwitchButton;
import com.hipad.bluetoothantilost.viewdrag.DragLayout;
import com.nineoldandroids.view.ViewHelper;

import java.lang.ref.WeakReference;

/**
 * created by wk
 */
public class MainActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private ImageView iv_left_ble;
    private RelativeLayout rl_connected;
    private TextView tv_ble_name;
    private TextView tv_not_connect;
    private InsLoadingView insLoadingView;
    private TextView tv_add_device;
    private TextView tv_device_name;
    private ImageView ivIcon;
    private ProgressBar progressBar;
    private View upDateLoadingView;
    private RotateLoading rotateLoading;
    private LinearLayout.LayoutParams params;
    private ProgressBar leftProgressbar;
    private ImageView iv_success_ion;
    private MediaPlayer mp;
    private String bleDeviceName;
    private TextView tvConnectStatus;
    private static final String DEFAULT = "";
    private static final String OTHER = "other";
    private String deviceName;
    private BleService bleService;
    private String deviceAddress;
    private boolean isRemind;
    private RelativeLayout rl_search_item;
    private RelativeLayout rl_update_item;
    private RelativeLayout rl_setting_item;
    private RelativeLayout rl_help_item;
    private RelativeLayout rl_about_item;
    private ImageView iv_left;
    private DragLayout dl;
    private RelativeLayout rl_main;
    private SwitchButton switchDisconnect;
    private long exitTime = 0;

    private static final int BACK_MSG = 5;
    private static final int CONNECT_BLE_COMPLETE_MSG = 6;
    private static final int CONNECT_BLE_CANCEL_MSG = 7;
    private static final int CONNECTING_MSG = 8;
    private static final int UPDATE_MSG = 9;
    private static final int CONNECT_FAILED_MSG = 10;
    private static final int REQUEST_ENABLE_BLE = 20;


    /**
     * handler message
     */
    private MainHandler handler = new MainHandler(this);

    /**
     * receive the info broadcast
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BleContants.CONNECTING_BLUETOOTH)) {
                if (dl.getStatus() == DragLayout.Status.Open){
                    dl.close();
                }
                deviceName = SharedPreferencesUtils.getString(MainActivity.this,
                        BleContants.BLE_DEVICE_NAME, DEFAULT);
                if (deviceName != null) {
                    tv_add_device.setText(getResources().getString(R.string.connecting_ble_device) + deviceName);
                    tv_device_name.setText(getResources().getString(R.string.no_device));
                } else
                    tv_add_device.setText(getResources().getString(R.string.connecting_ble_device));
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    deviceAddress = extras.getString(BleContants.BLE_DEVICE_ADDRESS);
                }
                Intent gattServiceIntent = new Intent(MainActivity.this, BleService.class);
                startService(gattServiceIntent);
                bindService(gattServiceIntent, connection, BIND_AUTO_CREATE);
                handler.sendEmptyMessage(CONNECTING_MSG);
            }
        }
    };


    /**
     * Register for access to equipment information broadcast
     */
    private void registerBroad() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BleContants.CONNECTING_BLUETOOTH);
        registerReceiver(receiver, filter);
    }

    /**
     * unregister broadcast
     */
    private void unregisterBroad() {
        unregisterReceiver(receiver);
    }


    /**
     * Connect with the service
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Toast.makeText(bleService, getResources().getString(R.string.ble_init_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

            }
            if (deviceAddress != null) bleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * Register the device connection status to change the broadcast
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleService.READ_RSSI);
        return intentFilter;
    }

    /**
     * Receiving device connection status change broadcast
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                handler.sendEmptyMessage(CONNECT_BLE_COMPLETE_MSG);
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (bleService != null) {
                    if (bleService.scheduler != null) {
                        bleService.scheduler.shutdownNow();
                    }
                }
                handler.sendEmptyMessage(CONNECT_BLE_CANCEL_MSG);
            }
        }
    };


    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }


    @Override
    public void initView() {
        BltManager.getInstance().initBltManager(this);
        mp = MediaPlayer.create(this, R.raw.bibibi);
        registerBroad();
        rl_main = ((RelativeLayout) findViewById(R.id.rl_main));
        insLoadingView = ((InsLoadingView) findViewById(R.id.main_ins_loading));
        tv_add_device = ((TextView) findViewById(R.id.tv_add_device));
        tv_device_name = ((TextView) findViewById(R.id.tv_device_name));
        ivIcon = ((ImageView) findViewById(R.id.iv_status_icon));
        switchDisconnect = ((SwitchButton) findViewById(R.id.main_switch_disconnect));
        progressBar = ((ProgressBar) findViewById(R.id.btl_bar));
        iv_success_ion = ((ImageView) findViewById(R.id.iv_success_icon));
        tvConnectStatus = ((TextView) findViewById(R.id.tv_connect_status));
        rl_search_item = ((RelativeLayout) findViewById(R.id.rl_search_item));
        rl_update_item = ((RelativeLayout) findViewById(R.id.rl_update_item));
        rl_setting_item = ((RelativeLayout) findViewById(R.id.rl_setting_item));
        rl_help_item = ((RelativeLayout) findViewById(R.id.rl_help_item));
        rl_about_item = ((RelativeLayout) findViewById(R.id.rl_about_item));
        iv_left = ((ImageView) findViewById(R.id.iv_left));

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        if (SharedPreferencesUtils.getBoolean(this, BleContants.GUIDE_HELP, true)) {
            startActivity(new Intent(this, GuideHelpActivity.class));
        }
        tvConnectStatus.setText(getResources().getString(R.string.not_connect));
        initDragLayout();
        String flag = SharedPreferencesUtils.getString(this, BleContants.BLE_CONNECT_FLAG, DEFAULT);
        if (flag != null) {
            if (flag.equals(BleService.CONNECTED)) {
                Intent gattServiceIntent = new Intent(MainActivity.this, BleService.class);
                bindService(gattServiceIntent, connection, BIND_AUTO_CREATE);
                handler.sendEmptyMessage(CONNECT_BLE_COMPLETE_MSG);
            } else if (flag.equals(BleService.DISCONNECTED)) {
                handler.sendEmptyMessage(CONNECT_FAILED_MSG);
                SharedPreferencesUtils.saveString(this, BleContants.BLE_CONNECT_FLAG, OTHER);
            } else {
                tv_device_name.setText(getResources().getString(R.string.no_device));
            }
        } else tv_device_name.setText(getResources().getString(R.string.no_device));


        insLoadingView.setStatus(InsLoadingView.Status.UNCLICKED);
        upDateLoadingView = ViewUtils.getUpDateLoadingView(this);
        rotateLoading = ((RotateLoading) upDateLoadingView.findViewById(R.id.rotateloading));

        BltManager.getInstance().checkBleDevice(this);
        initLeft();
        initClick();
    }

    /**
     * Initialize the left sidebar
     */
    private void initDragLayout() {
        dl = (DragLayout) findViewById(R.id.dragLayout);
        dl.setDragListener(new DragLayout.DragListener() {
            @Override
            public void onOpen() {
                //When the user first entered the application prompt on the left side of the function
                if (SharedPreferencesUtils.getBoolean(MainActivity.this, BleContants.IS_FIRST_OPEN_LEFT,
                        true)) {
                    openLeftAnim();
                }
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(iv_left, 1 - percent);
            }
        });
    }

    /**
     * Open the animation on the left side of the slide
     */
    private void openLeftAnim() {
        SharedPreferencesUtils.saveBoolean(this, BleContants.IS_FIRST_OPEN_LEFT, false);
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setDuration(800);
        dl.getVg_left().startAnimation(translateAnimation);
    }

    /**
     * view click
     */
    private void initClick() {
        ivIcon.setOnClickListener(this);
        switchDisconnect.setOnCheckedChangeListener(this);
        rl_search_item.setOnClickListener(this);
        rl_update_item.setOnClickListener(this);
        rl_setting_item.setOnClickListener(this);
        rl_help_item.setOnClickListener(this);
        rl_about_item.setOnClickListener(this);
        iv_left.setOnClickListener(this);
    }


    /**
     * connection succeeded
     */
    private void connectComplete() {
        switchDisconnect.setChecked(true);
        rl_search_item.setVisibility(View.VISIBLE);
        tvConnectStatus.setText(getResources().getString(R.string.ble_disconnect));
        ivIcon.setVisibility(View.GONE);
        iv_success_ion.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        rl_connected.setVisibility(View.VISIBLE);
        leftProgressbar.setVisibility(View.GONE);
        iv_left_ble.setVisibility(View.VISIBLE);
        iv_left_ble.setImageResource(R.drawable.bluetooth_white);
        tv_not_connect.setVisibility(View.GONE);
        if (bleDeviceName != null) {
            tv_add_device.setText(getResources().getString(R.string.success_connect) + bleDeviceName);
            tv_ble_name.setText(bleDeviceName);
            tv_device_name.setText(bleDeviceName);
        }
        insLoadingView.setStartColor(getResources().getColor(R.color.greenC3));
        insLoadingView.setStatus(InsLoadingView.Status.UNCLICKED);
        insLoadingView.postInvalidate();
    }

    /**
     * Connection failed
     */
    private void connectFailed() {
        switchDisconnect.setChecked(false);
        rl_search_item.setVisibility(View.VISIBLE);
        tvConnectStatus.setText(getResources().getString(R.string.not_connect));
        Toast.makeText(this, getResources().getString(R.string.connect_fail_hint),
                Toast.LENGTH_SHORT).show();
        if (bleDeviceName != null) {
            tv_device_name.setText(bleDeviceName +
                    getResources().getString(R.string.ble_disconnect));
        }
        ivIcon.setVisibility(View.VISIBLE);
        ivIcon.setImageResource(R.drawable.close);
        progressBar.setVisibility(View.GONE);
        rl_connected.setVisibility(View.GONE);
        leftProgressbar.setVisibility(View.GONE);
        iv_success_ion.setVisibility(View.GONE);
        iv_left_ble.setVisibility(View.VISIBLE);
        iv_left_ble.setImageResource(R.drawable.bluetooth_off_white);
        iv_left_ble.setOnClickListener(this);
        tv_not_connect.setVisibility(View.VISIBLE);
        tv_not_connect.setText(getResources().getString(R.string.cancel_connect));
        tv_add_device.setText(getResources().getString(R.string.cancel_connect));
        connectFailedAnim();
        insLoadingView.setStartColor(getResources().getColor(R.color.redCC));
        insLoadingView.setStatus(InsLoadingView.Status.UNCLICKED);
        insLoadingView.postInvalidate();
    }

    /**
     * The connection failed to prompt the text to execute the animation
     */
    private void connectFailedAnim() {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setDuration(800);
        tv_add_device.startAnimation(translateAnimation);
    }

    /**
     * Is being connected
     */
    private void isConnecting() {
        ivIcon.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        iv_left_ble.setVisibility(View.GONE);
        leftProgressbar.setVisibility(View.VISIBLE);
        tv_not_connect.setVisibility(View.VISIBLE);
        iv_success_ion.setVisibility(View.GONE);
        tv_not_connect.setText(getResources().getString(R.string.is_connect_device));
        insLoadingView.setStartColor(getResources().getColor(R.color.greenC3));
        insLoadingView.setEndColor(getResources().getColor(R.color.redF6));
        insLoadingView.setStatus(InsLoadingView.Status.LOADING);
        insLoadingView.postInvalidate();
    }


    /**
     * set the left layout
     */
    private void initLeft() {
        iv_left_ble = ((ImageView) findViewById(R.id.iv_left_ble));
        rl_connected = ((RelativeLayout) findViewById(R.id.rl_connected));
        tv_ble_name = ((TextView) findViewById(R.id.tv_ble_name));
        tv_not_connect = ((TextView) findViewById(R.id.tv_not_connect));
        leftProgressbar = ((ProgressBar) findViewById(R.id.main_left_progress_bar));

        tv_not_connect.setText(getResources().getString(R.string.not_connect));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleDeviceName = SharedPreferencesUtils.getString(this, BleContants.BLE_DEVICE_NAME, DEFAULT);
        isRemind = SharedPreferencesUtils.getBoolean(MainActivity.this, BleContants.SET_LOST_HINT, true);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    /**
     * Click the back button to exit the application
     */
    @Override
    public void onBackPressed() {
        /* If the left side slide is not closed, close the left side slide*/
        if (dl.getStatus() != DragLayout.Status.Close) {
            dl.close();
        } else {
            /*
              If you click the back button, the middle time interval is greater than 1.8 seconds.
              Press again to exit the application and press the back key twice to exit the application.
               If the time interval is less than 1.8 seconds, exit the application
             */
            if (System.currentTimeMillis() - exitTime > 1800) {
                Toast.makeText(this, getResources().getString(R.string.double_quit_app),
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                SharedPreferencesUtils.saveString(this, BleContants.BLE_CONNECT_FLAG, OTHER);
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unregisterBroad();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_status_icon:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.rl_search_item:
                insLoadingView.setStartColor(getResources().getColor(R.color.greenC3));
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_setting_item:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.rl_update_item:
                rl_main.addView(upDateLoadingView, params);
                handler.sendEmptyMessageDelayed(UPDATE_MSG, 3000);
                if (dl.getStatus() == DragLayout.Status.Open){
                    dl.close();
                }
                break;
            case R.id.rl_help_item:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case R.id.rl_about_item:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.iv_left:
                dl.open();
                break;
        }
    }

    /**
     * activity get result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLE:
                break;
        }
    }

    /**
     * switch click event
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.main_switch_disconnect:
                if (isChecked) {

                } else {
                    if (bleService != null) {
                        SharedPreferencesUtils.saveString(this, BleContants.BLE_CONNECT_FLAG, OTHER);
                        bleService.disconnect();
                    }
                }
                break;
        }
    }

    /**
     * Use a weakly referenced handler to prevent handler memory from being leaked
     */
    class MainHandler extends Handler {
        private WeakReference<MainActivity> activity;

        public MainHandler(MainActivity mainActivity) {
            activity = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = activity.get();
            switch (msg.what) {
                case CONNECT_BLE_COMPLETE_MSG:
                    mainActivity.insLoadingView.setEndColor(getResources().getColor(R.color.greenC3));
                    mainActivity.connectComplete();
                    break;
                case CONNECT_BLE_CANCEL_MSG:
                    if (mainActivity.isRemind) {
                        VibratorUtil.Vibrate(MainActivity.this, 1000);
                        mainActivity.mp.start();
                    }
                    mainActivity.insLoadingView.setEndColor(getResources().getColor(R.color.redCC));
                    mainActivity.connectFailed();
                    break;
                case CONNECTING_MSG:
                    mainActivity.isConnecting();
                    break;
                case UPDATE_MSG:
                    mainActivity.rl_main.removeView(upDateLoadingView);
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.the_latest_version),
                            Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_FAILED_MSG:
                    mainActivity.insLoadingView.setEndColor(getResources().getColor(R.color.redCC));
                    mainActivity.connectFailed();
                    break;
                default:
                    break;
            }
        }
    }
}
