package com.david.patientrecords.utils;

import android.text.TextUtils;
import android.util.Patterns;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String TAG = "ValidationUtils";

    // Regex patterns
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[1-9]?[0-9]{7,15}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s.'-]{2,50}$");
    private static final Pattern DOCTOR_NAME_PATTERN =
            Pattern.compile("^(Dr\\.?\\s)?[a-zA-Z\\s.'-]{2,50}$");

    /**
     * Validate patient name
     */
    public static ValidationResult validatePatientName(String name) {
        if (TextUtils.isEmpty(name)) {
            return new ValidationResult(false, "Patient name is required");
        }

        name = name.trim();

        if (name.length() < Constants.MIN_PATIENT_NAME_LENGTH) {
            return new ValidationResult(false,
                    "Name must be at least " + Constants.MIN_PATIENT_NAME_LENGTH + " characters");
        }

        if (name.length() > Constants.MAX_PATIENT_NAME_LENGTH) {
            return new ValidationResult(false,
                    "Name must be less than " + Constants.MAX_PATIENT_NAME_LENGTH + " characters");
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            return new ValidationResult(false,
                    "Name can only contain letters, spaces, periods, hyphens, and apostrophes");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate age
     */
    public static ValidationResult validateAge(String ageStr) {
        if (TextUtils.isEmpty(ageStr)) {
            return new ValidationResult(false, "Age is required");
        }

        try {
            int age = Integer.parseInt(ageStr.trim());

            if (age < Constants.MIN_PATIENT_AGE) {
                return new ValidationResult(false, "Age cannot be negative");
            }

            if (age > Constants.MAX_PATIENT_AGE) {
                return new ValidationResult(false,
                        "Age must be less than " + Constants.MAX_PATIENT_AGE);
            }

            return new ValidationResult(true, null);

        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Please enter a valid age");
        }
    }

    /**
     * Validate phone number
     */
    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return new ValidationResult(true, null); // Phone is optional
        }

        phone = phone.trim().replaceAll("\\s+", ""); // Remove spaces

        if (phone.length() > Constants.MAX_PHONE_LENGTH) {
            return new ValidationResult(false, "Phone number is too long");
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false,
                    "Please enter a valid phone number (e.g., +1-555-0123 or 5550123)");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate email address
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(true, null); // Email is optional
        }

        email = email.trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate doctor name
     */
    public static ValidationResult validateDoctorName(String doctorName) {
        if (TextUtils.isEmpty(doctorName)) {
            return new ValidationResult(false, "Doctor name is required");
        }

        doctorName = doctorName.trim();

        if (doctorName.length() < 2) {
            return new ValidationResult(false, "Doctor name is too short");
        }

        if (!DOCTOR_NAME_PATTERN.matcher(doctorName).matches()) {
            return new ValidationResult(false,
                    "Doctor name format is invalid (e.g., Dr. Smith or Dr Smith)");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate medical field (symptoms, diagnosis, treatment)
     */
    public static ValidationResult validateMedicalField(String field, String fieldName, boolean isRequired) {
        if (TextUtils.isEmpty(field)) {
            if (isRequired) {
                return new ValidationResult(false, fieldName + " is required");
            }
            return new ValidationResult(true, null);
        }

        field = field.trim();

        if (field.length() < 3) {
            return new ValidationResult(false, fieldName + " is too short (minimum 3 characters)");
        }

        if (field.length() > Constants.MAX_NOTES_LENGTH) {
            return new ValidationResult(false, fieldName + " is too long (maximum " +
                    Constants.MAX_NOTES_LENGTH + " characters)");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate address
     */
    public static ValidationResult validateAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return new ValidationResult(true, null); // Address is optional
        }

        address = address.trim();

        if (address.length() > Constants.MAX_ADDRESS_LENGTH) {
            return new ValidationResult(false,
                    "Address is too long (maximum " + Constants.MAX_ADDRESS_LENGTH + " characters)");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate blood type
     */
    public static ValidationResult validateBloodType(String bloodType) {
        if (TextUtils.isEmpty(bloodType)) {
            return new ValidationResult(true, null); // Blood type is optional
        }

        bloodType = bloodType.trim().toUpperCase();

        for (String validType : Constants.BLOOD_TYPES) {
            if (validType.equals(bloodType)) {
                return new ValidationResult(true, null);
            }
        }

        return new ValidationResult(false, "Please select a valid blood type");
    }

    /**
     * Validate date format (YYYY-MM-DD)
     */
    public static ValidationResult validateDate(String date) {
        if (TextUtils.isEmpty(date)) {
            return new ValidationResult(false, "Date is required");
        }

        try {
            // Basic format check
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return new ValidationResult(false, "Invalid date format (expected YYYY-MM-DD)");
            }

            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            if (year < 1900 || year > 2100) {
                return new ValidationResult(false, "Year must be between 1900 and 2100");
            }

            if (month < 1 || month > 12) {
                return new ValidationResult(false, "Month must be between 1 and 12");
            }

            if (day < 1 || day > 31) {
                return new ValidationResult(false, "Day must be between 1 and 31");
            }

            return new ValidationResult(true, null);

        } catch (Exception e) {
            return new ValidationResult(false, "Invalid date format");
        }
    }

    /**
     * Validate vital signs format
     */
    public static ValidationResult validateVitalSigns(String vitalSigns) {
        if (TextUtils.isEmpty(vitalSigns)) {
            return new ValidationResult(true, null); // Vital signs are optional
        }

        vitalSigns = vitalSigns.trim();

        if (vitalSigns.length() > 200) {
            return new ValidationResult(false, "Vital signs description is too long");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Apply validation result to TextInputLayout
     */
    public static void applyValidationResult(TextInputLayout layout, ValidationResult result) {
        if (result.isValid()) {
            layout.setError(null);
            layout.setErrorEnabled(false);
        } else {
            layout.setError(result.getErrorMessage());
            layout.setErrorEnabled(true);
        }
    }

    /**
     * Validate form field and apply result
     */
    public static boolean validateAndApply(TextInputLayout layout, String value,
                                           FieldValidator validator) {
        ValidationResult result = validator.validate(value);
        applyValidationResult(layout, result);
        return result.isValid();
    }

    /**
     * Clear all errors from multiple TextInputLayouts
     */
    public static void clearErrors(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            if (layout != null) {
                layout.setError(null);
                layout.setErrorEnabled(false);
            }
        }
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Interface for custom field validators
     */
    public interface FieldValidator {
        ValidationResult validate(String value);
    }

    /**
     * Common field validators
     */
    public static class CommonValidators {
        public static final FieldValidator PATIENT_NAME = ValidationUtils::validatePatientName;
        public static final FieldValidator AGE = ValidationUtils::validateAge;
        public static final FieldValidator PHONE = ValidationUtils::validatePhone;
        public static final FieldValidator EMAIL = ValidationUtils::validateEmail;
        public static final FieldValidator DOCTOR_NAME = ValidationUtils::validateDoctorName;
        public static final FieldValidator ADDRESS = ValidationUtils::validateAddress;
        public static final FieldValidator BLOOD_TYPE = ValidationUtils::validateBloodType;
        public static final FieldValidator DATE = ValidationUtils::validateDate;
        public static final FieldValidator VITAL_SIGNS = ValidationUtils::validateVitalSigns;

        public static FieldValidator medicalField(String fieldName, boolean required) {
            return value -> validateMedicalField(value, fieldName, required);
        }
    }
}