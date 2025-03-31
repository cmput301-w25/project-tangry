/**
 * HomePagerAdapter.java
 *
 * This adapter is responsible for managing the fragments displayed in the HomeFragment's ViewPager2.
 * It returns the appropriate fragment for each tab position: one for the user's own mood ("Your Mood")
 * and one for friends' moods ("Friend Moods"). The adapter extends FragmentStateAdapter to support
 * dynamic fragment lifecycles and efficient memory usage.
 *
 * Outstanding Issues:
 * - We considering refactoring if additional tabs are required in the future.
 * - The "Your Mood" fragment logic may need to be separated into a dedicated fragment (e.g., YourMoodFragment).
 */

package com.example.tangry.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs a new HomePagerAdapter.
     *
     * @param fragment the host fragment within which the ViewPager2 resides
     */
    public HomePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Creates and returns the fragment to display for a given tab position.
     *
     * @param position the position of the tab
     * @return the Fragment corresponding to the tab position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment for each tab.
        if (position == 0) {
            // Option 1: If your current HomeFragment code (Firestore listener) should be "Your Mood",
            // move that code to a new fragment (YourMoodFragment) and return it.
            return new YourMoodFragment();
        } else {
            // Return the fragment for friend moods.
            return new FriendMoodsFragment();
        }
    }

    /**
     * Returns the total number of tabs.
     *
     * @return the number of tabs (2)
     */
    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}
