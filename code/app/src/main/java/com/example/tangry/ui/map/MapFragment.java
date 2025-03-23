package com.example.tangry.ui.map;

import android.Manifest;
import com.bumptech.glide.Glide;
import android.view.LayoutInflater;
import org.osmdroid.util.BoundingBox;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import com.example.tangry.R;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.Emotion;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.models.EmotionProvider;
import com.example.tangry.utils.Geocoder; // our utility class (named Geocoder)
import com.google.firebase.firestore.DocumentSnapshot;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
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
import org.osmdroid.views.overlay.Marker;
import java.util.List;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private static final double DEFAULT_ZOOM = 15.0;
    // Fallback location (adjust as needed)
    private static final GeoPoint FALLBACK_LOCATION = new GeoPoint(53.535999, -113.500205);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set up OsmDroid configuration with your app's user agent.
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());

        // Inflate the layout.
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map);

        // Disable map repetition.
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);

        // Enable multi-touch and disable built-in zoom buttons.
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // Set the tile source.
        OnlineTileSourceBase cartoVoyager = new XYTileSource("CartoDB Voyager",
                0, 20, 256, ".png", new String[]{"https://basemaps.cartocdn.com/rastertiles/voyager/"});
        mapView.setTileSource(cartoVoyager);
        mapView.getController().setZoom(DEFAULT_ZOOM);

        // Add overlays.
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(10, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(getContext(),
                new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Setup custom zoom buttons.
        ImageButton btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        ImageButton btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        if (btnZoomIn != null && btnZoomOut != null) {
            btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
            btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());
        }

        mapView.post(() -> {
            // Define a fixed area around your center coordinate.
            double centerLat = FALLBACK_LOCATION.getLatitude();
            double centerLon = FALLBACK_LOCATION.getLongitude();
            // delta: half the width/height in degrees (adjust as needed)
            double delta = 0.5; // This creates a 1° x 1° area

            BoundingBox fixedBB = new BoundingBox(
                    centerLat + delta,   // North
                    centerLon + delta,   // East
                    centerLat - delta,   // South
                    centerLon - delta    // West
            );

            mapView.setScrollableAreaLimitDouble(fixedBB);

            // Lock zoom-out by setting the minimum zoom level to the current zoom level.
            double currentZoom = mapView.getZoomLevelDouble();
            mapView.setMinZoomLevel(currentZoom);
        });




        // Set a MapListener if you need to handle zoom or scroll events.
        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }
            @Override
            public boolean onZoom(ZoomEvent event) {
                // Optionally update markers dynamically on zoom.
                return false;
            }
        });

        // Check location permissions.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupLocationOverlay();
        }

        // Load EmotionPost pins.
        loadEmotionPostPins();

        return view;
    }


    private void setupLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Get the current location; if not available, use the fallback.
                    GeoPoint currentLocation = myLocationOverlay.getMyLocation();
                    if (currentLocation == null) {
                        currentLocation = FALLBACK_LOCATION;
                    }

                    // Calculate a 5km radius in degrees.
                    // 1 degree of latitude is approximately 111km.
                    double deltaLat = 5.0 / 111.0; // approx 0.045 degrees
                    // For longitude, the distance per degree is 111km * cos(latitude)
                    double deltaLon = 5.0 / (111.0 * Math.cos(Math.toRadians(currentLocation.getLatitude())));

                    // Create the bounding box (north, east, south, west).
                    BoundingBox fixedBB = new BoundingBox(
                            currentLocation.getLatitude() + deltaLat,   // North
                            currentLocation.getLongitude() + deltaLon,    // East
                            currentLocation.getLatitude() - deltaLat,   // South
                            currentLocation.getLongitude() - deltaLon     // West
                    );

                    // Set the scrollable area limit to this bounding box.
                    mapView.setScrollableAreaLimitDouble(fixedBB);

                    // Optionally, lock zoom out by setting the minimum zoom level to the current zoom.
                    double currentZoom = mapView.getZoomLevelDouble();
                    mapView.setMinZoomLevel(currentZoom);

                    // Optionally, center the map at the current location.
                    mapView.getController().animateTo(currentLocation);
                });
            }
        });
    }


    private void loadEmotionPostPins() {
        EmotionPostController controller = new EmotionPostController();
        controller.getPostsQuery().get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                EmotionPost post = document.toObject(EmotionPost.class);
                if (post != null && post.getLocation() != null && !post.getLocation().trim().isEmpty()) {
                    // Convert location string to GeoPoint using our Geocoder utility.
                    GeoPoint point = Geocoder.getGeoPointFromAddress(getContext(), post.getLocation());
                    if (point != null) {
                        Marker marker = new Marker(mapView);
                        marker.setPosition(point);
                        // Create a composite pin drawable using the emoji.
                        int emojiResId = getIconForEmotion(post.getEmotion());
                        Drawable pinDrawable = createPinDrawable(emojiResId);
                        marker.setIcon(pinDrawable);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        // Set a click listener to display post details.
                        marker.setOnMarkerClickListener((m, mapView) -> {
                            showPostDetails(post);
                            return true;
                        });
                        mapView.getOverlays().add(marker);
                    }
                }
            }
            mapView.invalidate();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
        );
    }

    // Helper method: Map emotion name to its corresponding drawable resource.
    private int getIconForEmotion(String emotionName) {
        List<Emotion> emotions = EmotionProvider.getSampleEmotions();
        for (Emotion e : emotions) {
            if (e.getName().equalsIgnoreCase(emotionName)) {
                return e.getIconResId();
            }
        }
        return R.drawable.ic_angry_selector; // fallback icon
    }

    /**
     * Creates a composite drawable that overlays a scaled-down emoji (20dp x 20dp) on top of a pin background.
     * The pin background is defined in res/drawable/map_pin_background.xml.
     */
    private Drawable createPinDrawable(int emojiDrawableResId) {
        // Obtain the pin background drawable.
        Drawable pinBackground = ContextCompat.getDrawable(getContext(), R.drawable.map_pin_background);
        if (pinBackground == null) {
            // Fallback: create an oval background programmatically.
            GradientDrawable fallbackBackground = new GradientDrawable();
            fallbackBackground.setShape(GradientDrawable.OVAL);
            fallbackBackground.setColor(Color.WHITE);
            int size = dpToPx(40);
            fallbackBackground.setSize(size, size);
            pinBackground = fallbackBackground;
        }
        // Obtain the emoji drawable.
        Drawable emojiDrawable = ContextCompat.getDrawable(getContext(), emojiDrawableResId);
        // Convert any drawable to a Bitmap.
        Bitmap emojiBitmap = drawableToBitmap(emojiDrawable);
        // Define desired emoji size (20dp x 20dp).
        int emojiSizePx = dpToPx(20);
        Bitmap scaledEmojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, emojiSizePx, emojiSizePx, true);
        Drawable scaledEmojiDrawable = new BitmapDrawable(getResources(), scaledEmojiBitmap);

        // Combine the pin background and the scaled emoji using a LayerDrawable.
        Drawable[] layers = new Drawable[2];
        layers[0] = pinBackground;
        layers[1] = scaledEmojiDrawable;
        LayerDrawable layeredDrawable = new LayerDrawable(layers);
        // Center the emoji overlay on the pin background.
        int pinWidth = pinBackground.getIntrinsicWidth();
        int pinHeight = pinBackground.getIntrinsicHeight();
        int insetX = (pinWidth - emojiSizePx) / 2;
        int insetY = (pinHeight - emojiSizePx) / 2;
        layeredDrawable.setLayerInset(1, insetX, insetY, insetX, insetY);
        return layeredDrawable;
    }

    /**
     * Helper method to convert any Drawable into a Bitmap.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) {
            width = dpToPx(40);
        }
        if (height <= 0) {
            height = dpToPx(40);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Helper method to convert dp to pixels.
     */
    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }

    /**
     * Displays an AlertDialog with the details of the EmotionPost when a marker is clicked.
     */
    private void showPostDetails(EmotionPost post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(post.getEmotion());

        // Inflate the custom dialog layout.
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_post_details, null);
        TextView tvDetails = dialogView.findViewById(R.id.tvDetails);
        ImageView ivPostImage = dialogView.findViewById(R.id.ivPostImage);

        // Set the text details.
        StringBuilder details = new StringBuilder();
        details.append("Posted by: ").append(post.getUsername()).append("\n");
        details.append("Explanation: ").append(post.getExplanation());
        tvDetails.setText(details.toString());

        // Load the image if the URI is available.
        if (post.getImageUri() != null && !post.getImageUri().trim().isEmpty()) {
            ivPostImage.setVisibility(View.VISIBLE);
            // Use Glide (or Picasso) to load the image.
            Glide.with(getContext())
                    .load(post.getImageUri())
                    .into(ivPostImage);
        } else {
            ivPostImage.setVisibility(View.GONE);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
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
