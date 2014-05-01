package com.giorgioaresu.batchrenamer;

import android.content.Context;

public class Application extends android.app.Application {
    public static final String EXTERNAL_FOLDER = "batch renamer/";
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
    }
}
