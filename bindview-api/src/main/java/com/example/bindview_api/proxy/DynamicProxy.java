package com.example.bindview_api.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by leiyu on 2018/3/19.
 */

public class DynamicProxy implements InvocationHandler {

    private Object target;

    public DynamicProxy(Object object){
        this.target = object;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Type types[] = method.getParameterTypes();
        if(method.getName().matches("eat.+") && types.length == 1 && (types[0] == int.class)) {
            System.out.println("动态代理吃东西");
            return  method.invoke(target, objects);
        }
        return method.invoke(target, objects);
    }
}
