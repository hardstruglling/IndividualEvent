package com.hipad.bluetoothantilost.tool;

import android.content.Context;
import android.util.TypedValue;

/**
 * create by wk
 */
public class DensityUtils {

	private DensityUtils()
	{
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	
	/**
	 * dp to px
	 * 
	 * @param context
	 * @param dp
	 * @return
	 */
	public static int dp2px(Context context, float dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
				.getDisplayMetrics());
	}


	/**
	 * px to dp
	 * @param context
	 * @param pxVal
	 * @return
	 */
	public static float px2dp(Context context, float pxVal)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (pxVal / scale);
	}

}
