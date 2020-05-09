package me.iweizi.stepchanger;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context ctx = null;
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
    }

    public static Context getContext() {
        return ctx;
    }

}
