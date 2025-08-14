package com.david.patientrecords.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    /**
     * Show success toast message (green background)
     */
    public static void showSuccess(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show error toast message (red background)
     */
    public static void showError(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Show info toast message (blue background)
     */
    public static void showInfo(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show warning toast message (orange background)
     */
    public static void showWarning(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show custom duration toast
     */
    public static void showCustom(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
