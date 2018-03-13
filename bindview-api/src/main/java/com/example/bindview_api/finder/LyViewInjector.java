package com.example.bindview_api.finder;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leiyu on 2018/1/31.
 */

public class LyViewInjector {
    private static final ActivityFinder ACTIVITY_FINDER = new ActivityFinder();
    private static final ViewFinder VIEW_FINDER = new ViewFinder();
    private static final Map<String, LyInjector> FINDER_MAP = new HashMap<>();
    private static Map<String, String> METHOD_MAP = new HashMap<>();

    public static void lyInject(Activity activity){
        lyInject(activity, activity, ACTIVITY_FINDER);
    }

    public static void lyInject(Object host, View view){
        lyInject(host, view, VIEW_FINDER);
    }

    public static void lyInject(Object host, Object source, Finder finder){
        String className = host.getClass().getName();

        try{
            LyInjector lyInjector = FINDER_MAP.get(className);
            if(lyInjector == null){
                Class<?> finderClass = Class.forName(className + "$LeiyuInjector");
                lyInjector = (LyInjector) finderClass.newInstance();
                FINDER_MAP.put(className, lyInjector);
            }
            lyInjector.lyInject(host, source, finder);
        }catch (Exception ex){
            throw new RuntimeException("Unable to jnject for " + className, ex);
        }
    }

    public static boolean hasPermission(Object host, String[] permissions){
        boolean flag = true;
        String className = host.getClass().getName();
        try{
            LyInjector lyInjector = FINDER_MAP.get(className);
            if(lyInjector == null){
                Class<?> finderClass = Class.forName(className + "$LeiyuInjector");
                lyInjector = (LyInjector) finderClass.newInstance();
                FINDER_MAP.put(className, lyInjector);
            }
            //Method[] methodName = lyInjector.getClass().getMethods();
            flag = lyInjector.hasPermission(host, permissions);
        }catch (Exception ex){
            throw new RuntimeException("Unable to jnject for " + className, ex);
        }
        return flag;
    }

    public static void requestPermission(Object host, String[] permissions, int code){
        String className = host.getClass().getName();
        try{
            LyInjector lyInjector = FINDER_MAP.get(className);
            if(lyInjector == null){
                Class<?> finderClass = Class.forName(className + "$LeiyuInjector");
                lyInjector = (LyInjector) finderClass.newInstance();
                FINDER_MAP.put(className, lyInjector);
            }
            lyInjector.requestPermission(host, permissions, code);
        }catch (Exception ex){
            throw new RuntimeException("Unable to jnject for " + className, ex);
        }
    }
}
