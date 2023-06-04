package com.jiokye.tankbattle_multiplayer.activity;

import android.app.Application;
import android.content.Context;

public class TankApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        TankApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return TankApplication.context;
    }


}
