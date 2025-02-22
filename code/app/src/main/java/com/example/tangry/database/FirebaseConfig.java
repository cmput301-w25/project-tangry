package com.example.tangry.database;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }

    public static FirebaseFirestore getFirestoreInstance() {
        return FirebaseFirestore.getInstance();
    }
}