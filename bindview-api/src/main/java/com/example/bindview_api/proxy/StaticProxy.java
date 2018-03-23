package com.example.bindview_api.proxy;

/**
 * Created by leiyu on 2018/3/19.
 */

public class StaticProxy implements FoodProvider{

    FoodProvider mFoodProvider;
    public StaticProxy(FoodProvider mFoodProvider){
        this.mFoodProvider = mFoodProvider;
    }

    @Override
    public void drinkWater(String water) {
        System.out.println("喝水前先拿杯子");
        mFoodProvider.drinkWater(water);
    }

    @Override
    public void eatFood(int food) {
        System.out.println("吃饭前先洗手");
        mFoodProvider.eatFood(food);
    }
}
