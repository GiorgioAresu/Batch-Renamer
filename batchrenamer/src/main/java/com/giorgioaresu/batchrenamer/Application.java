package com.giorgioaresu.batchrenamer;

import android.content.Context;

public class Application extends android.app.Application {

    private static Context mContext;

    public static Context getContext() {
        return Application.mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Application.mContext = getApplicationContext();
    }
}
