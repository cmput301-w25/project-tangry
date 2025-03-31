/**
 * LeaderboardAdapter.java
 * 
 * This file contains the adapter implementation for displaying user rankings in the Tangry application's
 * leaderboard feature. It converts User objects into list items showing their achievements and stats.
 * 
 * Key features:
 * - Displays usernames and karma scores for each user
 * - Shows counts for different badge types (gold, silver, daily)
 * - Applies visual styling to indicate badge counts (dimming icons when count is zero)
 * - Supports updating the list of displayed users
 * - Follows a consistent display format for comparing user achievements
 */
package com.example.tangry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        User user = users.get(position);

        // Show username and karma
        holder.usernameText.setText(user.getUsername());
        holder.karmaText.setText("🔥 " + user.getKarma());

        int goldCount = user.getGoldBadges();
        holder.goldBadgeCount.setText("x" + goldCount);

        if (goldCount == 0) {
            holder.goldBadgeIcon.setAlpha(0.3f); // dim
        } else {
            holder.goldBadgeIcon.setAlpha(1.0f); // normal
        }

        // Show silver badges
        int silverCount = user.getSilverBadges();
        holder.silverBadgeCount.setText("x" + silverCount);

        // Dim if 0:
        if (silverCount == 0) {
            holder.silverBadgeIcon.setAlpha(0.3f);
        } else {
            holder.silverBadgeIcon.setAlpha(1.0f);
        }

        // Show daily badges
        int dailyCount = user.getDailyBadges();
        holder.dailyBadgeCount.setText("x" + dailyCount);

        // Dim if 0:
        if (dailyCount == 0) {
            holder.dailyBadgeIcon.setAlpha(0.3f);
        } else {
            holder.dailyBadgeIcon.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, karmaText;
        ImageView goldBadgeIcon, silverBadgeIcon, dailyBadgeIcon;
        TextView goldBadgeCount, silverBadgeCount, dailyBadgeCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameText = itemView.findViewById(R.id.username_text);
            karmaText = itemView.findViewById(R.id.karma_text);

            goldBadgeIcon = itemView.findViewById(R.id.gold_badge_icon);
            goldBadgeCount = itemView.findViewById(R.id.gold_badge_count);

            silverBadgeIcon = itemView.findViewById(R.id.silver_badge_icon);
            silverBadgeCount = itemView.findViewById(R.id.silver_badge_count);

            dailyBadgeIcon = itemView.findViewById(R.id.daily_badge_icon);
            dailyBadgeCount = itemView.findViewById(R.id.daily_badge_count);
        }
    }
}
