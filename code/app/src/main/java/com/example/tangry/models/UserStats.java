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
