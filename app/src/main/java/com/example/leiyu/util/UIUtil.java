package com.example.leiyu.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by leiyu on 2018/3/23.
 */

public class UIUtil {

    public static final int getScreenWidth(Context context) {
        if(context == null) {
            return 0;
        } else {
            DisplayMetrics metric = new DisplayMetrics();
            WindowManager wm = (WindowManager)context.getSystemService("window");
            wm.getDefaultDisplay().getMetrics(metric);
            int width = metric.widthPixels;
            return width;
        }
    }

    public static int getScreenHeight(Context context) {
        if(context == null) {
            return 0;
        } else {
            DisplayMetrics metric = new DisplayMetrics();
            WindowManager wm = (WindowManager)context.getSystemService("window");
            wm.getDefaultDisplay().getMetrics(metric);
            int height = metric.heightPixels;
            return height;
        }
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
