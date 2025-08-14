package com.david.patientrecords.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

public class DateUtils {

    private static final String TAG = "DateUtils";

    // Date format constants
    public static final String DATE_FORMAT_DATABASE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATETIME_FORMAT_DATABASE = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy HH:mm";

    /**
     * Format date from year, month, day integers
     */
    public static String formatDate(int year, int month, int dayOfMonth) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, dayOfMonth);
    }

    /**
     * Format Date object to database format
     */
    public static String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get current date in database format
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current timestamp
     */
    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format date string for display (from database format to display format)
     */
    public static String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "No date";
        }

        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
            Date date = dbFormat.parse(dateString);
            return date != null ? displayFormat.format(date) : dateString;
        } catch (ParseException e) {
            // If parsing fails, return original string
            return dateString;
        }
    }

    /**
     * Parse date string to Calendar object
     */
    public static Calendar parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            }
        } catch (ParseException e) {
            // Return null if parsing fails
        }
        return null;
    }

    /**
     * Format Date object to database format (YYYY-MM-DD)
     */
    public static String formatDateForDatabase(Date date) {
        if (date == null) return getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get number of days between two dates
     */
    public static int getDaysBetween(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start == null || end == null) return 0;

            long diffInMillies = end.getTime() - start.getTime();
            return (int) (diffInMillies / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            Log.e("DateUtils", "Error calculating days between dates", e);
            return 0;
        }
    }

    /**
     * Get current date as Date object
     */
    public static Date getCurrentDateAsDate() {
        return new Date();
    }

    /**
     * Get relative time string (e.g., "2 days ago", "Last week")
     */
    public static String getRelativeTimeString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Unknown";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return dateString;

            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            if (diffInDays == 0) {
                return "Today";
            } else if (diffInDays == 1) {
                return "Yesterday";
            } else if (diffInDays < 7) {
                return diffInDays + " days ago";
            } else if (diffInDays < 30) {
                long weeks = diffInDays / 7;
                return weeks == 1 ? "Last week" : weeks + " weeks ago";
            } else if (diffInDays < 365) {
                long months = diffInDays / 30;
                return months == 1 ? "Last month" : months + " months ago";
            } else {
                long years = diffInDays / 365;
                return years == 1 ? "Last year" : years + " years ago";
            }
        } catch (ParseException e) {
            return dateString;
        }
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        String today = getCurrentDate();
        return today.equals(dateString);
    }

    /**
     * Check if date is within last N days
     */
    public static boolean isWithinLastDays(String dateString, int days) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return false;

            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            return diffInDays <= days;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Add days to a date string
     */
    public static String addDaysToDate(String dateString, int days) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return dateString;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, days);

            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            return dateString;
        }
    }

    /**
     * Get date string for N days from now
     */
    public static String getDateAfterDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * Get formatted time from timestamp
     */
    public static String getTimeFromTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATETIME_FORMAT_DATABASE, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return date != null ? outputFormat.format(date) : "";
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Calculate age from birth date
     */
    public static int calculateAge(String birthDateString) {
        if (birthDateString == null || birthDateString.isEmpty()) {
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date birthDate = sdf.parse(birthDateString);
            if (birthDate == null) return 0;

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar now = Calendar.getInstance();

            int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            // Check if birthday has occurred this year
            if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return Math.max(0, age);
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Get day of week from date string
     */
    public static String getDayOfWeek(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return "";

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Check if date string is valid
     */
    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            sdf.setLenient(false); // Strict parsing
            Date date = sdf.parse(dateString);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }
    /**
     * Get date string for N days ago from today
     */
    public static String getDateMinusDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days); // Subtract days
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * Get month name from date string
     */
    public static String getMonthName(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return "";

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            return monthFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Compare two date strings
     * Returns: -1 if date1 < date2, 0 if equal, 1 if date1 > date2
     */
    public static int compareDates(String date1, String date2) {
        if (date1 == null && date2 == null) return 0;
        if (date1 == null) return -1;
        if (date2 == null) return 1;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATABASE, Locale.getDefault());
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);

            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return -1;
            if (d2 == null) return 1;

            return d1.compareTo(d2);
        } catch (ParseException e) {
            return 0;
        }
    }
}