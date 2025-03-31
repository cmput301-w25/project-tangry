/**
 * EmotionsFragment.java
 *
 * This fragment displays a list of emotion emojis retrieved from the EmotionProvider.
 * It sets up a RecyclerView with an EmotionEmojisAdapter to display sample emotions.
 * When an emotion is selected, the fragment either passes the selection for editing an existing post
 * or navigates to the detail screen for creating a new post, based on the provided arguments.
 *
 * Additionally, this fragment adds a menu provider to display a filter icon,
 * which when selected shows a BottomSheetDialog with mood filter options.
 *
 * Outstanding Issues:
 * - We considering adding more robust error handling for navigation and argument retrieval.
 * - UI refinements for the BottomSheetDialog and RecyclerView animations could enhance user experience.
 */

package com.example.tangry.ui.emotionEmojis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.models.EmotionProvider;
import com.example.tangry.ui.emotionEmojis.EmotionsFragmentDirections;
import com.example.tangry.R;
import com.example.tangry.adapters.EmotionEmojisAdapter;
import com.example.tangry.models.Emotion;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;

public class EmotionsFragment extends Fragment implements EmotionEmojisAdapter.ItemClickListener {

    private NavController navController;

    /**
     * Inflates the layout for this fragment, initializes the RecyclerView with sample emotions,
     * and sets up navigation for item clicks.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate views in the fragment
     * @param container          the parent container for the fragment's UI
     * @param savedInstanceState a Bundle containing previous state data, if any
     * @return the root View of the inflated layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emotions, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        // Use EmotionProvider to retrieve the sample emotions.
        List<Emotion> emotions = EmotionProvider.getSampleEmotions();
        EmotionEmojisAdapter adapter = new EmotionEmojisAdapter(emotions, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    /**
     * Handles click events on emotion items.
     * Depending on the fragment's arguments, it either navigates to the edit screen with updated emotion data
     * or navigates to the detail screen for adding a new post.
     *
     * @param emotion the selected Emotion object
     */
    @Override
    public void onItemClick(Emotion emotion) {
        Bundle args = getArguments();
        if (args != null && args.getBoolean("isEditing", false)) {
            // Editing Mode → Pass Selected Emotion to EditEmotionFragment
            Bundle bundle = new Bundle();
            bundle.putString("postJson", args.getString("postJson")); // Keep post data
            bundle.putString("postId", args.getString("postId")); // Keep post ID
            bundle.putString("selectedEmotion", emotion.getName()); // Pass new emotion

            navController.navigate(R.id.action_emotionsFragment_to_editEmotionFragment, bundle);
        } else {
            // Add Mode → Navigate to Create (Add) Post Screen
            EmotionsFragmentDirections.ActionEmotionsFragmentToDetailFragment action =
                    EmotionsFragmentDirections.actionEmotionsFragmentToDetailFragment(emotion.getName());
            navController.navigate(action);
        }
    }

    /**
     * Called immediately after onCreateView() and sets up the MenuProvider to handle menu creation and selection.
     * The menu includes a filter icon that displays a BottomSheetDialog when selected.
     *
     * @param view               the View returned by onCreateView()
     * @param savedInstanceState a Bundle containing previous state data, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add MenuProvider to handle menu creation and item selection
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_profile) {
                    Navigation.findNavController(getView())
                            .navigate(R.id.action_global_personal_profile);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Displays a BottomSheetDialog with mood filter options.
     */
    private void showMoodsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
