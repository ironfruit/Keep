package com.irondigitalmedia.keep.Application;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Keep extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
