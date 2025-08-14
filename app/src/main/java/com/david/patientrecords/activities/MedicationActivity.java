package com.david.patientrecords.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.david.patientrecords.R;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.Medication;
import com.david.patientrecords.utils.DateUtils;
import com.david.patientrecords.utils.ToastHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class MedicationActivity extends AppCompatActivity {

    private static final String TAG = "MedicationActivity";

    // UI Components
    private TextInputEditText editMedicationName, editDosage, editInstructions;
    private TextInputEditText editPharmacy, editDoctorName, editNotes;
    private EditText editStartDate, editEndDate;
    private Spinner spinnerFrequency, spinnerMedicationType;
    private Button buttonSave, buttonClear, buttonChangeStartDate, buttonChangeEndDate;
    private TextView textPatientInfo;

    // Data
    private PatientRepository patientRepository;
    private long patientId;
    private String patientName;
    private boolean isEditMode = false;
    private long medicationId = -1;

    // Date handling
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        // Initialize repository
        patientRepository = PatientRepository.getInstance(this);

        // Get patient information from intent
        getPatientInfoFromIntent();

        // Initialize UI
        initViews();
        setupToolbar();
        setupSpinners();
        setupDatePickers();
        setupClickListeners();

        // Set patient information
        updatePatientInfoDisplay();

        // Check if this is edit mode
        checkEditMode();
    }

    private void getPatientInfoFromIntent() {
        Intent intent = getIntent();
        patientId = intent.getLongExtra("patient_id", -1);
        patientName = intent.getStringExtra("patient_name");
        isEditMode = intent.getBooleanExtra("is_edit_mode", false);
        medicationId = intent.getLongExtra("medication_id", -1);

        Log.d(TAG, "Patient ID: " + patientId + ", Name: " + patientName +
                ", Edit Mode: " + isEditMode + ", Medication ID: " + medicationId);

        if (patientId == -1) {
            Log.e(TAG, "No patient ID provided!");
            ToastHelper.showError(this, "Error: No patient selected");
            finish();
        }
    }

    private void initViews() {
        // Patient info
        textPatientInfo = findViewById(R.id.text_patient_info);

        // Medication details
        editMedicationName = findViewById(R.id.edit_medication_name);
        editDosage = findViewById(R.id.edit_dosage);
        editInstructions = findViewById(R.id.edit_instructions);
        editPharmacy = findViewById(R.id.edit_pharmacy);
        editDoctorName = findViewById(R.id.edit_doctor_name);
        editNotes = findViewById(R.id.edit_notes);

        // Date fields
        editStartDate = findViewById(R.id.edit_start_date);
        editEndDate = findViewById(R.id.edit_end_date);
        buttonChangeStartDate = findViewById(R.id.button_change_start_date);
        buttonChangeEndDate = findViewById(R.id.button_change_end_date);

        // Spinners
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        spinnerMedicationType = findViewById(R.id.spinner_medication_type);

        // Buttons
        buttonSave = findViewById(R.id.button_save_medication);
        buttonClear = findViewById(R.id.button_clear_medication);

        // Set default dates
        updateDateDisplays();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Medication" : "Add Medication");
        }
    }

    private void setupSpinners() {
        // Frequency spinner
        String[] frequencies = {
                "Once daily", "Twice daily", "Three times daily", "Four times daily",
                "Every 2 hours", "Every 4 hours", "Every 6 hours", "Every 8 hours",
                "Every 12 hours", "As needed", "Before meals", "After meals",
                "At bedtime", "Weekly", "Monthly", "Other"
        };

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);

        // Medication type spinner
        String[] medicationTypes = {
                "Tablet", "Capsule", "Liquid", "Injection", "Cream/Ointment",
                "Inhaler", "Drops", "Patch", "Spray", "Suppository", "Other"
        };

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, medicationTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedicationType.setAdapter(typeAdapter);
    }

    private void setupDatePickers() {
        // Set default dates
        Calendar today = Calendar.getInstance();
        startDateCalendar.setTime(today.getTime());

        // End date default to 30 days from now
        endDateCalendar.setTime(today.getTime());
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 30);

        updateDateDisplays();
    }

    private void setupClickListeners() {
        buttonChangeStartDate.setOnClickListener(v -> showStartDatePicker());
        buttonChangeEndDate.setOnClickListener(v -> showEndDatePicker());
        buttonSave.setOnClickListener(v -> saveMedication());
        buttonClear.setOnClickListener(v -> showClearConfirmation());
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(year, month, dayOfMonth);
                    updateDateDisplays();
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    endDateCalendar.set(year, month, dayOfMonth);
                    updateDateDisplays();
                },
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // End date should be after start date
        datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateDateDisplays() {
        editStartDate.setText(DateUtils.formatDateForDisplay(
                DateUtils.formatDateForDatabase(startDateCalendar.getTime())));
        editEndDate.setText(DateUtils.formatDateForDisplay(
                DateUtils.formatDateForDatabase(endDateCalendar.getTime())));
    }

    private void updatePatientInfoDisplay() {
        if (patientName != null && !patientName.isEmpty()) {
            textPatientInfo.setText("Medication for: " + patientName);
        } else {
            textPatientInfo.setText("Patient ID: " + patientId);
        }
    }

    private void checkEditMode() {
        if (isEditMode && medicationId != -1) {
            // Load existing medication data
            // This would typically load from database
            Log.d(TAG, "Edit mode - loading medication ID: " + medicationId);
            // TODO: Load medication data from database
        }
    }

    private void saveMedication() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create medication object
            Medication medication = new Medication();
            medication.setPatientId(patientId);
            medication.setMedicationName(editMedicationName.getText().toString().trim());
            medication.setDosage(editDosage.getText().toString().trim());
            medication.setFrequency(spinnerFrequency.getSelectedItem().toString());
            medication.setMedicationType(spinnerMedicationType.getSelectedItem().toString());
            medication.setInstructions(editInstructions.getText().toString().trim());
            medication.setPharmacy(editPharmacy.getText().toString().trim());
            medication.setDoctorName(editDoctorName.getText().toString().trim());
            medication.setNotes(editNotes.getText().toString().trim());
            medication.setStartDate(DateUtils.formatDateForDatabase(startDateCalendar.getTime()));
            medication.setEndDate(DateUtils.formatDateForDatabase(endDateCalendar.getTime()));
            medication.setActive(true);

            // Save to database
            long result;
            if (isEditMode && medicationId != -1) {
                medication.setId(medicationId);
                result = patientRepository.updateMedication(medication);
                Log.d(TAG, "Updated medication, result: " + result);
            } else {
                result = patientRepository.insertMedication(medication);
                Log.d(TAG, "Inserted medication, result: " + result);
            }

            if (result > 0) {
                String message = isEditMode ? "Medication updated successfully!" : "Medication added successfully!";
                ToastHelper.showSuccess(this, message);

                // Return result to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("medication_saved", true);
                resultIntent.putExtra("patient_id", patientId);
                setResult(RESULT_OK, resultIntent);

                finish();
            } else {
                ToastHelper.showError(this, "Failed to save medication");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving medication", e);
            ToastHelper.showError(this, "Error saving medication: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Medication name is required
        if (editMedicationName.getText().toString().trim().isEmpty()) {
            editMedicationName.setError("Medication name is required");
            editMedicationName.requestFocus();
            return false;
        }

        // Dosage is required
        if (editDosage.getText().toString().trim().isEmpty()) {
            editDosage.setError("Dosage is required");
            editDosage.requestFocus();
            return false;
        }

        // Instructions are required
        if (editInstructions.getText().toString().trim().isEmpty()) {
            editInstructions.setError("Instructions are required");
            editInstructions.requestFocus();
            return false;
        }

        // Validate dates
        if (endDateCalendar.before(startDateCalendar)) {
            ToastHelper.showError(this, "End date must be after start date");
            return false;
        }

        return true;
    }

    private void showClearConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Form")
                .setMessage("Are you sure you want to clear all fields? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> clearForm())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearForm() {
        editMedicationName.setText("");
        editDosage.setText("");
        editInstructions.setText("");
        editPharmacy.setText("");
        editDoctorName.setText("");
        editNotes.setText("");

        // Reset spinners to first item
        spinnerFrequency.setSelection(0);
        spinnerMedicationType.setSelection(0);

        // Reset dates
        setupDatePickers();

        ToastHelper.showInfo(this, "Form cleared");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.medication_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.menu_save) {
            saveMedication();
            return true;
        } else if (itemId == R.id.menu_clear) {
            showClearConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Leave", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        // Check if any fields have been modified
        return !editMedicationName.getText().toString().trim().isEmpty() ||
                !editDosage.getText().toString().trim().isEmpty() ||
                !editInstructions.getText().toString().trim().isEmpty() ||
                !editPharmacy.getText().toString().trim().isEmpty() ||
                !editDoctorName.getText().toString().trim().isEmpty() ||
                !editNotes.getText().toString().trim().isEmpty();
    }
}