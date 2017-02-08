package com.moliying.mly_tracklinedemo;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * description:
 * company: moliying.com
 * Created by vince on 2017/2/7.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
    }
}
