package com.example.tangry.ui.map;

import android.Manifest;
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
import android.util.Log;
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
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.utils.GeocoderUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final double DEFAULT_ZOOM = 15.0;
    // Fallback location (set to Edmonton)
    private static final GeoPoint FALLBACK_LOCATION = new GeoPoint(53.535999, -113.500205);

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private EmotionPostController emotionPostController;
    private EmotionPostRepository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set OSMDroid configuration (ensure you have set your user agent)
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map);

        // Disable map repetition
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);

        // Enable multi-touch and hide default zoom buttons
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // Set the tile source
        OnlineTileSourceBase tileSource = new XYTileSource("CartoDB Voyager",
                0, 20, 256, ".png", new String[]{"https://basemaps.cartocdn.com/rastertiles/voyager/"});
        mapView.setTileSource(tileSource);
        mapView.getController().setZoom(DEFAULT_ZOOM);

        // Add overlays: scale bar and compass
        mapView.getOverlays().add(new ScaleBarOverlay(mapView));
        CompassOverlay compassOverlay = new CompassOverlay(getContext(),
                new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Setup custom zoom buttons
        ImageButton btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        ImageButton btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        if (btnZoomIn != null && btnZoomOut != null) {
            btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
            btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());
        }

        // Optional: set a MapListener (if needed)
        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) { return false; }
            @Override
            public boolean onZoom(ZoomEvent event) { return false; }
        });

        // Check location permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupLocationOverlay();
        }

        // Initialize controllers and repository
        emotionPostController = new EmotionPostController();
        repository = EmotionPostRepository.getInstance();

        // Load posts and add markers
        loadFollowedMoodEventPins();

        return view;
    }

    private void setupLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    GeoPoint currentLocation = myLocationOverlay.getMyLocation();
                    // Force fallback if device location is outside expected Edmonton bounds
                    if (currentLocation == null ||
                            currentLocation.getLatitude() < 53.4 ||
                            currentLocation.getLatitude() > 53.7) {
                        Log.d(TAG, "Forcing fallback location due to invalid device location.");
                        currentLocation = FALLBACK_LOCATION;
                    }
                    mapView.getController().animateTo(currentLocation);
                    setMapBoundaries(currentLocation, 5.0);
                });
            }
        });
    }

    private void setMapBoundaries(GeoPoint center, double radiusKm) {
        double lat = center.getLatitude();
        double lon = center.getLongitude();
        double latOffset = radiusKm / 111.0;
        double lonOffset = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        BoundingBox bb = new BoundingBox(
                lat + latOffset,
                lon + lonOffset,
                lat - latOffset,
                lon - lonOffset
        );
        mapView.setScrollableAreaLimitDouble(bb);
        double currentZoom = mapView.getZoomLevelDouble();
        mapView.setMinZoomLevel(currentZoom);
    }

    /**
     * Loads posts from the current user and followed users,
     * geocodes their location strings, and adds emoji markers to the map.
     */
    private void loadFollowedMoodEventPins() {
        // Replace these stub methods with your actual implementations.
        List<String> followedUsers = getFollowedUsernames();
        String currentUsername = getCurrentUsername();

        // Query posts (no emotion filter used here)
        Query query = repository.getFilteredPostsQuery(new ArrayList<>());
        query.get().addOnSuccessListener(querySnapshot -> {
            Map<String, EmotionPost> latestEventPerUser = new HashMap<>();
            GeoPoint currentLocation = myLocationOverlay.getMyLocation();
            if (currentLocation == null ||
                    currentLocation.getLatitude() < 53.4 ||
                    currentLocation.getLatitude() > 53.7) {
                currentLocation = FALLBACK_LOCATION;
            }
            Log.d(TAG, "Filtering posts using location: " +
                    currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

            // Loop through posts and filter by user and distance
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                EmotionPost post = document.toObject(EmotionPost.class);
                if (post != null && post.getLocation() != null && !post.getLocation().trim().isEmpty()) {
                    String username = post.getUsername();
                    // Only include posts from the current user or followed users.
                    if (!username.equals(currentUsername) && !followedUsers.contains(username)) {
                        Log.d(TAG, "Skipping post from " + username + " (not current user or followed).");
                        continue;
                    }
                    // Geocode the post location.
                    GeoPoint eventPoint = GeocoderUtil.getGeoPointFromAddress(getContext(), post.getLocation());
                    if (eventPoint == null) {
                        Log.d(TAG, "Geocoder returned null for location: " + post.getLocation() +
                                " for post by " + username + ". Skipping marker.");
                        continue;
                    }
                    // For followed users, check distance.
                    if (!username.equals(currentUsername)) {
                        double distance = distanceInKm(currentLocation, eventPoint);
                        Log.d(TAG, "Post from " + username + " is " + distance + " km away.");
                        if (distance > 5.0) {
                            Log.d(TAG, "Skipping post from " + username + " (distance " + distance + " km > 5 km)");
                            continue;
                        }
                    }
                    // Add the first (latest) post per user.
                    if (!latestEventPerUser.containsKey(username)) {
                        latestEventPerUser.put(username, post);
                        Log.d(TAG, "Added post from " + username);
                    }
                }
            }

            // If no qualifying posts, add a dummy marker.
            if (latestEventPerUser.isEmpty()) {
                Log.d(TAG, "No qualifying posts found; adding dummy marker at fallback.");
                Marker dummyMarker = new Marker(mapView);
                dummyMarker.setPosition(FALLBACK_LOCATION);
                dummyMarker.setTitle("No Posts Found");
                mapView.getOverlays().add(dummyMarker);
            } else {
                // Create markers for each qualifying post.
                for (EmotionPost post : latestEventPerUser.values()) {
                    GeoPoint point = GeocoderUtil.getGeoPointFromAddress(getContext(), post.getLocation());
                    if (point == null) {
                        Log.d(TAG, "Geocoder returned null for post from " + post.getUsername() + ". Skipping marker.");
                        continue;
                    }
                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    int emojiResId = getIconForEmotion(post.getEmotion());
                    Drawable pinDrawable = createPinDrawable(emojiResId);
                    marker.setIcon(pinDrawable);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(post.getUsername());
                    marker.setOnMarkerClickListener((m, mapView) -> {
                        showPostDetails(post);
                        return true;
                    });
                    Log.d(TAG, "Adding marker for " + post.getUsername() + " at " +
                            point.getLatitude() + ", " + point.getLongitude());
                    mapView.getOverlays().add(marker);
                }
            }
            mapView.invalidate();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load posts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // Stub: Replace with your actual implementation to return followed usernames.
    private List<String> getFollowedUsernames() {
        List<String> followed = new ArrayList<>();
        // Example: add followed usernames (update as needed)
        followed.add("user1");
        followed.add("user2");
        return followed;
    }

    // Stub: Replace with your actual implementation to return the current user's username.
    private String getCurrentUsername() {
        // Example: return "Liam" (the current user's username)
        return "Liam";
    }

    private double distanceInKm(GeoPoint point1, GeoPoint point2) {
        return point1.distanceToAsDouble(point2) / 1000.0;
    }

    private int getIconForEmotion(String emotionName) {
        List<Emotion> emotions = EmotionProvider.getSampleEmotions();
        for (Emotion e : emotions) {
            if (e.getName().equalsIgnoreCase(emotionName)) {
                return e.getIconResId();
            }
        }
        return R.drawable.ic_angry_selector; // fallback icon.
    }

    /**
     * Creates a composite drawable that overlays a scaled-down emoji on a pin background.
     */
    private Drawable createPinDrawable(int emojiDrawableResId) {
        Drawable pinBackground = ContextCompat.getDrawable(getContext(), R.drawable.map_pin_background);
        if (pinBackground == null) {
            GradientDrawable fallbackBackground = new GradientDrawable();
            fallbackBackground.setShape(GradientDrawable.OVAL);
            fallbackBackground.setColor(Color.LTGRAY);
            int size = dpToPx(40);
            fallbackBackground.setSize(size, size);
            pinBackground = fallbackBackground;
        }
        // Ensure valid bounds on the background drawable.
        if (pinBackground.getIntrinsicWidth() <= 0 || pinBackground.getIntrinsicHeight() <= 0) {
            int defaultSize = dpToPx(40);
            pinBackground.setBounds(0, 0, defaultSize, defaultSize);
        }

        Drawable emojiDrawable = ContextCompat.getDrawable(getContext(), emojiDrawableResId);
        Bitmap emojiBitmap = drawableToBitmap(emojiDrawable);
        int emojiSizePx = dpToPx(20);
        Bitmap scaledEmojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, emojiSizePx, emojiSizePx, true);
        Drawable scaledEmojiDrawable = new BitmapDrawable(getResources(), scaledEmojiBitmap);

        Drawable[] layers = new Drawable[2];
        layers[0] = pinBackground;
        layers[1] = scaledEmojiDrawable;
        LayerDrawable layeredDrawable = new LayerDrawable(layers);
        int width = (pinBackground.getIntrinsicWidth() > 0) ? pinBackground.getIntrinsicWidth() : dpToPx(40);
        int height = (pinBackground.getIntrinsicHeight() > 0) ? pinBackground.getIntrinsicHeight() : dpToPx(40);
        layeredDrawable.setBounds(0, 0, width, height);

        int insetX = (width - emojiSizePx) / 2;
        int insetY = (height - emojiSizePx) / 2;
        layeredDrawable.setLayerInset(1, insetX, insetY, insetX, insetY);
        return layeredDrawable;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) width = dpToPx(40);
        if (height <= 0) height = dpToPx(40);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }

    private void showPostDetails(EmotionPost post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(post.getEmotion());
        StringBuilder details = new StringBuilder();
        details.append("Posted by: ").append(post.getUsername()).append("\n");
        details.append("Explanation: ").append(post.getExplanation());
        builder.setMessage(details.toString());
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
