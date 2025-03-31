package com.example.tangry.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder; // Android's built-in Geocoder
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocoderUtility { // Renamed to avoid confusion with Android's Geocoder
    private static final String TAG = "GeocoderUtil";

    /**
     * Returns a GeoPoint based on the provided address string.
     * Requests up to 5 results and uses the first match.
     *
     * @param context  the application context
     * @param location the address string to geocode
     * @return a GeoPoint representing the location, or null if not found or if the service is not present.
     */
    public static GeoPoint getGeoPointFromAddress(Context context, String location) {
        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder service is not present on this device.");
            return null;
        }

        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            // Request up to 5 possible results
            List<Address> addresses = geoCoder.getFromLocationName(location, 5);
            if (addresses != null && !addresses.isEmpty()) {
                Address bestAddress = addresses.get(0);
                Log.d(TAG, "Address found for \"" + location + "\": " +
                        bestAddress.getAddressLine(0) + " (" +
                        bestAddress.getLatitude() + ", " + bestAddress.getLongitude() + ")");
                return new GeoPoint(bestAddress.getLatitude(), bestAddress.getLongitude());
            } else {
                Log.e(TAG, "No addresses found for \"" + location + "\"");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed for \"" + location + "\"", e);
        }
        return null;
    }
}
