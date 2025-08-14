package com.david.patientrecords.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    // Profile colors for generating avatars
    private static final int[] PROFILE_COLORS = {
            0xFF4285F4, // Blue
            0xFF34A853, // Green
            0xFFEA4335, // Red
            0xFFFBBC05, // Yellow
            0xFF9C27B0, // Purple
            0xFFFF5722, // Deep Orange
            0xFF00BCD4, // Cyan
            0xFF8BC34A, // Light Green
            0xFFE91E63, // Pink
            0xFF795548  // Brown
    };

    /**
     * Load patient image with fallback to initials avatar - MAIN METHOD FOR ADAPTER
     */
    public static void loadPatientImageWithFallback(Context context, String imagePath,
                                                    String patientName, CircleImageView imageView) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from path: " + imagePath, e);
                // Fall through to initials avatar
            }
        }

        // Create initials avatar as fallback
        setInitialsAvatar(patientName, imageView);
    }

    /**
     * Set patient image with compatibility for both old and new image fields
     */
    public static void setPatientImage(Context context, ImageView imageView,
                                       String profileImage, String patientName) {
        loadPatientImageWithFallback(context, profileImage, patientName, (CircleImageView) imageView);
    }

    /**
     * Set initials avatar for CircleImageView
     */
    public static void setInitialsAvatar(String patientName, CircleImageView imageView) {
        if (patientName == null || patientName.trim().isEmpty()) {
            imageView.setImageResource(com.david.patientrecords.R.drawable.ic_person_placeholder);
            return;
        }

        // Generate initials
        String initials = generateInitials(patientName);

        // Generate color based on name
        int color = generateColorFromName(patientName);

        // Create bitmap with initials (use imageView size or default)
        int size = Math.max(imageView.getWidth() > 0 ? imageView.getWidth() : 200, 200);
        Bitmap avatar = createInitialsAvatar(initials, size, color);
        imageView.setImageBitmap(avatar);
    }

    /**
     * Create a circular avatar with initials
     */
    public static Bitmap createInitialsAvatar(String initials, int size, int backgroundColor) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw background circle
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint);

        // Draw initials text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.4f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Calculate text position
        Rect textBounds = new Rect();
        textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
        float textX = size / 2f;
        float textY = size / 2f + textBounds.height() / 2f;

        canvas.drawText(initials, textX, textY, textPaint);

        return bitmap;
    }

    /**
     * Create a circular avatar with initials using patient name
     */
    public static Bitmap createPatientAvatar(String patientName, int size) {
        String initials = getInitials(patientName);
        int color = getColorForName(patientName);
        return createInitialsAvatar(initials, size, color);
    }

    /**
     * Get initials from full name
     */
    public static String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] names = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String name : names) {
            if (!name.isEmpty() && initials.length() < 2) {
                initials.append(name.charAt(0));
            }
        }

        return initials.toString().toUpperCase();
    }

    /**
     * Get consistent color for a name (same name always gets same color)
     */
    public static int getColorForName(String name) {
        if (name == null || name.isEmpty()) {
            return PROFILE_COLORS[0];
        }

        int hash = name.hashCode();
        int index = Math.abs(hash) % PROFILE_COLORS.length;
        return PROFILE_COLORS[index];
    }

    /**
     * Generate initials from patient name
     */
    private static String generateInitials(String name) {
        return getInitials(name);
    }

    /**
     * Generate color from patient name
     */
    private static int generateColorFromName(String name) {
        return getColorForName(name);
    }

    /**
     * Load and resize bitmap from file path
     */
    public static Bitmap loadBitmapFromPath(String imagePath, int targetWidth, int targetHeight) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(imagePath, options);

        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from path: " + imagePath, e);
            return null;
        }
    }

    /**
     * Load bitmap from URI
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri, int targetWidth, int targetHeight) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            // Decode bitmap with inSampleSize set
            inputStream = context.getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from URI: " + uri.toString(), e);
            return null;
        }
    }

    /**
     * Calculate sample size for bitmap loading
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Compress bitmap to JPEG format with quality setting
     */
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    /**
     * Save bitmap to internal storage
     */
    public static String saveBitmapToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        try {
            File directory = new File(context.getFilesDir(), Constants.IMAGES_FOLDER);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, fos);
            fos.flush();
            fos.close();

            Log.d(TAG, "Bitmap saved to: " + file.getAbsolutePath());
            return file.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to internal storage", e);
            return null;
        }
    }

    /**
     * Resize bitmap to specific dimensions
     */
    public static Bitmap resizeBitmap(Bitmap originalBitmap, int targetWidth, int targetHeight) {
        if (originalBitmap == null) {
            return null;
        }

        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
    }

    /**
     * Resize bitmap maintaining aspect ratio
     */
    public static Bitmap resizeBitmapMaintainAspectRatio(Bitmap originalBitmap, int maxSize) {
        if (originalBitmap == null) {
            return null;
        }

        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        float ratio = Math.min(
                (float) maxSize / originalWidth,
                (float) maxSize / originalHeight
        );

        int newWidth = Math.round(originalWidth * ratio);
        int newHeight = Math.round(originalHeight * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    /**
     * Rotate bitmap based on EXIF orientation
     */
    public static Bitmap rotateBitmapFromExif(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap; // No rotation needed
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
            return bitmap;
        }
    }

    /**
     * Create a circular bitmap from any bitmap
     */
    public static Bitmap createCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Set transfer mode to only draw on existing pixels
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));

        // Draw the bitmap onto the circular mask
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect destRect = new Rect(0, 0, size, size);
        canvas.drawBitmap(bitmap, srcRect, destRect, paint);

        return output;
    }

    /**
     * Delete image file from storage
     */
    public static boolean deleteImageFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        try {
            File file = new File(imagePath);
            boolean deleted = file.delete();
            Log.d(TAG, "Image file deleted: " + imagePath + ", success: " + deleted);
            return deleted;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image file: " + imagePath, e);
            return false;
        }
    }

    /**
     * Check if image file exists
     */
    public static boolean imageFileExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        File file = new File(imagePath);
        return file.exists() && file.isFile();
    }

    /**
     * Get image file size in bytes
     */
    public static long getImageFileSize(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return 0;
        }

        File file = new File(imagePath);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Load patient image into any ImageView with automatic fallback
     */
    public static void loadPatientImageWithFallback(Context context, String imagePath,
                                                    String patientName, ImageView imageView) {
        // Check if it's a CircleImageView
        if (imageView instanceof CircleImageView) {
            loadPatientImageWithFallback(context, imagePath, patientName, (CircleImageView) imageView);
            return;
        }

        // Handle regular ImageView
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from path: " + imagePath, e);
            }
        }

        // Set initials avatar for regular ImageView
        setInitialsAvatarForImageView(patientName, imageView);
    }

    /**
     * Set initials avatar for regular ImageView
     */
    private static void setInitialsAvatarForImageView(String patientName, ImageView imageView) {
        if (patientName == null || patientName.trim().isEmpty()) {
            imageView.setImageResource(com.david.patientrecords.R.drawable.ic_person_placeholder);
            return;
        }

        // Get view dimensions or use default
        int size = Math.max(
                imageView.getWidth() > 0 ? imageView.getWidth() :
                        (int) (48 * imageView.getContext().getResources().getDisplayMetrics().density),
                48 * (int) imageView.getContext().getResources().getDisplayMetrics().density);

        Bitmap avatar = createPatientAvatar(patientName != null ? patientName : "?", size);
        imageView.setImageBitmap(avatar);
    }

    /**
     * Clean up old temporary files
     */
    public static void cleanupTempFiles(Context context) {
        try {
            File tempDir = new File(context.getFilesDir(), Constants.TEMP_FOLDER);
            if (tempDir.exists() && tempDir.isDirectory()) {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    long currentTime = System.currentTimeMillis();
                    for (File file : files) {
                        // Delete files older than 24 hours
                        if (currentTime - file.lastModified() > 24 * 60 * 60 * 1000) {
                            file.delete();
                            Log.d(TAG, "Deleted old temp file: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up temp files", e);
        }
    }

    /**
     * Get dominant color from bitmap (for future use)
     */
    public static int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) return Color.GRAY;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        int color = scaledBitmap.getPixel(0, 0);
        scaledBitmap.recycle();
        return color;
    }
}