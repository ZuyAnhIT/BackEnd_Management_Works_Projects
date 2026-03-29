package com.quanlyduan.project_manager_api.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Lop tien ich ho tro tinh toan va hien thi thoi gian tuong doi (Relative Time).
 * Vi du: "Just now", "3 minutes ago", "2 days ago".
 */
public final class TimeUtils {

    // Cac hang so thoi gian tinh bang Giay (Seconds)
    private static final long SECONDS_IN_MINUTE = 60;
    private static final long SECONDS_IN_HOUR = 3600;
    private static final long SECONDS_IN_DAY = 86400;
    private static final long SECONDS_IN_WEEK = 604800;
    private static final long SECONDS_IN_MONTH = 2592000; 
    private static final long SECONDS_IN_YEAR = 31536000;  

    // Cac thong bao mac dinh
    private static final String JUST_NOW = "Just now";
    private static final String AGO = " ago";
    private static final String ERR_INSTANTIATION = "This is a utility class and cannot be instantiated";

    // Ngan chan viec khoi tao doi tuong tu lop tien ich
    private TimeUtils() {
        throw new UnsupportedOperationException(ERR_INSTANTIATION);
    }

    /**
     * Chuyen doi thoi gian tuyet doi sang dang thoi gian tuong doi so voi hien tai.
     * * @param targetTime Moc thoi gian can so sanh.
     * @return Chuoi ky tu hien thi thoi gian tuong doi (vd: "5 hours ago").
     */
    public static String getRelativeTimeAgo(LocalDateTime targetTime) {
        if (targetTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(targetTime, now);
        long seconds = duration.getSeconds();

        if (seconds < SECONDS_IN_MINUTE) {
            return JUST_NOW;
        } 
        
        if (seconds < SECONDS_IN_HOUR) {
            long minutes = seconds / SECONDS_IN_MINUTE;
            return minutes + (minutes == 1 ? " minute" : " minutes") + AGO;
        } 
        
        if (seconds < SECONDS_IN_DAY) {
            long hours = seconds / SECONDS_IN_HOUR;
            return hours + (hours == 1 ? " hour" : " hours") + AGO;
        } 
        
        if (seconds < SECONDS_IN_WEEK) {
            long days = seconds / SECONDS_IN_DAY;
            return days + (days == 1 ? " day" : " days") + AGO;
        } 
        
        if (seconds < SECONDS_IN_MONTH) {
            long weeks = seconds / SECONDS_IN_WEEK;
            return weeks + (weeks == 1 ? " week" : " weeks") + AGO;
        } 
        
        if (seconds < SECONDS_IN_YEAR) {
            long months = seconds / SECONDS_IN_MONTH;
            return months + (months == 1 ? " month" : " months") + AGO;
        }

        long years = seconds / SECONDS_IN_YEAR;
        return years + (years == 1 ? " year" : " years") + AGO;
    }
}