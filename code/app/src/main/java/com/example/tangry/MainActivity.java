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

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onLoginSuccess() {
        // Reveal UI after successful login.
        binding.navView.setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        navController.navigate(R.id.navigation_home);
    }
}