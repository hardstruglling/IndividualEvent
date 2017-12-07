package com.hipad.bluetoothantilost.activity;
import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.base.BaseActivity;

/**
 * created by wk
 */
public class AboutActivity extends BaseActivity {


    @Override
    public int getLayout() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
      initTitle(getResources().getString(R.string.about));
    }

}
