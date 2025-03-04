package com.example.tangry;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.tangry.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference emotionFeedRef;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private MaterialToolbar secondaryToolbar; // For dynamic page title and profile icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configure the primary toolbar as the ActionBar so it always shows "Tangry"
        setSupportActionBar(binding.toolbarPrimary);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tangry");
        }

        // Set up bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_add_user, R.id.navigation_friends)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        // Find and configure the secondary toolbar.
        // It should NOT be set as the ActionBar – it’s a separate view that shows the current page name.
        secondaryToolbar = findViewById(R.id.toolbar_secondary);
        if (secondaryToolbar != null) {
            Log.e(TAG, "Secondary toolbar is null – please check your layout file!");
            // Optionally, set a default title (or leave it blank)
            secondaryToolbar.setTitle("");
        }

        // Add a destination changed listener to update the secondary toolbar's title
        // with the current destination's label (e.g., "Home", "Map", etc.)
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (secondaryToolbar != null) {
                CharSequence label = destination.getLabel();
                secondaryToolbar.setTitle(label != null ? label : "");
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        emotionFeedRef = db.collection("emotions");

        // Set up the Floating Action Button to navigate to the emotions fragment
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.emotionsFragment));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
