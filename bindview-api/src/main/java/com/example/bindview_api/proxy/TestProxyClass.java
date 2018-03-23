package com.example.bindview_api.proxy;

import java.lang.reflect.Proxy;

/**
 * Created by leiyu on 2018/3/19.
 */

public class TestProxyClass {
    public static void main(String args[]){
        /*FoodProvider foodProvider = new StaticProxy(new CustomerA());
        foodProvider.drinkWater("可口可乐");
        foodProvider.eatFood(3);*/

        FoodProvider customerA = new CustomerA();
        //Class<FoodProvider> customerA = FoodProvider.class;
        DynamicProxy dynamicProxy = new DynamicProxy(customerA);
        FoodProvider foodProvider1 = (FoodProvider) Proxy.newProxyInstance(customerA.getClass().getClassLoader(),
                customerA.getClass().getInterfaces(), dynamicProxy);
        foodProvider1.drinkWater("可口可乐");
        foodProvider1.eatFood(3);
        String a = "abc";
        String b = "abc";
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(a==b);
    }
}
