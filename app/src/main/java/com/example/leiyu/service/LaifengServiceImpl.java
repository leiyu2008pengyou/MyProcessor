package com.example.leiyu.service;

import java.util.HashMap;

/**
 * Created by leiyu on 2018/3/23.
 */

public class LaifengServiceImpl extends LaifengService{

    private static HashMap<String, Object> services = new HashMap<>();
    private LaifengServiceImpl(){
        services.put(Ilogin.class.getName(), new LoginImpl());
    }

    public static LaifengService getInstance(){
        if(instance == null){
            synchronized (LaifengServiceImpl.class){
                if(instance == null){
                    instance = new LaifengServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public <T> T getServiceImpl(Class<T> clazz) {
        if (services.containsKey(clazz.getName())) {
            return (T) services.get(clazz.getName());
        } else {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
