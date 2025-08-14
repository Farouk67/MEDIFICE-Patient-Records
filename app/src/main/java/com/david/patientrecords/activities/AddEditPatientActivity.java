package com.david.patientrecords.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.david.patientrecords.R;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.Patient;
import com.david.patientrecords.utils.Constants;
import com.david.patientrecords.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddEditPatientActivity extends AppCompatActivity {

    private static final String TAG = "AddEditPatientActivity";

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    // UI Components
    private Toolbar toolbar;
    private CircleImageView imagePatientProfile;
    private MaterialButton buttonSelectImage;

    // Form Fields - Personal Information
    private TextInputLayout layoutPatientName, layoutAge, layoutBloodType;
    private TextInputEditText editPatientName, editAge;
    private RadioGroup radioGroupGender;
    private AutoCompleteTextView spinnerBloodType;

    // Form Fields - Contact Information
    private TextInputLayout layoutPhone, layoutAddress;
    private TextInputEditText editPhone, editAddress;

    // Form Fields - Emergency Contact
    private TextInputLayout layoutEmergencyContact, layoutEmergencyPhone;
    private TextInputEditText editEmergencyContact, editEmergencyPhone;

    // Form Fields - Medical Information
    private CheckBox checkHypertension, checkDiabetes, checkAsthma, checkHeartDisease;
    private TextInputLayout layoutOtherConditions, layoutAllergies;
    private TextInputEditText editOtherConditions, editAllergies;

    // Registration Date
    private TextView textRegistrationDate;
    private MaterialButton buttonSelectDate;

    // Action Buttons
    private MaterialButton buttonCancel, buttonSavePatient;

    // Data
    private PatientRepository patientRepository;
    private boolean isEditMode = false;
    private int patientId = -1;
    private Patient currentPatient;
    private String selectedRegistrationDate;

    // Image handling variables
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_patient);

        initViews();
        setupToolbar();
        setupEventListeners();
        setupBloodTypeSpinner();
        initImageHandlers();
        setupImageClickListeners();

        patientRepository = PatientRepository.getInstance(this);

        getIntentData();

        if (isEditMode) {
            loadPatientData();
        } else {
            selectedRegistrationDate = DateUtils.getCurrentDate();
            updateRegistrationDateDisplay();
        }
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Profile Image
        imagePatientProfile = findViewById(R.id.image_patient_profile);
        buttonSelectImage = findViewById(R.id.button_select_image);

        // Form Fields - Personal Information
        layoutPatientName = findViewById(R.id.layout_patient_name);
        editPatientName = findViewById(R.id.edit_patient_name);

        layoutAge = findViewById(R.id.layout_age);
        editAge = findViewById(R.id.edit_age);

        radioGroupGender = findViewById(R.id.radio_group_gender);

        layoutBloodType = findViewById(R.id.layout_blood_type);
        spinnerBloodType = findViewById(R.id.spinner_blood_type);

        // Contact Information
        layoutPhone = findViewById(R.id.layout_phone);
        editPhone = findViewById(R.id.edit_phone);

        layoutAddress = findViewById(R.id.layout_address);
        editAddress = findViewById(R.id.edit_address);

        // Emergency Contact
        layoutEmergencyContact = findViewById(R.id.layout_emergency_contact);
        editEmergencyContact = findViewById(R.id.edit_emergency_contact);

        layoutEmergencyPhone = findViewById(R.id.layout_emergency_phone);
        editEmergencyPhone = findViewById(R.id.edit_emergency_phone);

        // Medical Information
        checkHypertension = findViewById(R.id.check_hypertension);
        checkDiabetes = findViewById(R.id.check_diabetes);
        checkAsthma = findViewById(R.id.check_asthma);
        checkHeartDisease = findViewById(R.id.check_heart_disease);

        layoutOtherConditions = findViewById(R.id.layout_other_conditions);
        editOtherConditions = findViewById(R.id.edit_other_conditions);

        layoutAllergies = findViewById(R.id.layout_allergies);
        editAllergies = findViewById(R.id.edit_allergies);

        // Registration Date
        textRegistrationDate = findViewById(R.id.text_registration_date);
        buttonSelectDate = findViewById(R.id.button_select_date);

        // Action Buttons
        buttonCancel = findViewById(R.id.button_cancel);
        buttonSavePatient = findViewById(R.id.button_save_patient);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Patient" : "Add New Patient");
        }
    }

    private void setupEventListeners() {
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonCancel.setOnClickListener(v -> finish());
        buttonSavePatient.setOnClickListener(v -> savePatient());
    }

    private void setupBloodTypeSpinner() {
        String[] bloodTypes = getResources().getStringArray(R.array.blood_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, bloodTypes);
        spinnerBloodType.setAdapter(adapter);
    }

    private void initImageHandlers() {
        // Initialize camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "Camera result: " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            handleCameraResult();
                        } else if (result.getResultCode() == RESULT_CANCELED) {
                            Log.d(TAG, "Camera capture cancelled by user");
                            Toast.makeText(AddEditPatientActivity.this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Camera capture failed with result code: " + result.getResultCode());
                            Toast.makeText(AddEditPatientActivity.this, "Camera capture failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "Gallery result: " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri selectedImageUri = data.getData();
                                if (selectedImageUri != null) {
                                    Log.d(TAG, "Gallery image selected: " + selectedImageUri.toString());
                                    handleGalleryResult(selectedImageUri);
                                } else {
                                    Log.e(TAG, "Gallery result data is null");
                                    Toast.makeText(AddEditPatientActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Gallery result intent is null");
                                Toast.makeText(AddEditPatientActivity.this, "Failed to get image from gallery", Toast.LENGTH_SHORT).show();
                            }
                        } else if (result.getResultCode() == RESULT_CANCELED) {
                            Log.d(TAG, "Gallery selection cancelled by user");
                            Toast.makeText(AddEditPatientActivity.this, "Gallery cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Gallery selection failed with result code: " + result.getResultCode());
                            Toast.makeText(AddEditPatientActivity.this, "Gallery selection failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        Log.d(TAG, "Permission result: " + result.toString());

                        Boolean cameraPermission = result.get(Manifest.permission.CAMERA);
                        Boolean storagePermission = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);

                        if (cameraPermission != null && cameraPermission &&
                                storagePermission != null && storagePermission) {
                            Log.d(TAG, "All permissions granted, showing image picker");
                            showImagePickerDialog();
                        } else {
                            Log.w(TAG, "Permissions denied - Camera: " + cameraPermission + ", Storage: " + storagePermission);

                            // Check which permissions were denied
                            if (cameraPermission == null || !cameraPermission) {
                                Toast.makeText(AddEditPatientActivity.this,
                                        "Camera permission is required to take photos",
                                        Toast.LENGTH_LONG).show();
                            }
                            if (storagePermission == null || !storagePermission) {
                                Toast.makeText(AddEditPatientActivity.this,
                                        "Storage permission is required to select photos",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );
    }

    private void checkPermissionsAndShowImagePicker() {
        // For Android 13+ (API 33+), we need different permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Use READ_MEDIA_IMAGES instead of READ_EXTERNAL_STORAGE
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };

            if (hasAllPermissions()) {
                showImagePickerDialog();
            } else {
                permissionLauncher.launch(permissions);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6-12 - Use READ_EXTERNAL_STORAGE
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };

            if (hasAllPermissions()) {
                showImagePickerDialog();
            } else {
                permissionLauncher.launch(permissions);
            }
        } else {
            // Older Android versions - permissions granted at install time
            showImagePickerDialog();
        }
    }


    private boolean hasAllPermissions() {
        // Check camera permission
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        // Check storage permission (different for different Android versions)
        boolean hasStoragePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            hasStoragePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            hasStoragePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        Log.d("Permissions", "Camera: " + hasCameraPermission + ", Storage: " + hasStoragePermission);
        return hasCameraPermission && hasStoragePermission;
    }

    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // Check storage permission based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private void setupSelectPhotoButton() {
        buttonSelectImage.setOnClickListener(v -> {
            Log.d("AddEditPatient", "SELECT PHOTO clicked");

            // Force check permissions again
            if (hasAllPermissions()) {
                Log.d("AddEditPatient", "Permissions OK - showing photo options");
                showPhotoSelectionDialog();
            } else {
                Log.d("AddEditPatient", "Permissions missing - requesting");
                requestPermissions();
            }
        });
    }


    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Profile Photo");
        builder.setMessage("Choose how you'd like to add a profile photo:");

        builder.setPositiveButton("Take Photo", (dialog, which) -> openCamera());
        builder.setNegativeButton("Choose from Gallery", (dialog, which) -> openGallery());
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openCamera() {
        try {
            Log.d(TAG, "Opening camera...");

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Check if camera app is available
            if (cameraIntent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
                return;
            }

            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);

                Log.d(TAG, "Photo URI created: " + currentPhotoUri.toString());
                Log.d(TAG, "Photo file path: " + currentPhotoPath);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);

                // Grant URI permission to camera app
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                cameraLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error opening camera", e);
            Toast.makeText(this, "Unexpected error opening camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Log.d(TAG, "Opening gallery...");

            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");

            // Check if gallery app is available
            if (galleryIntent.resolveActivity(getPackageManager()) == null) {
                // Try alternative intent
                galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

                if (galleryIntent.resolveActivity(getPackageManager()) == null) {
                    Toast.makeText(this, "No gallery app available", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            galleryLauncher.launch(galleryIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening gallery", e);
            Toast.makeText(this, "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo Source");

        String[] options = {"Camera", "Gallery"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Camera
                    Log.d("AddEditPatient", "Camera selected");
                    openCamera();
                    break;
                case 1: // Gallery
                    Log.d("AddEditPatient", "Gallery selected");
                    openGallery();
                    break;
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PATIENT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void handleCameraResult() {
        Log.d(TAG, "Handling camera result...");

        if (currentPhotoUri != null && currentPhotoPath != null) {
            try {
                // Check if file exists
                File imageFile = new File(currentPhotoPath);
                if (!imageFile.exists()) {
                    Log.e(TAG, "Camera image file does not exist: " + currentPhotoPath);
                    Toast.makeText(this, "Camera image file not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Camera image file size: " + imageFile.length() + " bytes");

                // Try to decode the image
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                if (bitmap != null) {
                    Log.d(TAG, "Camera bitmap decoded successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    Bitmap processedBitmap = processImageBitmap(bitmap, currentPhotoPath);
                    imagePatientProfile.setImageBitmap(processedBitmap);
                    buttonSelectImage.setText("Change Photo");

                    Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to decode camera image from: " + currentPhotoPath);
                    Toast.makeText(this, "Failed to process captured photo", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing camera result", e);
                Toast.makeText(this, "Error processing photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Camera result: currentPhotoUri or currentPhotoPath is null");
            Toast.makeText(this, "Error: No photo captured", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission result callback - Request code: " + requestCode);

        if (requestCode == 1001) {
            boolean allGranted = true;
            StringBuilder deniedPermissions = new StringBuilder();

            for (int i = 0; i < permissions.length; i++) {
                Log.d(TAG, "Permission: " + permissions[i] + " = " +
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    if (deniedPermissions.length() > 0) {
                        deniedPermissions.append(", ");
                    }
                    deniedPermissions.append(permissions[i]);
                }
            }

            if (allGranted) {
                Log.d(TAG, "All permissions granted!");
                showImagePickerDialog();
            } else {
                Log.d(TAG, "Permissions denied: " + deniedPermissions.toString());

                // Show specific message about what was denied
                String message = "The following permissions are required:\n";
                if (deniedPermissions.toString().contains("CAMERA")) {
                    message += "• Camera (to take photos)\n";
                }
                if (deniedPermissions.toString().contains("READ_EXTERNAL_STORAGE") ||
                        deniedPermissions.toString().contains("READ_MEDIA_IMAGES")) {
                    message += "• Storage (to select photos from gallery)\n";
                }
                message += "\nPlease enable them in App Settings.";

                // Show dialog with option to go to settings
                new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage(message)
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            // Open app settings
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void setupImageClickListeners() {
        buttonSelectImage.setOnClickListener(v -> {
            Log.d("AddEditPatient", "SELECT PHOTO button clicked!");
            checkPermissionsAndShowImagePicker();
        });

        imagePatientProfile.setOnClickListener(v -> {
            Log.d("AddEditPatient", "Profile image clicked!");
            checkPermissionsAndShowImagePicker();
        });
    }

    private void handleGalleryResult(Uri imageUri) {
        Log.d(TAG, "Handling gallery result: " + imageUri.toString());

        try {
            // Check if we can access the URI
            getContentResolver().takePersistableUriPermission(imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            Log.w(TAG, "Could not take persistable URI permission: " + e.getMessage());
            // Continue anyway, it might still work
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Could not open input stream for: " + imageUri);
                Toast.makeText(this, "Could not access selected image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap != null) {
                Log.d(TAG, "Gallery bitmap decoded successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                Bitmap processedBitmap = processImageBitmap(bitmap, null);
                imagePatientProfile.setImageBitmap(processedBitmap);

                // Save the processed image to internal storage
                currentPhotoPath = saveImageToInternalStorage(processedBitmap);
                if (currentPhotoPath != null) {
                    Log.d(TAG, "Gallery image saved to: " + currentPhotoPath);
                }

                buttonSelectImage.setText("Change Photo");
                Toast.makeText(this, "Photo selected successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to decode gallery image from: " + imageUri);
                Toast.makeText(this, "Failed to process selected image", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException loading gallery image", e);
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading gallery image", e);
            Toast.makeText(this, "Unexpected error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap processImageBitmap(Bitmap originalBitmap, String imagePath) {
        int maxSize = 512;
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        float ratio = Math.min(
                (float) maxSize / originalWidth,
                (float) maxSize / originalHeight
        );

        int newWidth = Math.round(originalWidth * ratio);
        int newHeight = Math.round(originalHeight * ratio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

        if (imagePath != null) {
            resizedBitmap = rotateImageIfRequired(resizedBitmap, imagePath);
        }

        return resizedBitmap;
    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            return bitmap;
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "patient_" + timeStamp + ".jpg";

            File internalDir = new File(getFilesDir(), "patient_images");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }

            File imageFile = new File(internalDir, filename);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            isEditMode = intent.getBooleanExtra(Constants.EXTRA_IS_EDIT_MODE, false);
            patientId = intent.getIntExtra(Constants.EXTRA_PATIENT_ID, -1);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    selectedRegistrationDate = DateUtils.formatDate(
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH) + 1,
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                    );
                    updateRegistrationDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateRegistrationDateDisplay() {
        if (selectedRegistrationDate != null) {
            String displayDate = DateUtils.formatDateForDisplay(selectedRegistrationDate);
            textRegistrationDate.setText(displayDate);
        }
    }

    private void loadPatientData() {
        new Thread(() -> {
            currentPatient = patientRepository.getPatientById(patientId);
            if (currentPatient != null) {
                runOnUiThread(() -> {
                    populateForm(currentPatient);
                    loadPatientImage(currentPatient.getImagePath());
                });
            }
        }).start();
    }

    private void populateForm(Patient patient) {
        editPatientName.setText(patient.getName());
        editAge.setText(String.valueOf(patient.getAge()));

        // Set gender
        String gender = patient.getGender();
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "male":
                    radioGroupGender.check(R.id.radio_male);
                    break;
                case "female":
                    radioGroupGender.check(R.id.radio_female);
                    break;
                default:
                    radioGroupGender.check(R.id.radio_other);
                    break;
            }
        }

        spinnerBloodType.setText(patient.getBloodType(), false);
        editPhone.setText(patient.getPhone());
        editAddress.setText(patient.getAddress());
        editEmergencyContact.setText(patient.getEmergencyContact());
        editEmergencyPhone.setText(patient.getEmergencyPhone());

        setMedicalConditions(patient.getMedicalConditions());
        editAllergies.setText(patient.getAllergies());

        selectedRegistrationDate = patient.getRegistrationDate();
        updateRegistrationDateDisplay();
    }

    private void setMedicalConditions(String conditions) {
        if (conditions != null && !conditions.trim().isEmpty()) {
            String[] conditionArray = conditions.toLowerCase().split(",");

            for (String condition : conditionArray) {
                condition = condition.trim();
                switch (condition) {
                    case "hypertension":
                        checkHypertension.setChecked(true);
                        break;
                    case "diabetes":
                        checkDiabetes.setChecked(true);
                        break;
                    case "asthma":
                        checkAsthma.setChecked(true);
                        break;
                    case "heart disease":
                        checkHeartDisease.setChecked(true);
                        break;
                    default:
                        String currentOther = editOtherConditions.getText() != null ?
                                editOtherConditions.getText().toString() : "";
                        if (!currentOther.contains(condition)) {
                            String newOther = currentOther.isEmpty() ? condition :
                                    currentOther + ", " + condition;
                            editOtherConditions.setText(newOther);
                        }
                        break;
                }
            }
        }
    }

    private void loadPatientImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imagePatientProfile.setImageBitmap(bitmap);
                        buttonSelectImage.setText("Change Photo");
                        currentPhotoPath = imagePath;
                    }
                }
            } catch (Exception e) {
                // Keep default placeholder if image loading fails
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate name
        String name = editPatientName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            layoutPatientName.setError("Name is required");
            isValid = false;
        } else {
            layoutPatientName.setError(null);
        }

        // Validate age
        String ageStr = editAge.getText().toString().trim();
        if (TextUtils.isEmpty(ageStr)) {
            layoutAge.setError("Age is required");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 0 || age > 150) {
                    layoutAge.setError("Please enter a valid age (0-150)");
                    isValid = false;
                } else {
                    layoutAge.setError(null);
                }
            } catch (NumberFormatException e) {
                layoutAge.setError("Please enter a valid age");
                isValid = false;
            }
        }

        return isValid;
    }

    private void savePatient() {
        if (!validateForm()) {
            return;
        }

        buttonSavePatient.setEnabled(false);
        buttonSavePatient.setText("Saving...");

        new Thread(() -> {
            try {
                Patient patient = createPatientFromForm();

                long result;
                if (isEditMode && currentPatient != null) {
                    patient.setId(currentPatient.getId());
                    result = patientRepository.updatePatient(patient);
                } else {
                    result = patientRepository.insertPatient(patient);
                }

                runOnUiThread(() -> {
                    buttonSavePatient.setEnabled(true);
                    buttonSavePatient.setText("Save Patient");

                    if (result > 0) {
                        Toast.makeText(this,
                                isEditMode ? "Patient updated successfully!" : "Patient added successfully!",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving patient. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    buttonSavePatient.setEnabled(true);
                    buttonSavePatient.setText("Save Patient");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private Patient createPatientFromForm() {
        String name = editPatientName.getText().toString().trim();
        int age = Integer.parseInt(editAge.getText().toString().trim());
        String gender = getSelectedGender();
        String bloodType = spinnerBloodType.getText().toString();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String emergencyContact = editEmergencyContact.getText().toString().trim();
        String emergencyPhone = editEmergencyPhone.getText().toString().trim();
        String medicalConditions = getMedicalConditions();
        String allergies = editAllergies.getText().toString().trim();

        if (isEditMode) {
            return new Patient(patientId, name, age, gender, bloodType, phone, address,
                    emergencyContact, emergencyPhone, medicalConditions, allergies,
                    selectedRegistrationDate, currentPhotoPath);
        } else {
            return new Patient(name, age, gender, bloodType, phone, address,
                    emergencyContact, emergencyPhone, medicalConditions, allergies,
                    selectedRegistrationDate, currentPhotoPath);
        }
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_male) {
            return "Male";
        } else if (selectedId == R.id.radio_female) {
            return "Female";
        } else {
            return "Other";
        }
    }

    private String getMedicalConditions() {
        List<String> conditions = new ArrayList<>();

        if (checkHypertension.isChecked()) {
            conditions.add("Hypertension");
        }
        if (checkDiabetes.isChecked()) {
            conditions.add("Diabetes");
        }
        if (checkAsthma.isChecked()) {
            conditions.add("Asthma");
        }
        if (checkHeartDisease.isChecked()) {
            conditions.add("Heart Disease");
        }

        String otherConditions = editOtherConditions.getText().toString().trim();
        if (!TextUtils.isEmpty(otherConditions)) {
            conditions.add(otherConditions);
        }

        return TextUtils.join(", ", conditions);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}