/**
 * TimeUtils.java
 *
 * This utility class provides a method to convert a Date object into a human-readable "time ago" format.
 * It calculates the time difference between the current time and the provided date, and returns a string
 * such as "5 minutes ago", "2 hours ago", "3 days ago", or a formatted date (e.g., "Jan 05, 2024") if the
 * date is older than 5 days.
 *
 * Outstanding Issues:
 * - The formatting thresholds can be adjusted based on the application's requirements.
 */

package com.example.tangry.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    /**
     * Converts a Date object into a human-readable "time ago" string.
     *
     * <p>The method computes the time difference between the current system time and the provided date.
     * Depending on the duration, it returns:
     * <ul>
     *   <li>"Just now" if less than a minute has passed</li>
     *   <li>"X minutes ago" if less than 60 minutes have passed</li>
     *   <li>"X hours ago" if less than 24 hours have passed</li>
     *   <li>"X days ago" if less than 5 days have passed</li>
     *   <li>A formatted date string ("MMM dd, yyyy") if 5 or more days have passed</li>
     * </ul>
     *
     * @param date the date to format.
     * @return a human-readable string representing the time difference.
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
