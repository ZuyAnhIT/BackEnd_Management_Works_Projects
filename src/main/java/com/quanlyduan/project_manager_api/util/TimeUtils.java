package com.quanlyduan.project_manager_api.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {

    public static String getRelativeTimeAgo(LocalDateTime targetTime) {
        if (targetTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(targetTime, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) { // < 7 ngày
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 2592000) { // < 30 ngày
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (seconds < 31536000) { // < 1 năm
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = seconds / 31536000;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
}