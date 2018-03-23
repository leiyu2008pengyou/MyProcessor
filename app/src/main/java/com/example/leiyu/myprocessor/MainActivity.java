package com.example.leiyu.myprocessor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BindLayout;
import com.example.BindView;
import com.example.OnClick;
import com.example.PermissionCheck;
import com.example.PermissionGranted;
import com.example.bindview_api.finder.LyViewInjector;
import com.example.leiyu.service.Ilogin;
import com.example.leiyu.service.LaifengService;

@BindLayout(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_CODE = 0x01;

    @BindView(R.id.id_textview)
    TextView mTextView;

    @BindView(R.id.id_btn)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LyViewInjector.lyInject(this);
    }

    @OnClick({R.id.id_textview, R.id.id_btn})
    public void ok() {
        Toast.makeText(this, "MyProcessor", Toast.LENGTH_SHORT).show();
        showCamera();
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
        LaifengService.getService(Ilogin.class).login();
        System.out.println("22222222");
    }

    @PermissionCheck({Manifest.permission.CAMERA})
    public void showCamera(){
        boolean hasPermission = LyViewInjector.hasPermission(this, new String[]{Manifest.permission.CAMERA});
        if(hasPermission){
            Toast.makeText(this, "摄像头权限设置成功", Toast.LENGTH_SHORT).show();
        }else{
            LyViewInjector.requestPermission(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
        }
    }

    @PermissionGranted({CAMERA_CODE})
    public void grantedMethod(){

    }
}
