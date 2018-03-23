package com.example.leiyu.service;

/**
 * Created by leiyu on 2018/3/23.
 */

public abstract class LaifengService {
    protected volatile static LaifengService instance;

    public static <T> T getService(Class<T> clazz){
        if(instance != null){
            return instance.getServiceImpl(clazz);
        }
        return null;
    }

    public abstract <T> T getServiceImpl(Class<T> clazz);
}
