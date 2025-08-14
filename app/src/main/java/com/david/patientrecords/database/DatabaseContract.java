package com.david.patientrecords.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    public static final String DATABASE_NAME = "patient_records.db";
    public static final int DATABASE_VERSION = 2; // Incremented for image_path addition

    // Prevent instantiation
    private DatabaseContract() {}

    // Patients table
    public static class PatientEntry implements BaseColumns {
        public static final String TABLE_NAME = "patients";
        public static final String COLUMN_PATIENT_NAME = "patient_name";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_BLOOD_TYPE = "blood_type";
        public static final String COLUMN_EMERGENCY_CONTACT = "emergency_contact";
        public static final String COLUMN_EMERGENCY_PHONE = "emergency_phone";
        public static final String COLUMN_MEDICAL_CONDITIONS = "medical_conditions";
        public static final String COLUMN_ALLERGIES = "allergies";
        public static final String COLUMN_PROFILE_IMAGE = "profile_image"; // Old field
        public static final String COLUMN_IMAGE_PATH = "image_path"; // New field for image paths
        public static final String COLUMN_REGISTRATION_DATE = "registration_date";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    // Medical Records table
    public static class MedicalRecordEntry implements BaseColumns {
        public static final String TABLE_NAME = "medical_records";
        public static final String COLUMN_PATIENT_ID = "patient_id";
        public static final String COLUMN_VISIT_DATE = "visit_date";
        public static final String COLUMN_VISIT_TYPE = "visit_type";
        public static final String COLUMN_SYMPTOMS = "symptoms";
        public static final String COLUMN_DIAGNOSIS = "diagnosis";
        public static final String COLUMN_TREATMENT = "treatment";
        public static final String COLUMN_DOCTOR_NAME = "doctor_name";
        public static final String COLUMN_DOCTOR_SPECIALTY = "doctor_specialty";
        public static final String COLUMN_VITAL_SIGNS = "vital_signs";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_FOLLOW_UP_DATE = "follow_up_date";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    // Medications table
    public static class MedicationEntry implements BaseColumns {
        public static final String TABLE_NAME = "medications";
        public static final String COLUMN_PATIENT_ID = "patient_id";
        public static final String COLUMN_MEDICATION_NAME = "medication_name";
        public static final String COLUMN_GENERIC_NAME = "generic_name";
        public static final String COLUMN_DOSAGE = "dosage";
        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_PRESCRIBED_BY = "prescribed_by";
        public static final String COLUMN_INSTRUCTIONS = "instructions";
        public static final String COLUMN_SIDE_EFFECTS = "side_effects";
        public static final String COLUMN_REFILLS_REMAINING = "refills_remaining";
        public static final String COLUMN_PHARMACY_NAME = "pharmacy_name";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    // SQL statements for creating tables
    public static final String SQL_CREATE_PATIENTS_TABLE =
            "CREATE TABLE " + PatientEntry.TABLE_NAME + " (" +
                    PatientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PatientEntry.COLUMN_PATIENT_NAME + " TEXT NOT NULL," +
                    PatientEntry.COLUMN_AGE + " INTEGER NOT NULL," +
                    PatientEntry.COLUMN_GENDER + " TEXT," +
                    PatientEntry.COLUMN_PHONE + " TEXT," +
                    PatientEntry.COLUMN_ADDRESS + " TEXT," +
                    PatientEntry.COLUMN_BLOOD_TYPE + " TEXT," +
                    PatientEntry.COLUMN_EMERGENCY_CONTACT + " TEXT," +
                    PatientEntry.COLUMN_EMERGENCY_PHONE + " TEXT," +
                    PatientEntry.COLUMN_MEDICAL_CONDITIONS + " TEXT," +
                    PatientEntry.COLUMN_ALLERGIES + " TEXT," +
                    PatientEntry.COLUMN_PROFILE_IMAGE + " TEXT," +
                    PatientEntry.COLUMN_IMAGE_PATH + " TEXT," + // New image path field
                    PatientEntry.COLUMN_REGISTRATION_DATE + " TEXT," +
                    PatientEntry.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1," +
                    PatientEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP," +
                    PatientEntry.COLUMN_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";

    public static final String SQL_CREATE_MEDICAL_RECORDS_TABLE =
            "CREATE TABLE " + MedicalRecordEntry.TABLE_NAME + " (" +
                    MedicalRecordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MedicalRecordEntry.COLUMN_PATIENT_ID + " INTEGER NOT NULL," +
                    MedicalRecordEntry.COLUMN_VISIT_DATE + " TEXT NOT NULL," +
                    MedicalRecordEntry.COLUMN_VISIT_TYPE + " TEXT," +
                    MedicalRecordEntry.COLUMN_SYMPTOMS + " TEXT," +
                    MedicalRecordEntry.COLUMN_DIAGNOSIS + " TEXT," +
                    MedicalRecordEntry.COLUMN_TREATMENT + " TEXT," +
                    MedicalRecordEntry.COLUMN_DOCTOR_NAME + " TEXT," +
                    MedicalRecordEntry.COLUMN_DOCTOR_SPECIALTY + " TEXT," +
                    MedicalRecordEntry.COLUMN_VITAL_SIGNS + " TEXT," +
                    MedicalRecordEntry.COLUMN_NOTES + " TEXT," +
                    MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " TEXT," +
                    MedicalRecordEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP," +
                    MedicalRecordEntry.COLUMN_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + MedicalRecordEntry.COLUMN_PATIENT_ID + ") REFERENCES " +
                    PatientEntry.TABLE_NAME + "(" + PatientEntry._ID + "))";

    public static final String SQL_CREATE_MEDICATIONS_TABLE =
            "CREATE TABLE " + MedicationEntry.TABLE_NAME + " (" +
                    MedicationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MedicationEntry.COLUMN_PATIENT_ID + " INTEGER NOT NULL," +
                    MedicationEntry.COLUMN_MEDICATION_NAME + " TEXT NOT NULL," +
                    MedicationEntry.COLUMN_GENERIC_NAME + " TEXT," +
                    MedicationEntry.COLUMN_DOSAGE + " TEXT," +
                    MedicationEntry.COLUMN_FREQUENCY + " TEXT," +
                    MedicationEntry.COLUMN_START_DATE + " TEXT," +
                    MedicationEntry.COLUMN_END_DATE + " TEXT," +
                    MedicationEntry.COLUMN_PRESCRIBED_BY + " TEXT," +
                    MedicationEntry.COLUMN_INSTRUCTIONS + " TEXT," +
                    MedicationEntry.COLUMN_SIDE_EFFECTS + " TEXT," +
                    MedicationEntry.COLUMN_REFILLS_REMAINING + " INTEGER DEFAULT 0," +
                    MedicationEntry.COLUMN_PHARMACY_NAME + " TEXT," +
                    MedicationEntry.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1," +
                    MedicationEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP," +
                    MedicationEntry.COLUMN_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + MedicationEntry.COLUMN_PATIENT_ID + ") REFERENCES " +
                    PatientEntry.TABLE_NAME + "(" + PatientEntry._ID + "))";

    // SQL statements for dropping tables
    public static final String SQL_DELETE_PATIENTS_TABLE =
            "DROP TABLE IF EXISTS " + PatientEntry.TABLE_NAME;

    public static final String SQL_DELETE_MEDICAL_RECORDS_TABLE =
            "DROP TABLE IF EXISTS " + MedicalRecordEntry.TABLE_NAME;

    public static final String SQL_DELETE_MEDICATIONS_TABLE =
            "DROP TABLE IF EXISTS " + MedicationEntry.TABLE_NAME;
}