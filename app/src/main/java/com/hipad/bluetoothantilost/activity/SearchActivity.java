package com.hipad.bluetoothantilost.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.adapter.BleAdapter;
import com.hipad.bluetoothantilost.base.BaseActivity;
import com.hipad.bluetoothantilost.contants.BleContants;
import com.hipad.bluetoothantilost.contants.BltContant;
import com.hipad.bluetoothantilost.manager.BltManager;
import com.hipad.bluetoothantilost.tool.DialogUtils;
import com.hipad.bluetoothantilost.tool.SharedPreferencesUtils;
import com.hipad.bluetoothantilost.tool.ViewUtils;
import com.hipad.bluetoothantilost.view.ButtonFloat;
import com.hipad.bluetoothantilost.view.ClearEditText;
import com.hipad.bluetoothantilost.view.SwitchButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, View.OnTouchListener {

    private ListView mListView;
    private List<BluetoothDevice> bltList;
    private BleAdapter mAdapter;
    private static final int SEARCH_NEW_BLEDEVICE_MSG = 5;
    private static final int REFRESH_MSG = 6;
    private boolean isRefresh = false;

    /**
     * handler message
     */
    private SearchHandler handler = new SearchHandler(this);

    private RelativeLayout rl_search;
    private RelativeLayout.LayoutParams params;
    private View loadingView;
    private RelativeLayout rl_search_below;
    private ButtonFloat ib_btn_search;
    private SwipeRefreshLayout swipeRefresh;
    private View shadeView;
    private ClearEditText editText;
    private SwitchButton btnSwitch;
    private Button btnSure;
    private RelativeLayout rl_pop;
    private View deviceView;
    private LinearLayout ll_pop;
    private BluetoothDevice bluetoothDevice;
    private boolean hasMeasured = false;
    private View content;
    private int screenWidth,screenHeight;
    private int downX,downY;
    private int lastY;
    private int lastX;
    private boolean clickormove;
    private View noDataView;

    @Override
    public int getLayout() {
        return R.layout.activity_search;
    }

    @Override
    public void initView() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        initTitle(getResources().getString(R.string.add_anti_lost));
        BltManager.getInstance().initBltManager(this);

        content = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        ViewTreeObserver vto = content.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!hasMeasured) {
                    screenHeight = content.getMeasuredHeight();
                    hasMeasured = true;
                }
                return true;
            }
        });
        mListView = (ListView) findViewById(R.id.search_listView);
        rl_search = ((RelativeLayout) findViewById(R.id.rl_search));
        rl_search_below = ((RelativeLayout) findViewById(R.id.rl_search_below));
        ib_btn_search = ((ButtonFloat) findViewById(R.id.ib_btn_search));
        swipeRefresh = ((SwipeRefreshLayout) findViewById(R.id.search_swipe_refresh));
        shadeView = getLayoutInflater().inflate(R.layout.transparent_shade, null);
        shadeView.setOnClickListener(null);

        mListView.setVerticalScrollBarEnabled(false);

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        loadingView = ViewUtils.getLoadingView(this);
        rl_search_below.addView(loadingView, params);
        noDataView = getLayoutInflater().inflate(R.layout.no_data_item,null);

        changeDeviceName();
        initData();
        ib_btn_search.setOnTouchListener(this);
        ib_btn_search.setOnClickListener(this);
        swipeRefresh.setOnRefreshListener(this);

    }

    /**
     * Set the device name and whether to open the anti-throw alert layout
     */
    private void changeDeviceName() {
        deviceView = getLayoutInflater().inflate(R.layout.change_set_device, null);
        editText = ((ClearEditText) deviceView.findViewById(R.id.edit_device_name));
        btnSwitch = ((SwitchButton) deviceView.findViewById(R.id.change_switch));
        btnSure = ((Button) deviceView.findViewById(R.id.btn_change_sure));
        rl_pop = ((RelativeLayout) deviceView.findViewById(R.id.rl_pop));
        ll_pop = ((LinearLayout) deviceView.findViewById(R.id.ll_pop));


        rl_pop.setOnClickListener(this);
        ll_pop.setOnClickListener(this);
        btnSure.setOnClickListener(this);
    }


    /**
     * set bluetooth
     */
    private void initData() {
        bltList = new ArrayList<>();
        mAdapter = new BleAdapter(this, bltList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        //Check if Bluetooth is on
        BltManager.getInstance().checkBleDevice(this);
        //Register a Bluetooth scan broadcast
        blueToothRegister();
        //First came in search for equipment
        BltManager.getInstance().clickBlt(this, BltContant.BLUE_TOOTH_SEARTH);
        handler.sendEmptyMessageDelayed(SEARCH_NEW_BLEDEVICE_MSG, 12 * 1000);
    }


    /**
     * register a Bluetooth callback broadcast
     */
    private void blueToothRegister() {
        BltManager.getInstance().registerBltReceiver(this, new BltManager.OnRegisterBltReceiver() {
            /**
             * Search for new devices
             * @param device
             */
            @Override
            public void onBluetoothDevice(BluetoothDevice device) {
                if (bltList != null && !bltList.contains(device)) {
                    bltList.add(device);
                }
                rl_search_below.removeView(noDataView);
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }

        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ib_btn_search://Search for equipment
                if (clickormove){
                    rl_search_below.removeView(noDataView);
                    if (bltList != null && mAdapter != null) {
                        bltList.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                    rl_search_below.addView(loadingView, params);
                    BltManager.getInstance().clickBlt(this, BltContant.BLUE_TOOTH_SEARTH);
                    if (!handler.hasMessages(SEARCH_NEW_BLEDEVICE_MSG)) {
                        handler.sendEmptyMessageDelayed(SEARCH_NEW_BLEDEVICE_MSG, 12 * 1000);
                    }
                }
                break;
            case R.id.btn_change_sure:
                SharedPreferencesUtils.saveString(this, BleContants.BLE_DEVICE_NAME,
                        editText.getText().toString());
                if (btnSwitch.isChecked()) {
                    SharedPreferencesUtils.saveBoolean(this, BleContants.SET_LOST_HINT, true);
                } else {
                    SharedPreferencesUtils.saveBoolean(this, BleContants.SET_LOST_HINT, false);
                }
                Intent intentConnect = new Intent();
                intentConnect.setAction(BleContants.CONNECTING_BLUETOOTH);
                Bundle bundle = new Bundle();
                if (bluetoothDevice != null) {
                    bundle.putString(BleContants.BLE_DEVICE_ADDRESS, bluetoothDevice.getAddress());
                }
                intentConnect.putExtras(bundle);
                sendBroadcast(intentConnect);
                rl_search.removeView(deviceView);
                finish();
                break;
            case R.id.rl_pop:
                rl_search.removeView(deviceView);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //When the page is closed, disconnect Bluetooth
        BltManager.getInstance().unregisterReceiver(this);
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onRefresh() {
        rl_search_below.addView(shadeView,params);
        rl_search_below.removeView(noDataView);
        if (bltList != null && mAdapter != null) {
            bltList.clear();
            mAdapter.notifyDataSetChanged();
        }
        BltManager.getInstance().clickBlt(this, BltContant.BLUE_TOOTH_SEARTH);
        handler.sendEmptyMessageDelayed(REFRESH_MSG, 12 * 1000);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        DialogUtils.show(this, getResources().getString(R.string.hint), getResources().getString
                        (R.string.sure_connect_device) + (bltList.get(position).getName() == null ?
                        bltList.get(position).getAddress() : bltList.get(position).getName()),
                getResources().getString(R.string.sure), getResources().getString(R.string.cancel),
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onSureClick() {
                        bluetoothDevice = bltList.get(position);
                        rl_search.addView(deviceView, params);
                        boolean isOn = SharedPreferencesUtils.getBoolean(SearchActivity.this,
                                BleContants.SET_LOST_HINT, true);
                        btnSwitch.setChecked(isOn);

                        editText.setText(bluetoothDevice.getName() == null ? bltList.get(position).getAddress()
                                : bluetoothDevice.getName());
                        if (editText.getText().toString().length() > 0) {
                            editText.setShakeAnimation();
                        }
                    }

                    @Override
                    public void onCancelClick() {

                    }
                });
    }

    /**
     * button touch event
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.ib_btn_search:
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        downX = lastX;
                        downY = lastY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - lastX);
                        int dy = (int) (event.getRawY() - lastY);
                        int left = v.getLeft() + dx;
                        int top = v.getTop() + dy;
                        int right = v.getRight() + dx;
                        int bottom = v.getBottom() + dy;
                        if (left < 0) {
                            left = 0;
                            right = left + v.getWidth();
                        }
                        if (right > screenWidth) {
                            right = screenWidth;
                            left = right - v.getWidth();
                        }
                        if (top < 0) {
                            top = 0;
                            bottom = top + v.getHeight();
                        }
                        if (bottom > screenHeight) {
                            bottom = screenHeight;
                            top = bottom - v.getHeight();
                        }
                        v.layout(left, top, right, bottom);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //Move distance less than 5 means drag
                        if (Math.abs((int) (event.getRawX() - downX)) > 5
                                || Math.abs((int) (event.getRawY() - downY)) > 5)
                            clickormove = false;
                        else
                            clickormove = true;
                        break;
                }
                break;
        }
        return false;
    }

    /**
     * Use a weakly referenced handler to prevent memory leaks
     */
    class SearchHandler extends Handler {
        private WeakReference<SearchActivity> activity;

        public SearchHandler(SearchActivity mActivity) {
            activity = new WeakReference<SearchActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_NEW_BLEDEVICE_MSG:
                    rl_search_below.removeView(loadingView);
                    if (bltList.size() <= 0 ){
                        rl_search_below.addView(noDataView,params);
                    }
                    break;
                case REFRESH_MSG:
                    rl_search_below.removeView(shadeView);
                    swipeRefresh.setRefreshing(false);
                    if (bltList.size() <= 0 ){
                        rl_search_below.addView(noDataView,params);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
