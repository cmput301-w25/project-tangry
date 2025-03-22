package com.example.tangry;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tangry.databinding.ActivityMainBinding;

/**
 * Main activity that sets up the app's navigation and UI elements.
 * This activity initializes the binding, toolbars, navigation controller,
 * AppBar configuration, and floating action button.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    /**
     * Called when the activity is starting. This sets up the UI elements, including
     * primary and secondary toolbars, navigation controller, bottom navigation view,
     * and floating action button.
     *
     * @param savedInstanceState if the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the primary toolbar.
        MaterialToolbar primaryToolbar = findViewById(R.id.toolbar_primary);
        setSupportActionBar(primaryToolbar);

        // Configure AppBar with top-level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_map,
                R.id.navigation_add_user,
                R.id.navigation_friends)
                .build();

        // Initialize Navigation Controller.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // To whoever is removing the secondary toolbar
        // DELETE FROM HERE

        // Set up the secondary toolbar for the page title and profile icon
        MaterialToolbar secondaryToolbar = findViewById(R.id.toolbar_secondary);
        // Inflate the menu for the secondary toolbar.
        secondaryToolbar.inflateMenu(R.menu.menu_profile);
        // Handle profile icon clicks.
        secondaryToolbar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            /**
             * Called when a menu item in the secondary toolbar is clicked.
             *
             * @param item the menu item that was clicked
             * @return true if the event was handled, false otherwise
             */
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_profile) {
                    Log.d("MainActivity", "Profile button pressed (secondary toolbar)");
                    navController.navigate(R.id.action_global_personal_profile);
                    return true;
                }
                return false;
            }
        });

        // TO HERE
        // to remove the profile button appearing on the secondary toolbar
        // Make sure that the profile icon on the primary toolbar navigates to PersonalProfileFragment
        // With love,
        // Jacob

        // Setup Floating Action Button.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.emotionsFragment));

        // Initially hide UI elements until user is logged in.
        binding.navView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    /**
     * Handles navigation when the up button is pressed.
     *
     * @return true if navigation was successful or the super method returns true, false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Called when the user has successfully logged in.
     * This method makes the bottom navigation view and floating action button visible
     * and navigates to the home destination.
     */
    public void onLoginSuccess() {
        // Reveal UI after successful login.
        binding.navView.setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        navController.navigate(R.id.navigation_home);
    }
}