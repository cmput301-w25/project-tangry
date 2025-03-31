package com.example.tangry.ui.map;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.graphics.drawable.BitmapDrawable;

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
import com.example.tangry.repositories.UserRepository;
import com.example.tangry.utils.GeocoderUtility;
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
                0, 20, 256, ".png", new String[] {"https://basemaps.cartocdn.com/rastertiles/voyager/"});
        mapView.setTileSource(tileSource);
        mapView.getController().setZoom(DEFAULT_ZOOM);

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
     * Loads posts from the current user and friends (public posts only),
     * geocodes their location strings, and adds custom emoji markers to the map.
     */
    private void loadFollowedMoodEventPins() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Log.d(TAG, "No current user available.");
            return;
        }
        String currentUserEmail = currentUser.getEmail();
        // Retrieve friends list asynchronously from UserRepository.
        UserRepository.getInstance().getFriendsList(currentUserEmail,
                friendsList -> {
                    // Combine friends with the current user's username.
                    List<String> validUsers = new ArrayList<>(friendsList);
                    String currentUsername = getCurrentUsername();
                    if (currentUsername != null && !currentUsername.equalsIgnoreCase("unknown")) {
                        validUsers.add(currentUsername);
                    }
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

                        // Loop through posts and filter by valid user and distance.
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            EmotionPost post = document.toObject(EmotionPost.class);
                            if (post != null && post.getLocation() != null && !post.getLocation().trim().isEmpty()) {
                                String username = post.getUsername();
                                if (username == null) {
                                    Log.d(TAG, "Skipping post with null username.");
                                    continue;
                                }
                                if (!validUsers.contains(username)) {
                                    Log.d(TAG, "Skipping post from " + username + " (not current user or friend).");
                                    continue;
                                }
                                // Geocode the post location.
                                GeoPoint eventPoint = GeocoderUtility.getGeoPointFromAddress(getContext(), post.getLocation());
                                if (eventPoint == null) {
                                    Log.d(TAG, "Geocoder returned null for location: " + post.getLocation() +
                                            " for post by " + username + ". Skipping marker.");
                                    continue;
                                }
                                // For friend posts (not current user), check distance.
                                if (!username.equals(currentUsername)) {
                                    double distance = distanceInKm(currentLocation, eventPoint);
                                    Log.d(TAG, "Post from " + username + " is " + distance + " km away.");
                                    if (distance > 5.0) {
                                        Log.d(TAG, "Skipping post from " + username + " (distance " + distance + " km > 5 km)");
                                        continue;
                                    }
                                }
                                // Only add the first (latest) post per user.
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
                            // Create markers for each qualifying post using our custom marker view.
                            for (EmotionPost post : latestEventPerUser.values()) {
                                GeoPoint point = GeocoderUtility.getGeoPointFromAddress(getContext(), post.getLocation());
                                if (point == null) {
                                    Log.d(TAG, "Geocoder returned null for post from " + post.getUsername() + ". Skipping marker.");
                                    continue;
                                }
                                Marker marker = createCustomMarker(post, point);
                                mapView.getOverlays().add(marker);
                            }
                        }
                        mapView.invalidate();
                    }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load posts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                },
                e -> {
                    Log.e(TAG, "Error loading friends list", e);
                    Toast.makeText(getContext(), "Error loading friends list.", Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Creates a custom marker for a given post and location.
     * This method inflates the custom layout (marker_custom.xml) which contains:
     * - An ImageView for the pin (with a tintable background)
     * - An ImageView for the emoji
     * - A TextView for the username (displayed below)
     *
     * @param post  the EmotionPost object (for username and emotion)
     * @param point the GeoPoint where the marker should be placed
     * @return a Marker with the custom icon
     */
    private Marker createCustomMarker(EmotionPost post, GeoPoint point) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);

        // Inflate the custom marker layout.
        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_custom, null);

        // Set the username text.
        android.widget.TextView usernameText = markerView.findViewById(R.id.marker_username);
        if (usernameText != null) {
            usernameText.setText(post.getUsername());
        } else {
            Log.e(TAG, "Marker layout missing TextView with ID marker_username!");
        }

        // Set the emoji image.
        android.widget.ImageView emojiImage = markerView.findViewById(R.id.marker_emoji);
        if (emojiImage != null) {
            int emojiResId = getIconForEmotion(post.getEmotion());
            emojiImage.setImageResource(emojiResId);
        } else {
            Log.e(TAG, "Marker layout missing ImageView with ID marker_emoji!");
        }

        // Set the pin background image and tint.
        android.widget.ImageView pinImage = markerView.findViewById(R.id.marker_pin);
        if (pinImage != null) {
            int emotionColor = getColorForEmotion(post.getEmotion());
            // Assume marker_pin drawable is the base shape – tint it.
            pinImage.setColorFilter(emotionColor);
        } else {
            Log.e(TAG, "Marker layout missing ImageView with ID marker_pin!");
        }

        // Convert the custom view to a bitmap.
        Bitmap markerBitmap = createBitmapFromView(markerView);
        marker.setIcon(new BitmapDrawable(getResources(), markerBitmap));
        // Anchor the marker so that the bottom center of the bitmap is the location point.
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        // Set a click listener to show post details.
        marker.setOnMarkerClickListener((m, mapView) -> {
            showPostDetails(post);
            return true;
        });
        Log.d(TAG, "Adding marker for " + post.getUsername() + " at " +
                point.getLatitude() + ", " + point.getLongitude());
        return marker;
    }

    /**
     * Converts a View to a Bitmap.
     */
    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Retrieves the current user's username using FirebaseAuth.
     */
    private String getCurrentUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.trim().isEmpty()) {
                return displayName;
            }
            String email = user.getEmail();
            if (email != null && email.contains("@")) {
                return email.substring(0, email.indexOf('@'));
            }
        }
        return "unknown";
    }

    private double distanceInKm(GeoPoint point1, GeoPoint point2) {
        return point1.distanceToAsDouble(point2) / 1000.0;
    }

    /**
     * Returns the icon resource ID for a given emotion name.
     */
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
     * Returns the color for a given emotion using its textColorResId.
     */
    private int getColorForEmotion(String emotionName) {
        List<Emotion> emotions = EmotionProvider.getSampleEmotions();
        for (Emotion e : emotions) {
            if (e.getName().equalsIgnoreCase(emotionName)) {
                return ContextCompat.getColor(getContext(), e.getTextColorResId());
            }
        }
        // Fallback color if not found
        return ContextCompat.getColor(getContext(), android.R.color.holo_red_dark);
    }

    /**
     * Displays a dialog with details for a post.
     */
    private void showPostDetails(EmotionPost post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(post.getEmotion());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_post_details, null);
        android.widget.TextView tvDetails = dialogView.findViewById(R.id.tvDetails);
        android.widget.ImageView ivPostImage = dialogView.findViewById(R.id.ivPostImage);
        StringBuilder details = new StringBuilder();
        details.append("Posted by: ").append(post.getUsername()).append("\n");
        details.append("Explanation: ").append(post.getExplanation());
        tvDetails.setText(details.toString());
        if (post.getImageUri() != null && !post.getImageUri().trim().isEmpty()) {
            ivPostImage.setVisibility(View.VISIBLE);
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

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
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
