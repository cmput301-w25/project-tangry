/**
 * User.java
 * 
 * This file contains the model class for representing user profiles in the Tangry application.
 * The User model stores identification information and tracks gamification elements such as
 * karma points and achievement badges.
 * 
 * Key features:
 * - Stores basic user identification (username)
 * - Tracks karma points for user reputation and engagement
 * - Maintains counts of different badge types (gold, silver, daily)
 * - Supports the leaderboard and achievement system
 * - Provides a simple data structure for user representation
 * - Used in user profile displays and comparative rankings
 */
package com.example.tangry.models;

public class User {
    private String username;
    private int karma;
    private int goldBadges;
    private int silverBadges;
    private int dailyBadges;

    public User(String username, int karma, int goldBadges, int silverBadges, int dailyBadges) {
        this.username = username;
        this.karma = karma;
        this.goldBadges = goldBadges;
        this.silverBadges = silverBadges;
        this.dailyBadges = dailyBadges;
    }

    public String getUsername() {
        return username;
    }

    public int getKarma() {
        return karma;
    }

    public int getGoldBadges() {
        return goldBadges;
    }

    public int getSilverBadges() {
        return silverBadges;
    }

    public int getDailyBadges() {
        return dailyBadges;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public void setGoldBadges(int goldBadges) {
        this.goldBadges = goldBadges;
    }

    public void setSilverBadges(int silverBadges) {
        this.silverBadges = silverBadges;
    }

    public void setDailyBadges(int dailyBadges) {
        this.dailyBadges = dailyBadges;
    }
}
