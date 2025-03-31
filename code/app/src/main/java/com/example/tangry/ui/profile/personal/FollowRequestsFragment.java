package com.example.tangry.ui.profile.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.controllers.FollowController;
import com.example.tangry.controllers.FollowController.FollowRequest;
import com.example.tangry.ui.profile.personal.FollowRequestsViewModel;

import java.util.ArrayList;
import java.util.List;

public class FollowRequestsFragment extends Fragment {

    private FollowRequestsViewModel viewModel;
    private RecyclerView sentRecyclerView;
    private RecyclerView receivedRecyclerView;

    private SentAdapter sentAdapter;
    private ReceivedAdapter receivedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(FollowRequestsViewModel.class);
        sentRecyclerView = view.findViewById(R.id.recycler_sent);
        receivedRecyclerView = view.findViewById(R.id.recycler_received);

        sentAdapter = new SentAdapter(new ArrayList<>());
        receivedAdapter = new ReceivedAdapter(new ArrayList<>());

        sentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sentRecyclerView.setAdapter(sentAdapter);

        receivedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        receivedRecyclerView.setAdapter(receivedAdapter);

        viewModel.getSentRequests().observe(getViewLifecycleOwner(), requests ->
                sentAdapter.setData(requests));
        viewModel.getReceivedRequests().observe(getViewLifecycleOwner(), requests ->
                receivedAdapter.setData(requests));
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SentAdapter extends RecyclerView.Adapter<SentAdapter.SentViewHolder> {
        private List<FollowRequest> data;

        SentAdapter(List<FollowRequest> data) {
            this.data = data;
        }

        void setData(List<FollowRequest> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sent_request, parent, false);
            return new SentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SentViewHolder holder, int position) {
            FollowRequest req = data.get(position);
            holder.bind(req);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class SentViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView textTo;
            private final android.widget.TextView textStatus;

            SentViewHolder(@NonNull View itemView) {
                super(itemView);
                textTo = itemView.findViewById(R.id.text_to);
                textStatus = itemView.findViewById(R.id.text_status);
            }

            void bind(FollowRequest req) {
                textTo.setText("To: " + req.to);
                textStatus.setText("Status: " + (req.accepted ? "Accepted" : "Pending"));
            }
        }
    }

    private class ReceivedAdapter extends RecyclerView.Adapter<ReceivedAdapter.ReceivedViewHolder> {
        private List<FollowRequest> data;

        ReceivedAdapter(List<FollowRequest> data) {
            this.data = data;
        }

        void setData(List<FollowRequest> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReceivedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_request, parent, false);
            return new ReceivedViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ReceivedViewHolder holder, int position) {
            FollowRequest req = data.get(position);
            holder.bind(req);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ReceivedViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView textFrom;
            private final android.widget.TextView textStatus;
            private final android.widget.Button btnAccept, btnDeny;

            ReceivedViewHolder(@NonNull View itemView) {
                super(itemView);
                textFrom = itemView.findViewById(R.id.text_from);
                textStatus = itemView.findViewById(R.id.text_status);
                btnAccept = itemView.findViewById(R.id.btn_accept);
                btnDeny = itemView.findViewById(R.id.btn_deny);
            }

            void bind(FollowRequest req) {
                textFrom.setText("From: " + req.from);
                textStatus.setText("Status: " + (req.accepted ? "Accepted" : "Pending"));
                if (req.accepted) {
                    btnAccept.setVisibility(View.GONE);
                    btnDeny.setVisibility(View.GONE);
                } else {
                    btnAccept.setVisibility(View.VISIBLE);
                    btnDeny.setVisibility(View.VISIBLE);
                    btnAccept.setOnClickListener(v -> viewModel.acceptRequest(req));
                    btnDeny.setOnClickListener(v -> viewModel.denyRequest(req));
                }
            }
        }
    }
}