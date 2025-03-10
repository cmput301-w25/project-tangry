package com.example.tangry.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting timestamps into human-readable "time ago" format.
 * <p>
 * Example Outputs:
 * - "5 minutes ago"
 * - "2 hours ago"
 * - "3 days ago"
 * - "Jan 05, 2024" (if older than 5 days)
 */
public class TimeUtils {

    /**
     * Converts a Date object into a "time ago" string.
     *
     * @param date The date to format.
     * @return A human-readable string representing the time difference.
     */
    public static String getTimeAgo(Date date) {
        long timeDiff = System.currentTimeMillis() - date.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 5) {
            return days + " days ago";
        } else {
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }
    }
}
