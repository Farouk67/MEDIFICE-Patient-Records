package com.david.patientrecords.utils;

public class Constants {

    // Request codes for activities
    public static final int REQUEST_ADD_PATIENT = 1001;
    public static final int REQUEST_EDIT_PATIENT = 1002;
    public static final int REQUEST_ADD_MEDICAL_RECORD = 1003;
    public static final int REQUEST_EDIT_MEDICAL_RECORD = 1004;
    public static final int REQUEST_ADD_MEDICATION = 1005;
    public static final int REQUEST_EDIT_MEDICATION = 1006;
    public static final int REQUEST_IMAGE_CAPTURE = 1007;
    public static final int REQUEST_IMAGE_GALLERY = 1008;
    public static final int REQUEST_PERMISSION_CAMERA = 1009;
    public static final int REQUEST_PERMISSION_STORAGE = 1010;

    // Intent extras
    public static final String EXTRA_PATIENT_ID = "patient_id";
    public static final String EXTRA_PATIENT_NAME = "patient_name";
    public static final String EXTRA_MEDICAL_RECORD_ID = "medical_record_id";
    public static final String EXTRA_MEDICATION_ID = "medication_id";
    public static final String EXTRA_IS_EDIT_MODE = "is_edit_mode";
    public static final String EXTRA_SEARCH_QUERY = "search_query";

    // Gender options
    public static final String[] GENDERS = {"Male", "Female", "Other"};

    // Blood type options
    public static final String[] BLOOD_TYPES = {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    // Visit types
    public static final String[] VISIT_TYPES = {
            "Regular", "Emergency", "Follow-up", "Consultation", "Check-up"
    };

    // Medication frequencies
    public static final String[] MEDICATION_FREQUENCIES = {
            "Once daily", "Twice daily", "Three times daily", "Four times daily",
            "Every 6 hours", "Every 8 hours", "Every 12 hours",
            "As needed", "Before meals", "After meals", "At bedtime"
    };

    // Doctor specialties
    public static final String[] DOCTOR_SPECIALTIES = {
            "General Practice", "Internal Medicine", "Pediatrics", "Cardiology",
            "Dermatology", "Neurology", "Orthopedics", "Psychiatry", "Surgery",
            "Obstetrics & Gynecology", "Ophthalmology", "ENT", "Urology",
            "Endocrinology", "Pulmonology", "Gastroenterology", "Rheumatology",
            "Oncology", "Radiology", "Pathology", "Emergency Medicine"
    };

    // Date formats
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_DATABASE = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy HH:mm";
    public static final String DATETIME_FORMAT_DATABASE = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT_DISPLAY = "HH:mm";

    // Validation constants
    public static final int MIN_PATIENT_AGE = 0;
    public static final int MAX_PATIENT_AGE = 120;
    public static final int MIN_PATIENT_NAME_LENGTH = 2;
    public static final int MAX_PATIENT_NAME_LENGTH = 100;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MAX_NOTES_LENGTH = 1000;

    // Image settings
    public static final int MAX_IMAGE_SIZE = 1024; // pixels
    public static final int JPEG_QUALITY = 85; // compression quality

    // Animation durations
    public static final int ANIMATION_DURATION_SHORT = 200;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // Search settings
    public static final int SEARCH_DELAY_MS = 300; // Delay before performing search
    public static final int MIN_SEARCH_LENGTH = 2; // Minimum characters to trigger search

    // UI Constants
    public static final int ITEMS_PER_PAGE = 20;
    public static final int CARD_CORNER_RADIUS = 16;
    public static final int CARD_ELEVATION = 8;

    // Shared Preferences keys
    public static final String PREFS_NAME = "PatientRecordsPrefs";
    public static final String PREF_FIRST_RUN = "first_run";
    public static final String PREF_LAST_BACKUP = "last_backup";
    public static final String PREF_THEME_MODE = "theme_mode";
    public static final String PREF_SORT_ORDER = "sort_order";

    // Sort options
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_DATE = "date";
    public static final String SORT_BY_AGE = "age";
    public static final String SORT_ORDER_ASC = "asc";
    public static final String SORT_ORDER_DESC = "desc";

    // Error messages
    public static final String ERROR_PATIENT_NOT_FOUND = "Patient not found";
    public static final String ERROR_INVALID_INPUT = "Please check your input";
    public static final String ERROR_DATABASE_ERROR = "Database error occurred";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied";
    public static final String ERROR_IMAGE_LOAD_FAILED = "Failed to load image";

    // Success messages
    public static final String SUCCESS_PATIENT_ADDED = "Patient added successfully";
    public static final String SUCCESS_PATIENT_UPDATED = "Patient updated successfully";
    public static final String SUCCESS_PATIENT_DELETED = "Patient deleted successfully";
    public static final String SUCCESS_RECORD_ADDED = "Medical record added successfully";
    public static final String SUCCESS_MEDICATION_ADDED = "Medication added successfully";

    // Default values
    public static final String DEFAULT_PROFILE_IMAGE = "default_profile";
    public static final String DEFAULT_DOCTOR_NAME = "Dr. Unknown";
    public static final int DEFAULT_REFILLS = 3;

    // File paths
    public static final String IMAGES_FOLDER = "patient_images";
    public static final String BACKUP_FOLDER = "backups";
    public static final String TEMP_FOLDER = "temp";
}
