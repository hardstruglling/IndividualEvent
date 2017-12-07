package com.hipad.bluetoothantilost.tool;

import android.app.Activity;
import android.view.View;

import com.hipad.bluetoothantilost.R;
import com.hipad.bluetoothantilost.avi.AVLoadingIndicatorView;
import com.hipad.bluetoothantilost.view.RotateLoading;

/**
 * Created by wk
 */

public class ViewUtils {
    /**
     * Get Load Wait View
     * @param context
     * @return
     */
    public static View getLoadingView(Activity context){
        View view = context.getLayoutInflater().inflate(R.layout.loading_view, null);
        AVLoadingIndicatorView loading = (AVLoadingIndicatorView)view.findViewById(R.id.ai_loading_view);
        loading.show();
        view.setBackgroundColor(context.getResources().getColor(R.color.transparent4));
        view.setOnClickListener(null);
        return view;
    }

    /**
     * Get Check for updates to wait for view
     * @param context
     * @return
     */
    public static View getUpDateLoadingView(Activity context){
        View view = context.getLayoutInflater().inflate(R.layout.update_loading_view, null);
        RotateLoading rotate = (RotateLoading) view.findViewById(R.id.rotateloading);
        rotate.start();
        view.setBackgroundColor(context.getResources().getColor(R.color.transparent4));
        view.setOnClickListener(null);
        return view;
    }
}
