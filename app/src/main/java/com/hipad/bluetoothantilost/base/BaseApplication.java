package com.hipad.bluetoothantilost.base;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wk
 */

public class BaseApplication extends Application{

    private static class ApplicationHolder{
        public static BaseApplication INSTANCE = new BaseApplication();
    }
    private List<Activity> mList = new LinkedList<>();
    public static BaseApplication getInstance(){
        return ApplicationHolder.INSTANCE;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void addActivity(Activity activity){
        mList.add(activity);
    }

    public void exit(){
        try {
            for (Activity activity : mList){
                if (activity != null) activity.finish();
            }
            mList = null;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.exit(0);
        }
    }
}
