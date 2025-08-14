package com.david.patientrecords.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;

    // Singleton pattern to prevent multiple database connections
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        try {
            // Create tables in order (Patients first, then tables with foreign keys)
            db.execSQL(DatabaseContract.SQL_CREATE_PATIENTS_TABLE);
            db.execSQL(DatabaseContract.SQL_CREATE_MEDICAL_RECORDS_TABLE);
            db.execSQL(DatabaseContract.SQL_CREATE_MEDICATIONS_TABLE);

            Log.d(TAG, "Database tables created successfully");

            // Insert sample data for demonstration
            insertSampleData(db);

        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop all tables and recreate them
        db.execSQL(DatabaseContract.SQL_DELETE_MEDICATIONS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_MEDICAL_RECORDS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_PATIENTS_TABLE);

        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * Insert sample data for demonstration purposes
     */
    private void insertSampleData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting sample data...");

        try {
            // Sample Patients
            insertSamplePatient(db, "John Anderson", 35, "Male", "+1-555-0101",
                    "123 Main St, Springfield", "O+", "Jane Anderson", "+1-555-0102",
                    "Hypertension", "Penicillin");

            insertSamplePatient(db, "Sarah Johnson", 28, "Female", "+1-555-0201",
                    "456 Oak Ave, Springfield", "A+", "Mike Johnson", "+1-555-0202",
                    "Diabetes Type 2", "Latex");

            insertSamplePatient(db, "Robert Smith", 45, "Male", "+1-555-0301",
                    "789 Pine St, Springfield", "B-", "Mary Smith", "+1-555-0302",
                    "Asthma", "Aspirin");

            insertSamplePatient(db, "Emily Davis", 32, "Female", "+1-555-0401",
                    "321 Elm St, Springfield", "AB+", "David Davis", "+1-555-0402",
                    null, "Shellfish");

            insertSamplePatient(db, "Michael Wilson", 52, "Male", "+1-555-0501",
                    "654 Maple Ave, Springfield", "O-", "Lisa Wilson", "+1-555-0502",
                    "High Cholesterol", null);

            // Sample Medical Records
            insertSampleMedicalRecord(db, 1, "Headache, nausea", "Migraine",
                    "Prescribed pain medication and rest", "Dr. Jennifer Martinez", "Neurology");

            insertSampleMedicalRecord(db, 2, "Frequent urination, thirst", "Diabetes follow-up",
                    "Adjusted insulin dosage", "Dr. Mark Thompson", "Endocrinology");

            insertSampleMedicalRecord(db, 3, "Shortness of breath", "Asthma exacerbation",
                    "Prescribed inhaler, follow-up in 2 weeks", "Dr. Lisa Chen", "Pulmonology");

            // Sample Medications
            insertSampleMedication(db, 1, "Sumatriptan", "Imitrex", "50mg", "As needed",
                    "Dr. Jennifer Martinez", "Take at onset of migraine");

            insertSampleMedication(db, 2, "Metformin", "Glucophage", "500mg", "Twice daily",
                    "Dr. Mark Thompson", "Take with meals");

            insertSampleMedication(db, 3, "Albuterol", "ProAir", "90mcg", "2 puffs every 4-6 hours",
                    "Dr. Lisa Chen", "Use as rescue inhaler");

            Log.d(TAG, "Sample data inserted successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data: " + e.getMessage());
        }
    }

    private void insertSamplePatient(SQLiteDatabase db, String name, int age, String gender,
                                     String phone, String address, String bloodType,
                                     String emergencyContact, String emergencyPhone,
                                     String medicalConditions, String allergies) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME, name);
        values.put(DatabaseContract.PatientEntry.COLUMN_AGE, age);
        values.put(DatabaseContract.PatientEntry.COLUMN_GENDER, gender);
        values.put(DatabaseContract.PatientEntry.COLUMN_PHONE, phone);
        values.put(DatabaseContract.PatientEntry.COLUMN_ADDRESS, address);
        values.put(DatabaseContract.PatientEntry.COLUMN_BLOOD_TYPE, bloodType);
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_CONTACT, emergencyContact);
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_PHONE, emergencyPhone);
        values.put(DatabaseContract.PatientEntry.COLUMN_MEDICAL_CONDITIONS, medicalConditions);
        values.put(DatabaseContract.PatientEntry.COLUMN_ALLERGIES, allergies);
        values.put(DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE, getCurrentDate());

        long newRowId = db.insert(DatabaseContract.PatientEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted patient: " + name + " with ID: " + newRowId);
    }

    private void insertSampleMedicalRecord(SQLiteDatabase db, long patientId, String symptoms,
                                           String diagnosis, String treatment, String doctorName,
                                           String doctorSpecialty) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID, patientId);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_SYMPTOMS, symptoms);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DIAGNOSIS, diagnosis);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_TREATMENT, treatment);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_NAME, doctorName);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_SPECIALTY, doctorSpecialty);
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE, getCurrentDate());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_TYPE, "Regular");

        long newRowId = db.insert(DatabaseContract.MedicalRecordEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted medical record with ID: " + newRowId);
    }

    private void insertSampleMedication(SQLiteDatabase db, long patientId, String medicationName,
                                        String genericName, String dosage, String frequency,
                                        String prescribedBy, String instructions) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID, patientId);
        values.put(DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME, medicationName);
        values.put(DatabaseContract.MedicationEntry.COLUMN_GENERIC_NAME, genericName);
        values.put(DatabaseContract.MedicationEntry.COLUMN_DOSAGE, dosage);
        values.put(DatabaseContract.MedicationEntry.COLUMN_FREQUENCY, frequency);
        values.put(DatabaseContract.MedicationEntry.COLUMN_PRESCRIBED_BY, prescribedBy);
        values.put(DatabaseContract.MedicationEntry.COLUMN_INSTRUCTIONS, instructions);
        values.put(DatabaseContract.MedicationEntry.COLUMN_START_DATE, getCurrentDate());
        values.put(DatabaseContract.MedicationEntry.COLUMN_REFILLS_REMAINING, 5);

        long newRowId = db.insert(DatabaseContract.MedicationEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted medication with ID: " + newRowId);
    }

    /**
     * Get current date in YYYY-MM-DD format
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current timestamp in YYYY-MM-DD HH:MM:SS format
     */
    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get database statistics for dashboard
     */
    public int[] getDatabaseStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] stats = new int[3]; // [patients, records, medications]

        try {
            // Count active patients
            String countPatientsQuery = "SELECT COUNT(*) FROM " +
                    DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            android.database.Cursor cursor = db.rawQuery(countPatientsQuery, null);
            if (cursor.moveToFirst()) {
                stats[0] = cursor.getInt(0);
            }
            cursor.close();

            // Count medical records
            String countRecordsQuery = "SELECT COUNT(*) FROM " +
                    DatabaseContract.MedicalRecordEntry.TABLE_NAME;
            cursor = db.rawQuery(countRecordsQuery, null);
            if (cursor.moveToFirst()) {
                stats[1] = cursor.getInt(0);
            }
            cursor.close();

            // Count active medications
            String countMedicationsQuery = "SELECT COUNT(*) FROM " +
                    DatabaseContract.MedicationEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1";
            cursor = db.rawQuery(countMedicationsQuery, null);
            if (cursor.moveToFirst()) {
                stats[2] = cursor.getInt(0);
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting database stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Clear all data from database (for testing purposes)
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DatabaseContract.MedicationEntry.TABLE_NAME, null, null);
            db.delete(DatabaseContract.MedicalRecordEntry.TABLE_NAME, null, null);
            db.delete(DatabaseContract.PatientEntry.TABLE_NAME, null, null);
            Log.d(TAG, "All data cleared from database");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing database: " + e.getMessage());
        }
    }

    /**
     * Get database version info
     */
    public String getDatabaseInfo() {
        return "Database: " + DatabaseContract.DATABASE_NAME +
                ", Version: " + DatabaseContract.DATABASE_VERSION +
                ", Path: " + getDatabaseName();
    }
}