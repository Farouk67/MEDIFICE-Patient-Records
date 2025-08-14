package com.david.patientrecords.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.david.patientrecords.R;
import com.david.patientrecords.adapters.MedicalRecordsAdapter;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.MedicalRecord;
import com.david.patientrecords.models.Patient;
import com.david.patientrecords.utils.Constants;
import com.david.patientrecords.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicalRecordActivity extends AppCompatActivity implements MedicalRecordsAdapter.OnMedicalRecordClickListener {

    private static final String TAG = "MedicalRecordActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView textPatientName;
    private TextView textPatientInfo;

    // Form Components
    private TextInputLayout layoutSymptoms;
    private TextInputEditText editSymptoms;
    private TextInputLayout layoutDiagnosis;
    private TextInputEditText editDiagnosis;
    private TextInputLayout layoutTreatment;
    private TextInputEditText editTreatment;
    private TextInputLayout layoutDoctorName;
    private TextInputEditText editDoctorName;
    private Spinner spinnerDoctorSpecialty;
    private Spinner spinnerVisitType;
    private TextInputLayout layoutNotes;
    private TextInputEditText editNotes;
    private TextInputLayout layoutVitalSigns;
    private TextInputEditText editVitalSigns;
    private TextView textVisitDate;
    private MaterialButton buttonSelectDate;
    private TextView textFollowUpDate;
    private MaterialButton buttonSelectFollowUpDate;
    private MaterialButton buttonSaveRecord;
    private MaterialButton buttonClearForm;

    // Medical Records List
    private RecyclerView recyclerMedicalRecords;
    private MedicalRecordsAdapter medicalRecordsAdapter;
    private TextView textNoRecords;

    // Data
    private PatientRepository patientRepository;
    private Patient currentPatient;
    private long patientId = -1;
    private String patientName = "";
    private List<MedicalRecord> medicalRecords;
    private String selectedVisitDate;
    private String selectedFollowUpDate;
    private boolean isEditMode = false;
    private MedicalRecord currentRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        // Initialize repository
        patientRepository = PatientRepository.getInstance(this);

        // Get intent data
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        setupRecyclerView();

        // Load data
        if (patientId != -1) {
            loadPatientData();
            loadMedicalRecords();
        } else {
            showError("Invalid patient ID");
            finish();
        }

        // Set default values
        setDefaultValues();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        patientId = intent.getLongExtra(Constants.EXTRA_PATIENT_ID, -1);
        patientName = intent.getStringExtra(Constants.EXTRA_PATIENT_NAME);

        long recordId = intent.getLongExtra(Constants.EXTRA_MEDICAL_RECORD_ID, -1);
        if (recordId != -1) {
            isEditMode = true;
            loadRecordForEdit(recordId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textPatientName = findViewById(R.id.text_patient_name);
        textPatientInfo = findViewById(R.id.text_patient_info);

        // Form components
        layoutSymptoms = findViewById(R.id.layout_symptoms);
        editSymptoms = findViewById(R.id.edit_symptoms);
        layoutDiagnosis = findViewById(R.id.layout_diagnosis);
        editDiagnosis = findViewById(R.id.edit_diagnosis);
        layoutTreatment = findViewById(R.id.layout_treatment);
        editTreatment = findViewById(R.id.edit_treatment);
        layoutDoctorName = findViewById(R.id.layout_doctor_name);
        editDoctorName = findViewById(R.id.edit_doctor_name);
        spinnerDoctorSpecialty = findViewById(R.id.spinner_doctor_specialty);
        spinnerVisitType = findViewById(R.id.spinner_visit_type);
        layoutNotes = findViewById(R.id.layout_notes);
        editNotes = findViewById(R.id.edit_notes);
        layoutVitalSigns = findViewById(R.id.layout_vital_signs);
        editVitalSigns = findViewById(R.id.edit_vital_signs);
        textVisitDate = findViewById(R.id.text_visit_date);
        buttonSelectDate = findViewById(R.id.button_select_date);
        textFollowUpDate = findViewById(R.id.text_follow_up_date);
        buttonSelectFollowUpDate = findViewById(R.id.button_select_follow_up_date);
        buttonSaveRecord = findViewById(R.id.button_save_record);
        buttonClearForm = findViewById(R.id.button_clear_form);

        // Medical records list
        recyclerMedicalRecords = findViewById(R.id.recycler_medical_records);
        textNoRecords = findViewById(R.id.text_no_records);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Medical Record" : "Medical Records");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Doctor Specialty Spinner
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.DOCTOR_SPECIALTIES);
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctorSpecialty.setAdapter(specialtyAdapter);

        // Visit Type Spinner
        ArrayAdapter<String> visitTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.VISIT_TYPES);
        visitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisitType.setAdapter(visitTypeAdapter);
    }

    private void setupClickListeners() {
        // Date selection buttons
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog(true));
        buttonSelectFollowUpDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Action buttons
        buttonSaveRecord.setOnClickListener(v -> validateAndSaveRecord());
        buttonClearForm.setOnClickListener(v -> clearForm());
    }

    private void setupRecyclerView() {
        recyclerMedicalRecords.setLayoutManager(new LinearLayoutManager(this));
        medicalRecords = new ArrayList<>();
        medicalRecordsAdapter = new MedicalRecordsAdapter(this, medicalRecords, false, this);
        recyclerMedicalRecords.setAdapter(medicalRecordsAdapter);
    }

    private void loadPatientData() {
        new Thread(() -> {
            try {
                currentPatient = patientRepository.getPatientById(patientId);

                runOnUiThread(() -> {
                    if (currentPatient != null) {
                        populatePatientInfo();
                    } else {
                        showError("Patient not found");
                        finish();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Error loading patient data: " + e.getMessage());
                    finish();
                });
            }
        }).start();
    }

    private void populatePatientInfo() {
        textPatientName.setText(currentPatient.getPatientName());
        String patientInfo = currentPatient.getAge() + " years • " +
                (currentPatient.getGender() != null ? currentPatient.getGender() : "Unknown") +
                " • " + (currentPatient.getBloodType() != null ? currentPatient.getBloodType() : "Unknown");
        textPatientInfo.setText(patientInfo);
    }

    private void loadMedicalRecords() {
        new Thread(() -> {
            try {
                List<MedicalRecord> records = patientRepository.getMedicalRecordsByPatientId(patientId);

                runOnUiThread(() -> {
                    medicalRecords.clear();
                    medicalRecords.addAll(records);
                    medicalRecordsAdapter.updateRecords(medicalRecords);

                    // Show/hide empty state
                    if (medicalRecords.isEmpty()) {
                        textNoRecords.setVisibility(View.VISIBLE);
                        recyclerMedicalRecords.setVisibility(View.GONE);
                    } else {
                        textNoRecords.setVisibility(View.GONE);
                        recyclerMedicalRecords.setVisibility(View.VISIBLE);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> showError("Error loading medical records"));
            }
        }).start();
    }

    private void loadRecordForEdit(long recordId) {
        new Thread(() -> {
            try {
                currentRecord = patientRepository.getMedicalRecordById(recordId);

                runOnUiThread(() -> {
                    if (currentRecord != null) {
                        populateFormWithRecord();
                    } else {
                        showError("Medical record not found");
                        finish();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Error loading medical record");
                    finish();
                });
            }
        }).start();
    }

    private void populateFormWithRecord() {
        editSymptoms.setText(currentRecord.getSymptoms());
        editDiagnosis.setText(currentRecord.getDiagnosis());
        editTreatment.setText(currentRecord.getTreatment());
        editDoctorName.setText(currentRecord.getDoctorName());
        editNotes.setText(currentRecord.getNotes());
        editVitalSigns.setText(currentRecord.getVitalSigns());

        // Set spinners
        if (currentRecord.getDoctorSpecialty() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDoctorSpecialty.getAdapter();
            int position = adapter.getPosition(currentRecord.getDoctorSpecialty());
            if (position >= 0) spinnerDoctorSpecialty.setSelection(position);
        }

        if (currentRecord.getVisitType() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerVisitType.getAdapter();
            int position = adapter.getPosition(currentRecord.getVisitType());
            if (position >= 0) spinnerVisitType.setSelection(position);
        }

        // Set dates
        selectedVisitDate = currentRecord.getVisitDate();
        selectedFollowUpDate = currentRecord.getFollowUpDate();
        updateDateDisplays();
    }

    private void setDefaultValues() {
        // Set default visit date to today
        selectedVisitDate = DateUtils.getCurrentDate();
        updateDateDisplays();

        // Set default doctor name if available in preferences
        editDoctorName.setText("Dr. ");
    }

    private void showDatePickerDialog(boolean isVisitDate) {
        Calendar calendar = Calendar.getInstance();

        // If editing and has existing date, use it
        String existingDate = isVisitDate ? selectedVisitDate : selectedFollowUpDate;
        if (existingDate != null && !existingDate.isEmpty()) {
            try {
                String[] dateParts = existingDate.split("-");
                calendar.set(Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2]));
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isVisitDate) {
                        selectedVisitDate = selectedDate;
                    } else {
                        selectedFollowUpDate = selectedDate;
                    }
                    updateDateDisplays();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateDisplays() {
        if (selectedVisitDate != null) {
            String displayDate = DateUtils.formatDateForDisplay(selectedVisitDate);
            textVisitDate.setText(displayDate);
        }

        if (selectedFollowUpDate != null && !selectedFollowUpDate.isEmpty()) {
            String displayDate = DateUtils.formatDateForDisplay(selectedFollowUpDate);
            textFollowUpDate.setText(displayDate);
        } else {
            textFollowUpDate.setText("No follow-up scheduled");
        }
    }

    private void validateAndSaveRecord() {
        // Clear previous errors
        clearErrors();

        boolean isValid = true;

        // Validate required fields
        String symptoms = editSymptoms.getText().toString().trim();
        if (TextUtils.isEmpty(symptoms)) {
            layoutSymptoms.setError("Symptoms are required");
            isValid = false;
        }

        String diagnosis = editDiagnosis.getText().toString().trim();
        if (TextUtils.isEmpty(diagnosis)) {
            layoutDiagnosis.setError("Diagnosis is required");
            isValid = false;
        }

        String treatment = editTreatment.getText().toString().trim();
        if (TextUtils.isEmpty(treatment)) {
            layoutTreatment.setError("Treatment is required");
            isValid = false;
        }

        String doctorName = editDoctorName.getText().toString().trim();
        if (TextUtils.isEmpty(doctorName)) {
            layoutDoctorName.setError("Doctor name is required");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, save the record
        saveRecord();
    }

    private void clearErrors() {
        layoutSymptoms.setError(null);
        layoutDiagnosis.setError(null);
        layoutTreatment.setError(null);
        layoutDoctorName.setError(null);
        layoutNotes.setError(null);
        layoutVitalSigns.setError(null);
    }

    private void saveRecord() {
        // Show loading state
        buttonSaveRecord.setEnabled(false);
        buttonSaveRecord.setText(isEditMode ? "Updating..." : "Saving...");

        new Thread(() -> {
            try {
                MedicalRecord record = isEditMode ? currentRecord : new MedicalRecord();

                // Set record data
                record.setPatientId(patientId);
                record.setSymptoms(editSymptoms.getText().toString().trim());
                record.setDiagnosis(editDiagnosis.getText().toString().trim());
                record.setTreatment(editTreatment.getText().toString().trim());
                record.setDoctorName(editDoctorName.getText().toString().trim());
                record.setDoctorSpecialty(spinnerDoctorSpecialty.getSelectedItem().toString());
                record.setVisitType(spinnerVisitType.getSelectedItem().toString());
                record.setNotes(editNotes.getText().toString().trim());
                record.setVitalSigns(editVitalSigns.getText().toString().trim());
                record.setVisitDate(selectedVisitDate);
                record.setFollowUpDate(selectedFollowUpDate);

                long result;
                if (isEditMode) {
                    result = patientRepository.updateMedicalRecord(record);
                } else {
                    result = patientRepository.insertMedicalRecord(record);
                }

                runOnUiThread(() -> {
                    buttonSaveRecord.setEnabled(true);
                    buttonSaveRecord.setText(isEditMode ? "Update Record" : "Save Record");

                    if (result > 0) {
                        String message = isEditMode ? "Medical record updated successfully" : "Medical record saved successfully";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (!isEditMode) {
                            clearForm();
                        }
                        loadMedicalRecords(); // Refresh the list

                        // Return result to calling activity
                        setResult(RESULT_OK);

                    } else {
                        Toast.makeText(this, "Error saving medical record", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    buttonSaveRecord.setEnabled(true);
                    buttonSaveRecord.setText(isEditMode ? "Update Record" : "Save Record");
                    Toast.makeText(this, "Error saving medical record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void clearForm() {
        editSymptoms.setText("");
        editDiagnosis.setText("");
        editTreatment.setText("");
        editDoctorName.setText("Dr. ");
        editNotes.setText("");
        editVitalSigns.setText("");

        spinnerDoctorSpecialty.setSelection(0);
        spinnerVisitType.setSelection(0);

        selectedVisitDate = DateUtils.getCurrentDate();
        selectedFollowUpDate = null;
        updateDateDisplays();

        clearErrors();

        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // MedicalRecordsAdapter.OnMedicalRecordClickListener implementation
    @Override
    public void onMedicalRecordClick(MedicalRecord record) {
        // Open record for editing
        Intent intent = new Intent(this, MedicalRecordActivity.class);
        intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
        intent.putExtra(Constants.EXTRA_PATIENT_NAME, patientName);
        intent.putExtra(Constants.EXTRA_MEDICAL_RECORD_ID, record.getId());
        startActivityForResult(intent, Constants.REQUEST_EDIT_MEDICAL_RECORD);
    }

    @Override
    public void onMedicalRecordEdit(MedicalRecord record) {
        // Same as click - open for editing
        onMedicalRecordClick(record);
    }

    @Override
    public void onMedicalRecordDelete(MedicalRecord record) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Medical Record")
                .setMessage("Are you sure you want to delete this medical record?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMedicalRecord(record))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMedicalRecord(MedicalRecord record) {
        new Thread(() -> {
            try {
                int result = patientRepository.deleteMedicalRecord(record.getId());

                runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(this, "Medical record deleted", Toast.LENGTH_SHORT).show();
                        loadMedicalRecords(); // Refresh the list
                    } else {
                        showError("Failed to delete medical record");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> showError("Error deleting medical record"));
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.medical_record_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_save) {
            validateAndSaveRecord();
            return true;
        } else if (itemId == R.id.action_clear) {
            clearForm();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_EDIT_MEDICAL_RECORD:
                    loadMedicalRecords(); // Refresh the list
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (patientRepository != null) {
            patientRepository.close();
        }
    }
}