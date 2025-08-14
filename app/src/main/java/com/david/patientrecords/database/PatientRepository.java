package com.david.patientrecords.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.david.patientrecords.models.Patient;
import com.david.patientrecords.models.MedicalRecord;
import com.david.patientrecords.models.Medication;

import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    private static final String TAG = "PatientRepository";
    private DatabaseHelper dbHelper;
    private static PatientRepository instance;

    // Singleton pattern
    public static synchronized PatientRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PatientRepository(context);
        }
        return instance;
    }

    private PatientRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // ==================== PATIENT OPERATIONS ====================

    /**
     * Insert a new patient
     */
    public long insertPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME, patient.getPatientName());
        values.put(DatabaseContract.PatientEntry.COLUMN_AGE, patient.getAge());
        values.put(DatabaseContract.PatientEntry.COLUMN_GENDER, patient.getGender());
        values.put(DatabaseContract.PatientEntry.COLUMN_PHONE, patient.getPhone());
        values.put(DatabaseContract.PatientEntry.COLUMN_ADDRESS, patient.getAddress());
        values.put(DatabaseContract.PatientEntry.COLUMN_BLOOD_TYPE, patient.getBloodType());
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_CONTACT, patient.getEmergencyContact());
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_PHONE, patient.getEmergencyPhone());
        values.put(DatabaseContract.PatientEntry.COLUMN_MEDICAL_CONDITIONS, patient.getMedicalConditions());
        values.put(DatabaseContract.PatientEntry.COLUMN_ALLERGIES, patient.getAllergies());
        values.put(DatabaseContract.PatientEntry.COLUMN_PROFILE_IMAGE, patient.getProfileImage());
        values.put(DatabaseContract.PatientEntry.COLUMN_IMAGE_PATH, patient.getImagePath()); // NEW: Image path support
        values.put(DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE, patient.getRegistrationDate());
        values.put(DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE, patient.isActive() ? 1 : 0);

        long newRowId = db.insert(DatabaseContract.PatientEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Log.d(TAG, "Patient inserted successfully with ID: " + newRowId);
        } else {
            Log.e(TAG, "Error inserting patient");
        }

        return newRowId;
    }
    /**
     * Get all medical records across all patients
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        List<MedicalRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT mr.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " ORDER BY mr." + DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicalRecord record = cursorToMedicalRecord(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    record.setPatientName(cursor.getString(patientNameIndex));
                }
                records.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + records.size() + " medical records");
        return records;
    }

    /**
     * Get all medications across all patients
     */
    public List<Medication> getAllMedications() {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicationEntry.TABLE_NAME + " m " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON m." + DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE m." + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " ORDER BY m." + DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Medication medication = cursorToMedication(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    medication.setPatientName(cursor.getString(patientNameIndex));
                }
                medications.add(medication);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + medications.size() + " active medications");
        return medications;
    }

    /**
     * Get upcoming follow-up appointments
     */
    public List<MedicalRecord> getUpcomingFollowUps() {
        List<MedicalRecord> followUps = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT mr.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " IS NOT NULL" +
                " AND mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " >= date('now')" +
                " ORDER BY mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicalRecord record = cursorToMedicalRecord(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    record.setPatientName(cursor.getString(patientNameIndex));
                }
                followUps.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + followUps.size() + " upcoming follow-ups");
        return followUps;
    }

    /**
     * Get total patients count
     */
    public int getTotalPatientsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total patients count: " + e.getMessage());
        }

        Log.d(TAG, "Total patients count: " + count);
        return count;
    }

    /**
     * Get total medical records count
     */
    public int getTotalMedicalRecordsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                    "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                    "ON mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                    " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total medical records count: " + e.getMessage());
        }

        Log.d(TAG, "Total medical records count: " + count);
        return count;
    }

    /**
     * Get total medications count
     */
    public int getTotalMedicationsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseContract.MedicationEntry.TABLE_NAME + " m " +
                    "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                    "ON m." + DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                    " WHERE m." + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1" +
                    " AND p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total medications count: " + e.getMessage());
        }

        Log.d(TAG, "Total medications count: " + count);
        return count;
    }

    /**
     * Get upcoming follow-ups count
     */
    public int getUpcomingFollowUpsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                    "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                    "ON mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                    " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                    " AND mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " IS NOT NULL" +
                    " AND mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " >= date('now')";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting upcoming follow-ups count: " + e.getMessage());
        }

        Log.d(TAG, "Upcoming follow-ups count: " + count);
        return count;
    }

    /**
     * Get recent patients (registered in last 30 days)
     */
    public List<Patient> getRecentPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = ? AND " +
                DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE + " >= date('now', '-30 days')";
        String[] selectionArgs = {"1"};
        String sortOrder = DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + patients.size() + " recent patients");
        return patients;
    }

    /**
     * Get medications expiring within next X days
     */
    public List<Medication> getMedicationsExpiringInDays(int days) {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicationEntry.TABLE_NAME + " m " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON m." + DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE m." + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND m." + DatabaseContract.MedicationEntry.COLUMN_END_DATE + " BETWEEN date('now') AND date('now', '+" + days + " days')" +
                " ORDER BY m." + DatabaseContract.MedicationEntry.COLUMN_END_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Medication medication = cursorToMedication(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    medication.setPatientName(cursor.getString(patientNameIndex));
                }
                medications.add(medication);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + medications.size() + " medications expiring in " + days + " days");
        return medications;
    }

    /**
     * Get all active patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};
        String sortOrder = DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Retrieved " + patients.size() + " patients");
        return patients;
    }



    /**
     * Get patient by ID
     */
    public Patient getPatientById(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Patient patient = null;
        if (cursor != null && cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
            cursor.close();
        }

        return patient;
    }

    /**
     * Update patient information (UPDATED VERSION with image path support)
     */
    public int updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME, patient.getPatientName());
        values.put(DatabaseContract.PatientEntry.COLUMN_AGE, patient.getAge());
        values.put(DatabaseContract.PatientEntry.COLUMN_GENDER, patient.getGender());
        values.put(DatabaseContract.PatientEntry.COLUMN_PHONE, patient.getPhone());
        values.put(DatabaseContract.PatientEntry.COLUMN_ADDRESS, patient.getAddress());
        values.put(DatabaseContract.PatientEntry.COLUMN_BLOOD_TYPE, patient.getBloodType());
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_CONTACT, patient.getEmergencyContact());
        values.put(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_PHONE, patient.getEmergencyPhone());
        values.put(DatabaseContract.PatientEntry.COLUMN_MEDICAL_CONDITIONS, patient.getMedicalConditions());
        values.put(DatabaseContract.PatientEntry.COLUMN_ALLERGIES, patient.getAllergies());
        values.put(DatabaseContract.PatientEntry.COLUMN_PROFILE_IMAGE, patient.getProfileImage());
        values.put(DatabaseContract.PatientEntry.COLUMN_IMAGE_PATH, patient.getImagePath()); // NEW: Image path support
        values.put(DatabaseContract.PatientEntry.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        String selection = DatabaseContract.PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patient.getId())};

        int rowsAffected = db.update(
                DatabaseContract.PatientEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Updated patient ID " + patient.getId() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }


    /**
     * Soft delete patient (set inactive)
     */
    public int deletePatient(long patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE, 0);
        values.put(DatabaseContract.PatientEntry.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        String selection = DatabaseContract.PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};

        int rowsAffected = db.update(
                DatabaseContract.PatientEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Soft deleted patient ID " + patientId + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Search patients by name or phone
     */
    public List<Patient> searchPatients(String query) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "(" + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME + " LIKE ? OR " +
                DatabaseContract.PatientEntry.COLUMN_PHONE + " LIKE ?) AND " +
                DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {"%" + query + "%", "%" + query + "%", "1"};
        String sortOrder = DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Search query '" + query + "' returned " + patients.size() + " patients");
        return patients;
    }

    // ==================== MEDICAL RECORD OPERATIONS ====================

    /**
     * Insert medical record
     */
    public long insertMedicalRecord(MedicalRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID, record.getPatientId());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE, record.getVisitDate());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_SYMPTOMS, record.getSymptoms());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DIAGNOSIS, record.getDiagnosis());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_TREATMENT, record.getTreatment());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_NAME, record.getDoctorName());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_SPECIALTY, record.getDoctorSpecialty());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VITAL_SIGNS, record.getVitalSigns());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_NOTES, record.getNotes());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE, record.getFollowUpDate());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_TYPE, record.getVisitType());

        long newRowId = db.insert(DatabaseContract.MedicalRecordEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Medical record inserted with ID: " + newRowId);
        return newRowId;
    }

    /**
     * Get medical records for a patient
     */
    public List<MedicalRecord> getMedicalRecordsByPatientId(long patientId) {
        List<MedicalRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};
        String sortOrder = DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicalRecord record = cursorToMedicalRecord(cursor);
                records.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return records;
    }

    /**
     * Get recent medical records (last 10)
     */
    public List<MedicalRecord> getRecentMedicalRecords() {
        List<MedicalRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT mr.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " ORDER BY mr." + DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE + " DESC " +
                "LIMIT 10";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicalRecord record = cursorToMedicalRecord(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    record.setPatientName(cursor.getString(patientNameIndex));
                }
                records.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return records;
    }

    /**
     * Update medical record
     */
    public int updateMedicalRecord(MedicalRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE, record.getVisitDate());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_SYMPTOMS, record.getSymptoms());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DIAGNOSIS, record.getDiagnosis());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_TREATMENT, record.getTreatment());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_NAME, record.getDoctorName());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_SPECIALTY, record.getDoctorSpecialty());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VITAL_SIGNS, record.getVitalSigns());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_NOTES, record.getNotes());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE, record.getFollowUpDate());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_TYPE, record.getVisitType());
        values.put(DatabaseContract.MedicalRecordEntry.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        String selection = DatabaseContract.MedicalRecordEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(record.getId())};

        int rowsAffected = db.update(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Updated medical record ID " + record.getId() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Delete medical record
     */
    public int deleteMedicalRecord(long recordId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DatabaseContract.MedicalRecordEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(recordId)};

        int rowsAffected = db.delete(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Deleted medical record ID " + recordId + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    // ==================== MEDICATION OPERATIONS ====================

    /**
     * Insert medication
     */
    public long insertMedication(Medication medication) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID, medication.getPatientId());
        values.put(DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME, medication.getMedicationName());
        values.put(DatabaseContract.MedicationEntry.COLUMN_GENERIC_NAME, medication.getGenericName());
        values.put(DatabaseContract.MedicationEntry.COLUMN_DOSAGE, medication.getDosage());
        values.put(DatabaseContract.MedicationEntry.COLUMN_FREQUENCY, medication.getFrequency());
        values.put(DatabaseContract.MedicationEntry.COLUMN_START_DATE, medication.getStartDate());
        values.put(DatabaseContract.MedicationEntry.COLUMN_END_DATE, medication.getEndDate());
        values.put(DatabaseContract.MedicationEntry.COLUMN_PRESCRIBED_BY, medication.getPrescribedBy());
        values.put(DatabaseContract.MedicationEntry.COLUMN_INSTRUCTIONS, medication.getInstructions());
        values.put(DatabaseContract.MedicationEntry.COLUMN_SIDE_EFFECTS, medication.getSideEffects());
        values.put(DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE, medication.isActive() ? 1 : 0);
        values.put(DatabaseContract.MedicationEntry.COLUMN_REFILLS_REMAINING, medication.getRefillsRemaining());
        values.put(DatabaseContract.MedicationEntry.COLUMN_PHARMACY_NAME, medication.getPharmacyName());

        long newRowId = db.insert(DatabaseContract.MedicationEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Medication inserted with ID: " + newRowId);
        return newRowId;
    }

    /**
     * Get medications for a patient
     */
    public List<Medication> getMedicationsByPatientId(long patientId) {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = ? AND " +
                DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {String.valueOf(patientId), "1"};
        String sortOrder = DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.MedicationEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Medication medication = cursorToMedication(cursor);
                medications.add(medication);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return medications;
    }

    /**
     * Get all active medications across all patients
     */
    public List<Medication> getAllActiveMedications() {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicationEntry.TABLE_NAME + " m " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON m." + DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE m." + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " ORDER BY m." + DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Medication medication = cursorToMedication(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    medication.setPatientName(cursor.getString(patientNameIndex));
                }
                medications.add(medication);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return medications;
    }

    /**
     * Delete medication (actually deactivate)
     */
    public boolean deleteMedication(long medicationId) {
        try {
            int result = deactivateMedication(medicationId);
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting medication", e);
            return false;
        }
    }


    /**
     * Update medication
     */
    public int updateMedication(Medication medication) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME, medication.getMedicationName());
        values.put(DatabaseContract.MedicationEntry.COLUMN_GENERIC_NAME, medication.getGenericName());
        values.put(DatabaseContract.MedicationEntry.COLUMN_DOSAGE, medication.getDosage());
        values.put(DatabaseContract.MedicationEntry.COLUMN_FREQUENCY, medication.getFrequency());
        values.put(DatabaseContract.MedicationEntry.COLUMN_START_DATE, medication.getStartDate());
        values.put(DatabaseContract.MedicationEntry.COLUMN_END_DATE, medication.getEndDate());
        values.put(DatabaseContract.MedicationEntry.COLUMN_PRESCRIBED_BY, medication.getPrescribedBy());
        values.put(DatabaseContract.MedicationEntry.COLUMN_INSTRUCTIONS, medication.getInstructions());
        values.put(DatabaseContract.MedicationEntry.COLUMN_SIDE_EFFECTS, medication.getSideEffects());
        values.put(DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE, medication.isActive() ? 1 : 0);
        values.put(DatabaseContract.MedicationEntry.COLUMN_REFILLS_REMAINING, medication.getRefillsRemaining());
        values.put(DatabaseContract.MedicationEntry.COLUMN_PHARMACY_NAME, medication.getPharmacyName());
        values.put(DatabaseContract.MedicationEntry.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        String selection = DatabaseContract.MedicationEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(medication.getId())};

        int rowsAffected = db.update(
                DatabaseContract.MedicationEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Updated medication ID " + medication.getId() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Deactivate medication
     */
    public int deactivateMedication(long medicationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE, 0);
        values.put(DatabaseContract.MedicationEntry.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        String selection = DatabaseContract.MedicationEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(medicationId)};

        int rowsAffected = db.update(
                DatabaseContract.MedicationEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Deactivated medication ID " + medicationId + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert cursor to Patient object
     */
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        patient.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry._ID)));
        patient.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME)));
        patient.setAge(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_AGE)));
        patient.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_GENDER)));
        patient.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_PHONE)));
        patient.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_ADDRESS)));
        patient.setBloodType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_BLOOD_TYPE)));
        patient.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_CONTACT)));
        patient.setEmergencyPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_EMERGENCY_PHONE)));
        patient.setMedicalConditions(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_MEDICAL_CONDITIONS)));
        patient.setAllergies(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_ALLERGIES)));
        patient.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_PROFILE_IMAGE)));

        // NEW: Handle image path with safe column access
        int imagePathColumnIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_IMAGE_PATH);
        if (imagePathColumnIndex != -1) {
            patient.setImagePath(cursor.getString(imagePathColumnIndex));
        }

        patient.setRegistrationDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE)));
        patient.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE)) == 1);
        patient.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_CREATED_AT)));
        patient.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatientEntry.COLUMN_UPDATED_AT)));

        return patient;
    }



    /**
     * Convert cursor to MedicalRecord object
     */
    private MedicalRecord cursorToMedicalRecord(Cursor cursor) {
        MedicalRecord record = new MedicalRecord();

        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry._ID)));
        record.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID)));
        record.setVisitDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE)));
        record.setSymptoms(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_SYMPTOMS)));
        record.setDiagnosis(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_DIAGNOSIS)));
        record.setTreatment(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_TREATMENT)));
        record.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_NAME)));
        record.setDoctorSpecialty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_DOCTOR_SPECIALTY)));
        record.setVitalSigns(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_VITAL_SIGNS)));
        record.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_NOTES)));
        record.setFollowUpDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE)));
        record.setVisitType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_TYPE)));
        record.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_CREATED_AT)));
        record.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicalRecordEntry.COLUMN_UPDATED_AT)));

        return record;
    }

    /**
     * Convert cursor to Medication object
     */
    private Medication cursorToMedication(Cursor cursor) {
        Medication medication = new Medication();

        medication.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry._ID)));
        medication.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID)));
        medication.setMedicationName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_MEDICATION_NAME)));
        medication.setGenericName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_GENERIC_NAME)));
        medication.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_DOSAGE)));
        medication.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_FREQUENCY)));
        medication.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_START_DATE)));
        medication.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_END_DATE)));
        medication.setPrescribedBy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_PRESCRIBED_BY)));
        medication.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_INSTRUCTIONS)));
        medication.setSideEffects(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_SIDE_EFFECTS)));
        medication.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE)) == 1);
        medication.setRefillsRemaining(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_REFILLS_REMAINING)));
        medication.setPharmacyName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_PHARMACY_NAME)));
        medication.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_CREATED_AT)));
        medication.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MedicationEntry.COLUMN_UPDATED_AT)));

        return medication;
    }

    // ==================== ADDITIONAL UTILITY METHODS ====================

    /**
     * Get patients with upcoming follow-up appointments
     */
    public List<Patient> getPatientsWithUpcomingFollowUps() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT p.* FROM " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "INNER JOIN " + DatabaseContract.MedicalRecordEntry.TABLE_NAME + " mr " +
                "ON p." + DatabaseContract.PatientEntry._ID + " = mr." + DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID +
                " WHERE p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " >= date('now')" +
                " ORDER BY mr." + DatabaseContract.MedicalRecordEntry.COLUMN_FOLLOW_UP_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get patients by blood type
     */
    public List<Patient> getPatientsByBloodType(String bloodType) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.PatientEntry.COLUMN_BLOOD_TYPE + " = ? AND " +
                DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {bloodType, "1"};
        String sortOrder = DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get patients by age range
     */
    public List<Patient> getPatientsByAgeRange(int minAge, int maxAge) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.PatientEntry.COLUMN_AGE + " >= ? AND " +
                DatabaseContract.PatientEntry.COLUMN_AGE + " <= ? AND " +
                DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {String.valueOf(minAge), String.valueOf(maxAge), "1"};
        String sortOrder = DatabaseContract.PatientEntry.COLUMN_AGE + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient patient = cursorToPatient(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get medications expiring soon (within next 30 days)
     */
    public List<Medication> getMedicationsExpiringSoon() {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.*, p." + DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME +
                " FROM " + DatabaseContract.MedicationEntry.TABLE_NAME + " m " +
                "INNER JOIN " + DatabaseContract.PatientEntry.TABLE_NAME + " p " +
                "ON m." + DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = p." + DatabaseContract.PatientEntry._ID +
                " WHERE m." + DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND p." + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                " AND m." + DatabaseContract.MedicationEntry.COLUMN_END_DATE + " BETWEEN date('now') AND date('now', '+30 days')" +
                " ORDER BY m." + DatabaseContract.MedicationEntry.COLUMN_END_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Medication medication = cursorToMedication(cursor);
                // Get patient name from joined query
                int patientNameIndex = cursor.getColumnIndex(DatabaseContract.PatientEntry.COLUMN_PATIENT_NAME);
                if (patientNameIndex != -1) {
                    medication.setPatientName(cursor.getString(patientNameIndex));
                }
                medications.add(medication);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return medications;
    }

    /**
     * Get medical record by ID
     */
    public MedicalRecord getMedicalRecordById(long recordId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicalRecordEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(recordId)};

        Cursor cursor = db.query(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        MedicalRecord record = null;
        if (cursor != null && cursor.moveToFirst()) {
            record = cursorToMedicalRecord(cursor);
            cursor.close();
        }

        return record;
    }

    /**
     * Get medication by ID
     */
    public Medication getMedicationById(long medicationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicationEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(medicationId)};

        Cursor cursor = db.query(
                DatabaseContract.MedicationEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Medication medication = null;
        if (cursor != null && cursor.moveToFirst()) {
            medication = cursorToMedication(cursor);
            cursor.close();
        }

        return medication;
    }

    /**
     * Get dashboard statistics
     */
    public int[] getDashboardStats() {
        return dbHelper.getDatabaseStats();
    }

    /**
     * Get patient count by gender
     */
    public int[] getPatientCountByGender() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int[] genderCounts = new int[3]; // [male, female, other]

        try {
            // Count male patients
            String maleQuery = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_GENDER + " = 'Male' AND " +
                    DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            Cursor cursor = db.rawQuery(maleQuery, null);
            if (cursor.moveToFirst()) {
                genderCounts[0] = cursor.getInt(0);
            }
            cursor.close();

            // Count female patients
            String femaleQuery = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_GENDER + " = 'Female' AND " +
                    DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            cursor = db.rawQuery(femaleQuery, null);
            if (cursor.moveToFirst()) {
                genderCounts[1] = cursor.getInt(0);
            }
            cursor.close();

            // Count other gender patients
            String otherQuery = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_GENDER + " = 'Other' AND " +
                    DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            cursor = db.rawQuery(otherQuery, null);
            if (cursor.moveToFirst()) {
                genderCounts[2] = cursor.getInt(0);
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting gender statistics: " + e.getMessage());
        }

        return genderCounts;
    }

    /**
     * Check if patient has any medical records
     */
    public boolean hasPatientMedicalRecords(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};

        Cursor cursor = db.query(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean hasRecords = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasRecords = cursor.getInt(0) > 0;
            cursor.close();
        }

        return hasRecords;
    }

    /**
     * Check if patient has any active medications
     */
    public boolean hasPatientActiveMedications(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicationEntry.COLUMN_PATIENT_ID + " = ? AND " +
                DatabaseContract.MedicationEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = {String.valueOf(patientId), "1"};

        Cursor cursor = db.query(
                DatabaseContract.MedicationEntry.TABLE_NAME,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean hasMedications = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasMedications = cursor.getInt(0) > 0;
            cursor.close();
        }

        return hasMedications;
    }

    /**
     * Get latest medical record for a patient
     */
    public MedicalRecord getLatestMedicalRecord(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.MedicalRecordEntry.COLUMN_PATIENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};
        String sortOrder = DatabaseContract.MedicalRecordEntry.COLUMN_VISIT_DATE + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.MedicalRecordEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder,
                "1" // Limit to 1 result
        );

        MedicalRecord record = null;
        if (cursor != null && cursor.moveToFirst()) {
            record = cursorToMedicalRecord(cursor);
            cursor.close();
        }

        return record;
    }

    /**
     * Get patient statistics for reporting
     */
    public List<Integer> getPatientStatistics() {
        List<Integer> stats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // Total active patients
            String totalQuery = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            Cursor cursor = db.rawQuery(totalQuery, null);
            if (cursor.moveToFirst()) {
                stats.add(cursor.getInt(0));
            }
            cursor.close();

            // Patients added this month
            String monthQuery = "SELECT COUNT(*) FROM " + DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1" +
                    " AND " + DatabaseContract.PatientEntry.COLUMN_REGISTRATION_DATE + " >= date('now', 'start of month')";
            cursor = db.rawQuery(monthQuery, null);
            if (cursor.moveToFirst()) {
                stats.add(cursor.getInt(0));
            }
            cursor.close();

            // Average age
            String ageQuery = "SELECT AVG(" + DatabaseContract.PatientEntry.COLUMN_AGE + ") FROM " +
                    DatabaseContract.PatientEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.PatientEntry.COLUMN_IS_ACTIVE + " = 1";
            cursor = db.rawQuery(ageQuery, null);
            if (cursor.moveToFirst()) {
                stats.add((int) cursor.getDouble(0));
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting patient statistics: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Close database connections
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}