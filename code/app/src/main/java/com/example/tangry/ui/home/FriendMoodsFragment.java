package com.example.tangry.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.tangry.R;

public class FriendMoodsFragment extends Fragment {

    public FriendMoodsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate a layout for friend moods (create fragment_friend_moods.xml)
        return inflater.inflate(R.layout.fragment_friend_moods, container, false);
    }
}
