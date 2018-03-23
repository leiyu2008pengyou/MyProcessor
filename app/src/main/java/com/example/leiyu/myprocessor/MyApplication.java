package com.example.leiyu.myprocessor;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.example.leiyu.service.LaifengServiceImpl;

/**
 * Created by leiyu on 2018/3/22.
 */

public class MyApplication extends Application {
    private static MyApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        LaifengServiceImpl.getInstance();
    }

    public Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            String activityname = activity.getClass().getName();
            Log.i("myapplication", activityname);
            System.out.println("111111");
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
