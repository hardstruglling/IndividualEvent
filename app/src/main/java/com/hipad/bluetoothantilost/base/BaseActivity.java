package com.hipad.bluetoothantilost.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hipad.bluetoothantilost.R;

/**
 * Created by wk
 */

public abstract class BaseActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayout());
        BaseApplication.getInstance().addActivity(this);
        initView();
    }

    public int getLayout() {
        return 0;
    }

    public void initTitle(String title){
        ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
        TextView tv_title = (TextView)findViewById(R.id.tv_act_title);
        tv_title.setText(title);
        iv_back.setOnClickListener(this);
    }

    public abstract void initView();

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
