package com.example.bindview_api.finder;

/**
 * Created by leiyu on 2018/1/31.
 */

public interface LyInjector<T> {

    //绑定Layout，view， click
    void lyInject(T host, Object source, Finder finder);

    //判断是否有某些权限
    boolean hasPermission(T host, String[] permissions);

    //申请权限
    void requestPermission(T host, String[] permissions, int code);

    //允许权限
    //void granted();

    //拒绝权限
    //void denied();
}
