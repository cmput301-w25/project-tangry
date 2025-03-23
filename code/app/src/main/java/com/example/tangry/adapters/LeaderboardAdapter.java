package com.example.tangry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.R;
import com.example.tangry.models.User;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<User> users;

    public LeaderboardAdapter(List<User> users) {
        this.users = users;
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText(user.getUsername());
        holder.karmaText.setText(String.valueOf(user.getKarma()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, karmaText;
        public ViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            karmaText = itemView.findViewById(R.id.karma_text);
        }
    }
}