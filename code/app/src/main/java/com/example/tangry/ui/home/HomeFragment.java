/**
 * HomeFragment.java
 *
 * This fragment serves as the home screen of the application. It utilizes a TabLayout and ViewPager2 to display
 * multiple tabs, allowing the user to switch between viewing their own mood and their friends' moods.
 * The HomePagerAdapter manages the fragments for each tab.
 *
 * Outstanding Issues:
 * - Future iterations might include more dynamic tab titles or additional tabs.
 * - Consider enhancing page transition animations and tab customization.
 */

package com.example.tangry.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.tangry.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    /**
     * Default empty constructor required for fragment instantiation.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout containing the TabLayout and ViewPager2.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle containing previously saved state, if any.
     * @return The root View of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the container layout with TabLayout and ViewPager2
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned. Initializes the TabLayout and ViewPager2,
     * sets up the HomePagerAdapter, and attaches the TabLayout to the ViewPager2.
     *
     * @param view               The View returned by onCreateView().
     * @param savedInstanceState A Bundle containing previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tab_layout_home);
        viewPager = view.findViewById(R.id.view_pager_home);

        // Use the HomePagerAdapter to manage pages
        HomePagerAdapter adapter = new HomePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach the TabLayout to the ViewPager2 with appropriate tab titles
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Your Mood");
            } else {
                tab.setText("Friend Moods");
            }
        }).attach();
    }
}
