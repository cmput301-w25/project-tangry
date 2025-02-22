package com.example.tangry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class EmotionsFragment extends Fragment implements ItemAdapter.ItemClickListener {
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emotions, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        ItemAdapter adapter = new ItemAdapter(getSampleEmotions(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private List<String> getSampleEmotions() {
        return Arrays.asList("Happy", "Sad", "Excited", "Calm", "Angry");
    }

    @Override
    public void onItemClick(String itemText) {
        // Using Safe Args
        EmotionsFragmentDirections.ActionEmotionsFragmentToDetailFragment action =
                EmotionsFragmentDirections.actionEmotionsFragmentToDetailFragment(itemText);
        navController.navigate(action);
    }
}