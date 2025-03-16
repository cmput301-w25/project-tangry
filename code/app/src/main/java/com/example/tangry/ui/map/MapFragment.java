package com.example.tangry.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.tangry.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private static final double DEFAULT_ZOOM = 10.0;
    // Fallback location: Example uses San Francisco (adjust as needed)
    private static final GeoPoint FALLBACK_LOCATION = new GeoPoint(37.7749, -122.4194);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set the OsmDroid configuration with your app's user agent
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());

        // Inflate the layout (see XML section below)
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map);

        // Enable multi-touch controls (for pinch-to-zoom) and disable built-in zoom buttons
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // Use CartoDB Voyager tile source for a more vibrant map appearance
        OnlineTileSourceBase cartoVoyager = new XYTileSource("CartoDB Voyager",
                0, 20, 256, ".png", new String[]{"https://basemaps.cartocdn.com/rastertiles/voyager/"});
        mapView.setTileSource(cartoVoyager);

        // Set default zoom level
        mapView.getController().setZoom(DEFAULT_ZOOM);

        // Optionally, add a scale bar overlay
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(10, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        // Optionally, add a compass overlay
        CompassOverlay compassOverlay = new CompassOverlay(getContext(),
                new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Optional: Setup custom zoom buttons if defined in your layout
        ImageButton btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        ImageButton btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        if (btnZoomIn != null && btnZoomOut != null) {
            btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
            btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());
        }

        // Check for location permissions and request if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupLocationOverlay();
        }

        return view;
    }

    private void setupLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Once a valid location fix is obtained, center the map on the user's location
        myLocationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    GeoPoint currentLocation = myLocationOverlay.getMyLocation();
                    if (currentLocation != null) {
                        mapView.getController().animateTo(currentLocation);
                    } else {
                        mapView.getController().animateTo(FALLBACK_LOCATION);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationOverlay();
            } else {
                Toast.makeText(getContext(),
                        "Location permission is required to display your current location.",
                        Toast.LENGTH_LONG).show();
                mapView.getController().animateTo(FALLBACK_LOCATION);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
