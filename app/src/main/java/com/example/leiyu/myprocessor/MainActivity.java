package com.example.leiyu.myprocessor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.BindLayout;
import com.example.bindview_api.finder.LyViewInjector;

@BindLayout(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LyViewInjector.lyInject(this);
        //setContentView(R.layout.activity_main);
    }
}
