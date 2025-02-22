package com.example.tangry.ui.add_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tangry.databinding.FragmentAddUserBinding;

public class AddUserFragment extends Fragment {
    public AddUserFragment() {
        // Required empty public constructor
    }
    private FragmentAddUserBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AddUserViewModel addUserViewModel =
                new ViewModelProvider(this).get(AddUserViewModel.class);

        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.addUser;
        addUserViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}