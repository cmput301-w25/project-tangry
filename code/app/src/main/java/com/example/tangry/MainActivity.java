/**
 * MainActivity.java
 *
 * This is the main activity of the application which sets up the navigation components,
 * including the bottom navigation view, the toolbar, and the floating action button (FAB).
 * It initializes the Navigation Controller and configures the AppBar with top-level destinations.
 * The activity also controls the visibility of UI elements based on the user's login state.
 *
 * Outstanding Issues:
 * - Additional error handling for navigation and UI updates could be implemented.
 * - Consider adding animations or transitions when showing/hiding UI elements.
 */

package com.example.tangry;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tangry.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    /**
     * Called when the activity is first created. This method initializes view binding,
     * sets up the toolbar, bottom navigation view, Navigation Controller, and floating action button.
     * It also initially hides the navigation UI until the user logs in.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar as the app bar for the activity.
        setSupportActionBar(findViewById(R.id.toolbar_primary));

        // Configure AppBar with top-level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_add_user, R.id.navigation_friends)
                .build();

        // Initialize Navigation Controller and set up action bar and bottom navigation view.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Find and set up the Floating Action Button (FAB) to navigate to the emotions fragment.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.emotionsFragment));

        // Initially hide UI elements until user is logged in.
        binding.navView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    /**
     * Called upon a successful login. This method reveals the bottom navigation view and FAB,
     * and navigates the user to the home screen.
     */
    public void onLoginSuccess() {
        // Reveal UI after successful login.
        binding.navView.setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        navController.navigate(R.id.navigation_home);
    }

    /**
     * Handles the navigation "Up" action in the app bar.
     *
     * @return true if navigation was handled successfully; otherwise, it delegates to the superclass.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
