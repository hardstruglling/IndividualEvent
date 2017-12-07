package com.hipad.bluetoothantilost.activity;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.base.BaseActivity;
import com.hipad.bluetoothantilost.base.BaseApplication;
import com.hipad.bluetoothantilost.contants.BltContant;
import com.hipad.bluetoothantilost.manager.BltManager;
import com.hipad.bluetoothantilost.tool.ClearCacheUtils;
import com.hipad.bluetoothantilost.tool.DialogUtils;
import com.hipad.bluetoothantilost.view.ButtonRectangle;
import com.hipad.bluetoothantilost.view.SwitchButton;

/**
 * created by wk
 */
public class SettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private RelativeLayout rl_clear_cache;
    private ButtonRectangle btn_quit;
    private RelativeLayout rl_introduce;
    private TextView tv_cache_size;
    private SwitchButton switchSetting;


    @Override
    public int getLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {
        BltManager.getInstance().initBltManager(this);
        initTitle(getResources().getString(R.string.setting));
        rl_clear_cache = ((RelativeLayout) findViewById(R.id.rl_clear_cache));
        btn_quit = ((ButtonRectangle) findViewById(R.id.btn_quit));
        rl_introduce = ((RelativeLayout) findViewById(R.id.rl_introduce));
        tv_cache_size = ((TextView) findViewById(R.id.tv_cache_size));
        switchSetting = (SwitchButton) findViewById(R.id.switch_setting);

        btn_quit.setRippleSpeed(25.0f);
        checkBlueTooth();
        tv_cache_size.setText(ClearCacheUtils.getTotalCacheSize(this));

        rl_clear_cache.setOnClickListener(this);
        btn_quit.setOnClickListener(this);
        switchSetting.setOnCheckedChangeListener(this);
    }


    /**
     * Check if Bluetooth is on
     */
    private void checkBlueTooth() {
        if (BltManager.getInstance().getmBluetoothAdapter() == null ||
                !BltManager.getInstance().getmBluetoothAdapter().isEnabled()) {
            switchSetting.setChecked(false);
        } else switchSetting.setChecked(true);
        switchSetting.setOnCheckedChangeListener(this);
        switchSetting.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_clear_cache:
                DialogUtils.show(this, getResources().getString(R.string.hint), getResources().getString(
                        R.string.sure_clear_cache), getResources().getString(R.string.sure),
                        getResources().getString(R.string.cancel), new DialogUtils.OnDialogClickListener() {
                            @Override
                            public void onSureClick() {
                                ClearCacheUtils.cleanApplicationData(SettingActivity.this, "");
                                Toast.makeText(SettingActivity.this,
                                        getResources().getString(R.string.clear_cache_success) +
                                                ClearCacheUtils.getTotalCacheSize(SettingActivity.this),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        });
                break;
            case R.id.btn_quit:
                DialogUtils.show(this, getResources().getString(R.string.hint), getResources().getString(
                        R.string.sure_quit_application), getResources().getString(R.string.sure), getResources()
                        .getString(R.string.cancel), new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onSureClick() {
                        BaseApplication.getInstance().exit();
                    }

                    @Override
                    public void onCancelClick() {

                    }
                });
                break;
            case R.id.iv_back:
                finish();
                break;

        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_setting:
                if (isChecked) {
                    //Enable Bluetooth
                    BltManager.getInstance().clickBlt(SettingActivity.this, BltContant.BLUE_TOOTH_OPEN);
                } else {
                    BltManager.getInstance().clickBlt(SettingActivity.this, BltContant.BLUE_TOOTH_CLOSE);
                }
                break;
        }
    }
}
