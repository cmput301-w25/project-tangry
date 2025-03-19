package com.example.tangry.utils;

import android.content.Context;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.osmdroid.util.GeoPoint;

public class Geocoder {
    public static GeoPoint getGeoPointFromAddress(Context context, String location) {
        // Fully qualify Android's Geocoder to avoid confusion with this class
        android.location.Geocoder geoCoder = new android.location.Geocoder(context, Locale.getDefault());
        try {
            // getFromLocationName takes two parameters: the location string and the max number of results to return
            List<Address> addresses = geoCoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return new GeoPoint(addr.getLatitude(), addr.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
