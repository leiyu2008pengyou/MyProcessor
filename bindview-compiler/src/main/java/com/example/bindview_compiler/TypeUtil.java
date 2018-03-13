package com.example.bindview_compiler;

import com.squareup.javapoet.ClassName;

/**
 * Created by leiyu on 2018/1/31.
 */

public class TypeUtil {

    public static final ClassName FINDER = ClassName.get("com.example.bindview_api.finder", "Finder");
    public static final ClassName INJECTOR = ClassName.get("com.example.bindview_api.finder", "LyInjector");
    public static final ClassName ANDROID_ON_CLICK_LISTENER = ClassName.get("android.view", "View", "OnClickListener");
    public static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");
    public static final ClassName ANDROID_CONTEXTCOMPAT = ClassName.get("android.support.v4.content", "ContextCompat");
    public static final ClassName ANDROID_PACKAGEMANAGER = ClassName.get("android.content.pm", "PackageManager");
    public static final ClassName ANDROID_ACTIVITYCOMPAT =  ClassName.get("android.support.v4.app", "ActivityCompat");
}
