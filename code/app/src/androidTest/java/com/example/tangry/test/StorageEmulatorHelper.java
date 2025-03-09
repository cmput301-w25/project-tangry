package com.example.tangry.test;

import com.google.firebase.storage.FirebaseStorage;

public class StorageEmulatorHelper {

    private static final String EMULATOR_HOST = "10.0.2.2";
    private static final int STORAGE_PORT = 9199;

    public static void useStorageEmulator() {
        // Connect to the Firebase Storage emulator
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.useEmulator(EMULATOR_HOST, STORAGE_PORT);

        // Set the storage bucket name for the emulator
        // The bucket name is typically your-project-id.appspot.com
        // But for emulator you can use any name
        try {
            java.lang.reflect.Field field = storage.getClass().getDeclaredField("mStorageBucket");
            field.setAccessible(true);
            field.set(storage, "test-bucket");
        } catch (Exception e) {
            System.err.println("Failed to set storage bucket: " + e);
        }
    }
}