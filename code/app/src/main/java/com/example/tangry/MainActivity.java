package com.example.tangry;

import android.os.Bundle;
import android.view.Menu;
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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the primary toolbar.
        MaterialToolbar primaryToolbar = findViewById(R.id.toolbar_primary);
        setSupportActionBar(primaryToolbar);
        // Inflate the toolbar menu including the profile button.
        primaryToolbar.inflateMenu(R.menu.menu_profile);

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

        // Set up Floating Action Button.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.emotionsFragment));

        // Initially hide UI elements until user is logged in.
        binding.navView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle profile button click.
        if (item.getItemId() == R.id.action_profile) {
            navController.navigate(R.id.personalProfileFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        binding.navView.setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        navController.navigate(R.id.navigation_home);
    }
}