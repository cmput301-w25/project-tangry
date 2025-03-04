package com.example.tangry.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment for each tab
        if (position == 0) {
            // Option 1: If your current HomeFragment code (Firestore listener) should be "Your Mood",
            // move that code to a new fragment (YourMoodFragment) and return it.
            return new YourMoodFragment();
        } else {
            // Return the fragment for friend moods
            return new FriendMoodsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}
