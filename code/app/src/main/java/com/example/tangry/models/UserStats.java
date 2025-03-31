/**
 * UserStats.java
 * 
 * This file contains the model class for representing user statistics in the Tangry application.
 * The UserStats model provides a dedicated structure for tracking numerical achievements and
 * engagement metrics for users across the platform.
 * 
 * Key features:
 * - Tracks karma points as a measure of user contribution and activity
 * - Maintains counts of achievement badges (gold, silver, daily)
 * - Supports the achievement and rewards system
 * - Provides a focused view of user statistics for profiles and leaderboards
 * - Used for calculating user rankings and displaying achievement progress
 */
package com.example.tangry.models;

public class UserStats {
    private long karma;
    private long goldBadges;
    private long silverBadges;
    private int dailyBadgeCount;

    public UserStats(long karma, long goldBadges, long silverBadges, int dailyBadgeCount) {
        this.karma = karma;
        this.goldBadges = goldBadges;
        this.silverBadges = silverBadges;
        this.dailyBadgeCount = dailyBadgeCount;
    }

    public long getKarma() {
        return karma;
    }

    public long getGoldBadges() {
        return goldBadges;
    }

    public long getSilverBadges() {
        return silverBadges;
    }

    public int getDailyBadgeCount() {
        return dailyBadgeCount;
    }
}
