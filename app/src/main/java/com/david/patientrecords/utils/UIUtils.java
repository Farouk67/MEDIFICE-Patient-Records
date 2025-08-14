package com.david.patientrecords.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.david.patientrecords.R;

public class UIUtils {

    private static final String TAG = "UIUtils";

    /**
     * Animate a number change in a TextView (for dashboard statistics)
     */
    public static void animateNumberChange(TextView textView, int startValue, int endValue) {
        animateNumberChange(textView, startValue, endValue, 1000);
    }

    public static void animateNumberChange(TextView textView, int startValue, int endValue, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(animatedValue));
        });

        animator.start();
    }

    /**
     * Smooth fade in animation
     */
    public static void fadeIn(View view) {
        fadeIn(view, 300);
    }

    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(null);
    }

    /**
     * Smooth fade out animation
     */
    public static void fadeOut(View view) {
        fadeOut(view, 300);
    }

    public static void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Scale and fade in animation for cards
     */
    public static void scaleIn(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .setListener(null);
    }

    /**
     * Slide up animation for bottom sheets/cards
     */
    public static void slideUp(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        view.animate()
                .translationY(0)
                .setDuration(350)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(null);
    }

    /**
     * Slide down and hide
     */
    public static void slideDown(View view) {
        view.animate()
                .translationY(view.getHeight())
                .setDuration(350)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        view.setTranslationY(0); // Reset for next time
                    }
                });
    }

    /**
     * Bounce animation for buttons
     */
    public static void bounceView(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .setListener(null);
                    }
                });
    }

    /**
     * Pulse animation for notifications
     */
    public static void pulseView(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setRepeatCount(2);
        scaleY.setRepeatCount(2);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Show loading state with animation
     */
    public static void showLoadingState(View loadingView, View contentView) {
        if (contentView.getVisibility() == View.VISIBLE) {
            fadeOut(contentView, 200);
        }

        loadingView.postDelayed(() -> fadeIn(loadingView, 200), 150);
    }

    /**
     * Show content and hide loading
     */
    public static void showContentState(View loadingView, View contentView) {
        if (loadingView.getVisibility() == View.VISIBLE) {
            fadeOut(loadingView, 200);
        }

        contentView.postDelayed(() -> {
            scaleIn(contentView);
        }, 150);
    }

    /**
     * Hide keyboard
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Show keyboard
     */
    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Set status bar color for different themes
     */
    public static void setStatusBarColor(android.app.Activity activity, int colorResId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, colorResId));
        }
    }

    /**
     * Animate view visibility changes
     */
    public static void setViewVisibility(View view, int visibility, boolean animate) {
        if (!animate) {
            view.setVisibility(visibility);
            return;
        }

        switch (visibility) {
            case View.VISIBLE:
                if (view.getVisibility() != View.VISIBLE) {
                    fadeIn(view);
                }
                break;
            case View.GONE:
            case View.INVISIBLE:
                if (view.getVisibility() == View.VISIBLE) {
                    fadeOut(view);
                }
                break;
        }
    }

    /**
     * Create a staggered animation for list items
     */
    public static void animateListItem(View view, int position) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up_fade_in);
        animation.setStartOffset(position * 50); // Stagger by 50ms per item
        view.startAnimation(animation);
    }

    /**
     * Professional error shake animation
     */
    public static void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f);
        shake.setDuration(500);
        shake.start();
    }

    /**
     * Professional success checkmark animation
     */
    public static void successPulse(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .setListener(null);
                    }
                });
    }

    /**
     * Smooth color transition for backgrounds
     */
    public static void animateBackgroundColor(View view, int fromColor, int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(fromColor, toColor);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator ->
                view.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    /**
     * Professional loading dots animation for text
     */
    public static void animateLoadingDots(TextView textView, String baseText) {
        final String[] loadingStates = {
                baseText,
                baseText + ".",
                baseText + "..",
                baseText + "..."
        };

        ValueAnimator animator = ValueAnimator.ofInt(0, loadingStates.length - 1);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            int index = (int) animation.getAnimatedValue();
            textView.setText(loadingStates[index]);
        });
        animator.start();

        // Store animator in tag for later cancellation
        textView.setTag(animator);
    }

    /**
     * Stop loading dots animation
     */
    public static void stopLoadingDots(TextView textView) {
        Object tag = textView.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
            textView.setTag(null);
        }
    }
}