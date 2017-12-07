package com.hipad.bluetoothantilost.activity;

import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.base.BaseActivity;
import com.hipad.bluetoothantilost.contants.BleContants;
import com.hipad.bluetoothantilost.tool.SharedPreferencesUtils;

/**
 * created by wk
 */
public class GuideHelpActivity extends BaseActivity {


    private TextView tv_content;
    private TextView tv_know;
    private TextView tv_menu_content;
    private TextView tv_menu_know;
    private RelativeLayout rl_help_menu;
    private RelativeLayout rl_add_help;
    private String path = "fonts/hk.ttf";


    @Override
    public int getLayout() {
        return R.layout.activity_guide_help;
    }

    @Override
    public void initView() {
        SharedPreferencesUtils.saveBoolean(this, BleContants.GUIDE_HELP,false);
        //Set the display font style
        Typeface fontFace = Typeface.createFromAsset(getAssets(),path);
        tv_content = ((TextView) findViewById(R.id.tv_content));
        tv_know = ((TextView) findViewById(R.id.tv_know));
        tv_menu_content = ((TextView) findViewById(R.id.tv_menu_content));
        tv_menu_know = ((TextView) findViewById(R.id.tv_menu_know));
        rl_help_menu = ((RelativeLayout) findViewById(R.id.rl_help_menu));
        rl_add_help = ((RelativeLayout) findViewById(R.id.rl_add_help));

        tv_know.setTypeface(fontFace);
        tv_content.setTypeface(fontFace);
        tv_menu_content.setTypeface(fontFace);
        tv_menu_know.setTypeface(fontFace);

        tv_know.setOnClickListener(this);
        tv_menu_know.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.tv_know:
                endBelowLayoutAnim();
                break;
            case R.id.tv_menu_know:
                finish();
                overridePendingTransition(R.anim.act_enter_anim,R.anim.act_exit_anim);
                break;
        }
    }

    /**
     * Add a button to help guide the animation
     */
    private void endBelowLayoutAnim(){
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(600);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setDuration(600);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rl_add_help.setVisibility(View.GONE);
                showTopLayoutAnim();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        rl_add_help.startAnimation(set);
    }

    /**
     * 添加左上角列表帮助引导执行动画
     */
    private void showTopLayoutAnim(){
        rl_help_menu.setVisibility(View.VISIBLE);
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,0,1);
        scaleAnimation.setDuration(600);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(600);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        rl_help_menu.startAnimation(set);
    }
}
