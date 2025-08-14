package com.david.patientrecords.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;

import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.david.patientrecords.utils.PdfExportUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.david.patientrecords.R;
import com.david.patientrecords.adapters.MedicalRecordsAdapter;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.MedicalRecord;
import com.david.patientrecords.models.Patient;
import com.david.patientrecords.utils.Constants;
import com.david.patientrecords.utils.DateUtils;
import com.david.patientrecords.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.david.patientrecords.adapters.MedicationsAdapter;
import com.david.patientrecords.models.Medication;
import com.david.patientrecords.utils.ToastHelper;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailActivity extends AppCompatActivity implements MedicalRecordsAdapter.OnMedicalRecordClickListener {

    private static final String TAG = "PatientDetailActivity";
    private static final int REQUEST_CODE_ADD_MEDICATION = 2001;
    private static final int REQUEST_CODE_EDIT_MEDICATION = 2002;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private Toolbar toolbar;
    private CircleImageView imagePatientProfile;
    private TextView textPatientName;
    private TextView textPatientDetails;
    private TextView textBloodType;
    private TextView textPhone;
    private TextView textAddress;

    // Medications UI Components
    private RecyclerView recyclerMedications;
    private MedicationsAdapter medicationsAdapter;
    private List<Medication> medicationsList;
    private Button buttonAddMedication;
    private TextView textNoMedications;
    private String patientName;

    private TextView textRegistrationDate;
    private TextView textEmergencyContact;
    private TextView textMedicalConditions;
    private TextView textAllergies;

    // Tab Layout
    private TabLayout tabLayout;
    private LinearLayout layoutBasicInfo;
    private LinearLayout layoutMedicalRecords;
    private LinearLayout layoutMedications;

    // Medical Records
    private RecyclerView recyclerMedicalRecords;
    private MedicalRecordsAdapter medicalRecordsAdapter;
    private TextView textNoMedicalRecords;

    // Action Buttons
    private MaterialButton buttonEditPatient;
    private MaterialButton buttonCallPatient;
    private MaterialButton buttonAddRecord;

    // Data
    private PatientRepository patientRepository;
    private Patient currentPatient;
    private long patientId = -1;
    private List<MedicalRecord> medicalRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Initialize repository
        patientRepository = PatientRepository.getInstance(this);

        // Get intent data
        getIntentData();

        // Initialize views
        initViews();
        setupToolbar();
        setupTabs();
        setupClickListeners();

        // Load patient data
        if (patientId != -1) {
            loadPatientData();
        } else {
            showError("Invalid patient ID");
            finish();
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        patientId = intent.getLongExtra(Constants.EXTRA_PATIENT_ID, -1);
        patientName = intent.getStringExtra(Constants.EXTRA_PATIENT_NAME);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imagePatientProfile = findViewById(R.id.image_patient_profile);
        textPatientName = findViewById(R.id.text_patient_name);
        textPatientDetails = findViewById(R.id.text_patient_details);
        textBloodType = findViewById(R.id.text_blood_type);
        textPhone = findViewById(R.id.text_phone);
        textAddress = findViewById(R.id.text_address);
        textRegistrationDate = findViewById(R.id.text_registration_date);
        textEmergencyContact = findViewById(R.id.text_emergency_contact);
        textMedicalConditions = findViewById(R.id.text_medical_conditions);
        textAllergies = findViewById(R.id.text_allergies);

        tabLayout = findViewById(R.id.tab_layout);
        layoutBasicInfo = findViewById(R.id.layout_basic_info);
        layoutMedicalRecords = findViewById(R.id.layout_medical_records);
        layoutMedications = findViewById(R.id.layout_medications);

        recyclerMedicalRecords = findViewById(R.id.recycler_medical_records);
        textNoMedicalRecords = findViewById(R.id.text_no_medical_records);

        buttonEditPatient = findViewById(R.id.button_edit_patient);
        buttonCallPatient = findViewById(R.id.button_call_patient);
        buttonAddRecord = findViewById(R.id.button_add_record);

        // Initialize medications UI components
        recyclerMedications = findViewById(R.id.recycler_medications);
        buttonAddMedication = findViewById(R.id.button_add_medication);
        textNoMedications = findViewById(R.id.text_no_medications);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Patient Details");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Basic Info").setIcon(R.drawable.ic_person));
        tabLayout.addTab(tabLayout.newTab().setText("Medical Records").setIcon(R.drawable.ic_medical_record));
        tabLayout.addTab(tabLayout.newTab().setText("Medications").setIcon(R.drawable.ic_medication));

        // Set default tab
        showBasicInfo();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showBasicInfo();
                        break;
                    case 1:
                        showMedicalRecords();
                        break;
                    case 2:
                        showMedications();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        buttonEditPatient.setOnClickListener(v -> editPatient());
        buttonCallPatient.setOnClickListener(v -> callPatient());
        buttonAddRecord.setOnClickListener(v -> addMedicalRecord());

        // Setup medications button click listener
        if (buttonAddMedication != null) {
            buttonAddMedication.setOnClickListener(v -> {
                Log.d(TAG, "Add Medication button clicked");
                openMedicationActivity(-1, false);
            });
        }
    }

    private void setupMedicationsTab() {
        // Initialize medications list and adapter
        medicationsList = new ArrayList<>();
        medicationsAdapter = new MedicationsAdapter(this, medicationsList, new MedicationsAdapter.OnMedicationClickListener() {
            @Override
            public void onMedicationClick(Medication medication) {
                showMedicationDetails(medication);
            }

            @Override
            public void onMedicationEdit(Medication medication) {
                openMedicationActivity(medication.getId(), true);
            }

            @Override
            public void onMedicationDelete(Medication medication) {
                showDeleteMedicationConfirmation(medication);
            }
        });

        if (recyclerMedications != null) {
            recyclerMedications.setLayoutManager(new LinearLayoutManager(this));
            recyclerMedications.setAdapter(medicationsAdapter);
        }

        // Load patient medications
        loadPatientMedications();
    }

    private void openMedicationActivity(long medicationId, boolean isEditMode) {
        try {
            Intent intent = new Intent(this, MedicationActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("patient_name", getPatientName());

            if (isEditMode && medicationId != -1) {
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("medication_id", medicationId);
                startActivityForResult(intent, REQUEST_CODE_EDIT_MEDICATION);
            } else {
                startActivityForResult(intent, REQUEST_CODE_ADD_MEDICATION);
            }

            Log.d(TAG, "Starting MedicationActivity with patient_id: " + patientId);

        } catch (Exception e) {
            Log.e(TAG, "Error opening MedicationActivity", e);
            Toast.makeText(this, "Error opening medication form", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportPatientToPdf() {
        // Check for storage permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }

        // Show loading dialog
        androidx.appcompat.app.AlertDialog loadingDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Generating PDF")
                .setMessage("Please wait while we generate the patient report...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Generate PDF in background thread
        new Thread(() -> {
            try {
                // Get all patient data
                List<MedicalRecord> records = patientRepository.getMedicalRecordsByPatientId(patientId);
                List<Medication> medications = patientRepository.getMedicationsByPatientId(patientId);

                // Export to PDF
                PdfExportUtils.ExportResult result = PdfExportUtils.exportPatientToPdf(
                        this, currentPatient, records, medications);

                runOnUiThread(() -> {
                    loadingDialog.dismiss();

                    if (result.success) {
                        // Show success dialog
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Export Successful")
                                .setMessage("Patient report saved to Downloads folder:\n\n" +
                                        new File(result.filePath).getName())
                                .setPositiveButton("OK", null)
                                .setNeutralButton("Share", (dialog, which) -> shareFile(result.filePath))
                                .show();
                    } else {
                        // Show error dialog
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Export Failed")
                                .setMessage("Failed to generate PDF:\n" + result.errorMessage)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    ToastHelper.showError(this, "Error generating PDF: " + e.getMessage());
                });
            }
        }).start();
    }

    // Add this method to share the generated PDF
    private void shareFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM,
                        androidx.core.content.FileProvider.getUriForFile(
                                this, getPackageName() + ".fileprovider", file));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                        "Patient Report - " + currentPatient.getPatientName());
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Share Patient Report"));
            }
        } catch (Exception e) {
            ToastHelper.showError(this, "Error sharing file: " + e.getMessage());
        }
    }

    // Add this to handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportPatientToPdf(); // Try export again
            } else {
                ToastHelper.showError(this, "Storage permission required to export PDF");
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_export_pdf) {
            exportPatientToPdf(); // NEW: Export to PDF
            return true;
        } else if (itemId == R.id.action_edit) {
            editPatient();
            return true;
        } else if (itemId == R.id.action_share) {
            sharePatientInfo();
            return true;
        } else if (itemId == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getPatientName() {
        if (patientName != null && !patientName.isEmpty()) {
            return patientName;
        }

        if (currentPatient != null) {
            return currentPatient.getPatientName();
        }

        TextView nameView = findViewById(R.id.text_patient_name);
        if (nameView != null) {
            return nameView.getText().toString();
        }

        return "Patient #" + patientId;
    }

    private void loadPatientData() {
        new Thread(() -> {
            try {
                currentPatient = patientRepository.getPatientById(patientId);
                medicalRecords = patientRepository.getMedicalRecordsByPatientId(patientId);

                runOnUiThread(() -> {
                    if (currentPatient != null) {
                        populatePatientData();
                        setupMedicalRecordsRecyclerView();
                        setupMedicationsTab(); // Setup medications when patient data loads
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

    private void populatePatientData() {
        // Basic information
        textPatientName.setText(currentPatient.getPatientName());
        patientName = currentPatient.getPatientName(); // Set the patient name

        textPatientDetails.setText(currentPatient.getAge() + " years • " +
                (currentPatient.getGender() != null ? currentPatient.getGender() : "Unknown"));
        textBloodType.setText(currentPatient.getBloodType() != null ? currentPatient.getBloodType() : "Unknown");
        textPhone.setText(currentPatient.getPhone() != null ? currentPatient.getPhone() : "No phone");
        textAddress.setText(currentPatient.getAddress() != null ? currentPatient.getAddress() : "No address");
        textRegistrationDate.setText("Registered: " +
                DateUtils.formatDateForDisplay(currentPatient.getRegistrationDate()));

        // Emergency contact
        if (currentPatient.getEmergencyContact() != null && !currentPatient.getEmergencyContact().isEmpty()) {
            String emergencyInfo = currentPatient.getEmergencyContact();
            if (currentPatient.getEmergencyPhone() != null && !currentPatient.getEmergencyPhone().isEmpty()) {
                emergencyInfo += " • " + currentPatient.getEmergencyPhone();
            }
            textEmergencyContact.setText(emergencyInfo);
        } else {
            textEmergencyContact.setText("No emergency contact");
        }

        // Medical conditions
        if (currentPatient.getMedicalConditions() != null && !currentPatient.getMedicalConditions().isEmpty()) {
            textMedicalConditions.setText(currentPatient.getMedicalConditions());
            textMedicalConditions.setVisibility(View.VISIBLE);
        } else {
            textMedicalConditions.setText("No medical conditions recorded");
            textMedicalConditions.setVisibility(View.VISIBLE);
        }

        // Allergies
        if (currentPatient.getAllergies() != null && !currentPatient.getAllergies().isEmpty()) {
            textAllergies.setText(currentPatient.getAllergies());
            textAllergies.setVisibility(View.VISIBLE);
        } else {
            textAllergies.setText("No known allergies");
            textAllergies.setVisibility(View.VISIBLE);
        }

        // Profile image
        ImageUtils.setPatientImage(this, imagePatientProfile,
                currentPatient.getProfileImage(),
                currentPatient.getPatientName());

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentPatient.getPatientName());
        }
    }

    private void setupMedicalRecordsRecyclerView() {
        recyclerMedicalRecords.setLayoutManager(new LinearLayoutManager(this));
        medicalRecordsAdapter = new MedicalRecordsAdapter(this, medicalRecords, false, this);
        recyclerMedicalRecords.setAdapter(medicalRecordsAdapter);

        // Show/hide empty state
        if (medicalRecords.isEmpty()) {
            textNoMedicalRecords.setVisibility(View.VISIBLE);
            recyclerMedicalRecords.setVisibility(View.GONE);
        } else {
            textNoMedicalRecords.setVisibility(View.GONE);
            recyclerMedicalRecords.setVisibility(View.VISIBLE);
        }
    }

    private void showBasicInfo() {
        layoutBasicInfo.setVisibility(View.VISIBLE);
        layoutMedicalRecords.setVisibility(View.GONE);
        layoutMedications.setVisibility(View.GONE);
    }

    private void showMedicalRecords() {
        layoutBasicInfo.setVisibility(View.GONE);
        layoutMedicalRecords.setVisibility(View.VISIBLE);
        layoutMedications.setVisibility(View.GONE);
    }

    private void showMedications() {
        layoutBasicInfo.setVisibility(View.GONE);
        layoutMedicalRecords.setVisibility(View.GONE);
        layoutMedications.setVisibility(View.VISIBLE);

        // Load medications when tab is selected
        loadPatientMedications();
    }

    private void loadPatientMedications() {
        new Thread(() -> {
            try {
                List<Medication> medications = patientRepository.getMedicationsByPatientId(patientId);

                runOnUiThread(() -> {
                    medicationsList.clear();
                    medicationsList.addAll(medications);

                    if (medicationsAdapter != null) {
                        medicationsAdapter.updateMedications(medicationsList);
                    }

                    // Show/hide empty state
                    if (medications.isEmpty()) {
                        if (textNoMedications != null) {
                            textNoMedications.setVisibility(View.VISIBLE);
                        }
                        if (recyclerMedications != null) {
                            recyclerMedications.setVisibility(View.GONE);
                        }
                    } else {
                        if (textNoMedications != null) {
                            textNoMedications.setVisibility(View.GONE);
                        }
                        if (recyclerMedications != null) {
                            recyclerMedications.setVisibility(View.VISIBLE);
                        }
                    }

                    Log.d(TAG, "Loaded " + medications.size() + " medications for patient " + patientId);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading medications", e);
                runOnUiThread(() -> {
                    ToastHelper.showError(this, "Error loading medications");
                });
            }
        }).start();
    }

    private void showMedicationDetails(Medication medication) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(medication.getMedicationName());

        String details = "Dosage: " + medication.getDosage() + "\n" +
                "Frequency: " + medication.getFrequency() + "\n" +
                "Type: " + medication.getMedicationType() + "\n" +
                "Instructions: " + medication.getInstructions() + "\n" +
                "Start Date: " + DateUtils.formatDateForDisplay(medication.getStartDate()) + "\n" +
                "End Date: " + DateUtils.formatDateForDisplay(medication.getEndDate());

        if (medication.getDoctorName() != null && !medication.getDoctorName().isEmpty()) {
            details += "\nPrescribed by: Dr. " + medication.getDoctorName();
        }

        builder.setMessage(details);
        builder.setPositiveButton("Edit", (dialog, which) -> {
            openMedicationActivity(medication.getId(), true);
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showDeleteMedicationConfirmation(Medication medication) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete " + medication.getMedicationName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMedication(medication))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMedication(Medication medication) {
        new Thread(() -> {
            try {
                int result = patientRepository.deactivateMedication(medication.getId());

                runOnUiThread(() -> {
                    if (result > 0) {
                        ToastHelper.showSuccess(this, "Medication deleted successfully");
                        loadPatientMedications(); // Refresh the list
                    } else {
                        ToastHelper.showError(this, "Failed to delete medication");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error deleting medication", e);
                runOnUiThread(() -> {
                    ToastHelper.showError(this, "Error deleting medication");
                });
            }
        }).start();
    }

    private void editPatient() {
        Intent intent = new Intent(this, AddEditPatientActivity.class);
        intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
        intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
        startActivityForResult(intent, Constants.REQUEST_EDIT_PATIENT);
    }

    private void callPatient() {
        if (currentPatient != null && currentPatient.getPhone() != null && !currentPatient.getPhone().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + currentPatient.getPhone()));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMedicalRecord() {
        try {
            Intent intent = new Intent(this, MedicalRecordActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
            intent.putExtra(Constants.EXTRA_PATIENT_NAME, currentPatient.getPatientName());
            startActivityForResult(intent, Constants.REQUEST_ADD_MEDICAL_RECORD);
        } catch (Exception e) {
            Toast.makeText(this, "Medical records feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // MedicalRecordsAdapter.OnMedicalRecordClickListener implementation
    @Override
    public void onMedicalRecordClick(MedicalRecord record) {
        // Open medical record details
        Toast.makeText(this, "Medical record details coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMedicalRecordEdit(MedicalRecord record) {
        // Edit medical record
        Toast.makeText(this, "Edit medical record coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMedicalRecordDelete(MedicalRecord record) {
        // Delete medical record with confirmation
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
                        // Refresh medical records
                        loadPatientData();
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
        getMenuInflater().inflate(R.menu.patient_detail_menu, menu);
        return true;
    }

    private void sharePatientInfo() {
        if (currentPatient == null) return;

        String shareText = "Patient Information:\n\n" +
                "Name: " + currentPatient.getPatientName() + "\n" +
                "Age: " + currentPatient.getAge() + " years\n" +
                "Gender: " + (currentPatient.getGender() != null ? currentPatient.getGender() : "Not specified") + "\n" +
                "Blood Type: " + (currentPatient.getBloodType() != null ? currentPatient.getBloodType() : "Unknown") + "\n" +
                "Phone: " + (currentPatient.getPhone() != null ? currentPatient.getPhone() : "No phone") + "\n" +
                "Registration: " + DateUtils.formatDateForDisplay(currentPatient.getRegistrationDate());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Patient Information - " + currentPatient.getPatientName());

        startActivity(Intent.createChooser(shareIntent, "Share Patient Information"));
    }

    private void showDeleteConfirmation() {
        if (currentPatient == null) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + currentPatient.getPatientName() + "?\n\nThis will also delete all associated medical records and medications.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        new Thread(() -> {
            try {
                int result = patientRepository.deletePatient(patientId);

                runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(this, Constants.SUCCESS_PATIENT_DELETED, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError("Failed to delete patient");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> showError("Error deleting patient"));
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_MEDICATION:
                case REQUEST_CODE_EDIT_MEDICATION:
                    // Refresh medications list
                    loadPatientMedications();
                    ToastHelper.showSuccess(this, "Medications updated");
                    break;
                case Constants.REQUEST_EDIT_PATIENT:
                    Toast.makeText(this, Constants.SUCCESS_PATIENT_UPDATED, Toast.LENGTH_SHORT).show();
                    loadPatientData(); // Refresh patient data
                    break;
                case Constants.REQUEST_ADD_MEDICAL_RECORD:
                    Toast.makeText(this, "Medical record added successfully", Toast.LENGTH_SHORT).show();
                    loadPatientData(); // Refresh medical records
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