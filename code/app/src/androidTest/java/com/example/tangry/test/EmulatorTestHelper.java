package com.example.tangry.test;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.auth.FirebaseAuth;

public class EmulatorTestHelper {
    private static final String EMULATOR_HOST = "10.0.2.2";
    private static final int FIRESTORE_PORT = 8080;
    private static final int AUTH_PORT = 9099;

    public static void useFirebaseEmulators() {
        // Configure Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost(EMULATOR_HOST + ":" + FIRESTORE_PORT)
                .setSslEnabled(false)
                .setPersistenceEnabled(false)
                .build();
        firestore.setFirestoreSettings(settings);
        // Configure Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useEmulator(EMULATOR_HOST, AUTH_PORT);
    }
}