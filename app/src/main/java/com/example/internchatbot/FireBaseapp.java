package com.example.internchatbot;

import com.google.firebase.database.FirebaseDatabase;

public class FireBaseapp extends android.app.Application {
    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
