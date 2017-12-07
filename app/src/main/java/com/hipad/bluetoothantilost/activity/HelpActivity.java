package com.hipad.bluetoothantilost.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.base.BaseActivity;
import com.hipad.bluetoothantilost.tool.DensityUtils;

/**
 * created by wk
 */
public class HelpActivity extends BaseActivity {

    private TextView mTextView;
    private ImageView mImageView;
    private int maxLine=5;
    private TextView tvNotConnectInfo;
    private ImageView imgBelow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_help;
    }

    @Override
    public void initView() {
        initTitle(getResources().getString(R.string.help));
        mTextView= (TextView) findViewById(R.id.adjust_text);
        mImageView= (ImageView) findViewById(R.id.turn_over_icon);
        tvNotConnectInfo = ((TextView) findViewById(R.id.tv_not_connect_info));
        imgBelow = ((ImageView) findViewById(R.id.turn_over_icon_below));


        initText(mTextView,mImageView);
        initText(tvNotConnectInfo,imgBelow);
        mImageView.setOnClickListener(new OnTurnListener(mTextView,mImageView));
        imgBelow.setOnClickListener(new OnTurnListener(tvNotConnectInfo,imgBelow));

    }


    /**
     * set the default textview height, determine the icon display and hide
     * @param tv
     * @param iv
     */
    private void initText(final TextView tv, final ImageView iv) {
        tv.setHeight(tv.getLineHeight() * maxLine+ DensityUtils.dp2px(this,3));
        tv.post(new Runnable() {
            @Override
            public void run() {
                iv.setVisibility(tv.getLineCount() > maxLine ? View.VISIBLE : View.GONE);
            }
        });

    }


    /**
     * Click the icon to expand or hide the contents of the textview
     */
    private class OnTurnListener implements View.OnClickListener {

        private boolean isExpand;
        private TextView mText;
        private ImageView mImage;
        public OnTurnListener(TextView textView,ImageView imageView){
            this.mText = textView;
            this.mImage = imageView;
        }

        @Override
        public void onClick(View v) {
            isExpand = !isExpand;
            mText.clearAnimation();
            final int tempHight;
            final int startHight = mText.getHeight();
            int durationMillis = 500;

            if (isExpand) {
                tempHight = mText.getLineHeight() * mText.getLineCount() - startHight;  //为正值，长文减去短文的高度差
                RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(durationMillis);
                animation.setFillAfter(true);
                mImage.startAnimation(animation);
            } else {

                tempHight = mText.getLineHeight() * maxLine - startHight;
                RotateAnimation animation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(durationMillis);
                animation.setFillAfter(true);
                mImage.startAnimation(animation);
            }

            Animation animation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    mText.setHeight((int) (startHight + tempHight * interpolatedTime+DensityUtils
                            .dp2px(HelpActivity.this,3)));

                }
            };
            animation.setDuration(durationMillis);
            mText.startAnimation(animation);

        }
    }
}
