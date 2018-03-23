package com.example.bindview_api.proxy;

/**
 * Created by leiyu on 2018/3/19.
 */

public class CustomerA implements FoodProvider{

    @Override
    public void drinkWater(String water) {
        System.out.println(water);
    }

    @Override
    public void eatFood(int food) {
        System.out.println("吃饭" + food);
    }
}
