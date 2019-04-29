package com.example.contourdetector;

import android.app.Application;

import com.bravin.btoast.BToast;

public class ApplicationInit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BToast.Config.getInstance().apply(this);
    }

}
