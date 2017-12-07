package com.hipad.bluetoothantilost.tool;

import android.content.Context;
import android.view.View;

import com.hipad.bluetoothantilost.view.MaterialDialog;

/**
 * Created by wk
 */

public class DialogUtils {

    private static MaterialDialog dialog;

    /**
     * Encapsulates the static method of the dialog
     * @param context
     * @param title
     * @param content
     * @param right
     * @param left
     * @param listener
     */
    public static void show(Context context, String title, String content, String right, String left,
                     final OnDialogClickListener listener){
        dialog = new MaterialDialog(context)
                .setTitle(title)
                .setMessage(content)
                .setNegativeButton(left, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onCancelClick();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(right, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onSureClick();
                        dialog.dismiss();
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

    /**
     * Button to click on the callback event
     */
    public interface OnDialogClickListener{
        void onSureClick();
        void onCancelClick();
    }
}
