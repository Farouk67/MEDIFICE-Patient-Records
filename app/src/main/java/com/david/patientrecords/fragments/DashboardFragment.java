package com.david.patientrecords.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.david.patientrecords.R;
import com.david.patientrecords.database.PatientRepository;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // UI Components
    private TextView welcomeText;
    private TextView dateText;
    private TextView textTotalPatients;
    private TextView textMedicalRecords;
    private TextView textMedications;
    private TextView textFollowUps;
    private TextView textNoRecentRecords;

    // Cards
    private CardView cardTotalPatients;
    private CardView cardMedicalRecords;
    private CardView cardMedications;
    private CardView cardFollowUps;
    private CardView cardAddPatient;
    private CardView cardViewPatients;

    // Data
    private PatientRepository patientRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize repository
        if (getContext() != null) {
            patientRepository = PatientRepository.getInstance(getContext());
        }

        // Initialize views
        initViews(view);
        setupClickListeners();
        loadDashboardData();

        return view;
    }

    private void initViews(View view) {
        // Header
        welcomeText = view.findViewById(R.id.text_welcome);
        dateText = view.findViewById(R.id.text_date);

        // Statistics TextViews
        textTotalPatients = view.findViewById(R.id.text_total_patients);
        textMedicalRecords = view.findViewById(R.id.text_medical_records);
        textMedications = view.findViewById(R.id.text_medications);
        textFollowUps = view.findViewById(R.id.text_follow_ups);

        // Cards
        cardTotalPatients = view.findViewById(R.id.card_total_patients);
        cardMedicalRecords = view.findViewById(R.id.card_medical_records);
        cardMedications = view.findViewById(R.id.card_medications);
        cardAddPatient = view.findViewById(R.id.card_add_patient);
        cardViewPatients = view.findViewById(R.id.card_view_patients);

        // Recent activities
        textNoRecentRecords = view.findViewById(R.id.text_no_recent_records);
    }

    private void setupClickListeners() {
        // Statistics cards - navigate to relevant sections
        if (cardTotalPatients != null) {
            cardTotalPatients.setOnClickListener(v -> navigateToPatients());
        }

        if (cardMedicalRecords != null) {
            cardMedicalRecords.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Medical Records feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardMedications != null) {
            cardMedications.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Medications feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardFollowUps != null) {
            cardFollowUps.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Follow-ups feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Quick action cards
        if (cardAddPatient != null) {
            cardAddPatient.setOnClickListener(v -> openAddPatientActivity());
        }

        if (cardViewPatients != null) {
            cardViewPatients.setOnClickListener(v -> navigateToPatients());
        }
    }

    private void loadDashboardData() {
        // Load dashboard statistics
        if (patientRepository != null) {
            try {
                // Get patient count
                int patientCount = patientRepository.getAllPatients().size();
                if (textTotalPatients != null) {
                    textTotalPatients.setText(String.valueOf(patientCount));
                }

                // Set placeholder values for other statistics
                if (textMedicalRecords != null) {
                    textMedicalRecords.setText("0");
                }
                if (textMedications != null) {
                    textMedications.setText("0");
                }
                if (textFollowUps != null) {
                    textFollowUps.setText("0");
                }

                // Update recent activities message
                if (textNoRecentRecords != null) {
                    if (patientCount == 0) {
                        textNoRecentRecords.setText("No patients found.\nAdd your first patient to get started!");
                    } else {
                        textNoRecentRecords.setText("Recent activities will appear here.\nStart by adding medical records for your patients.");
                    }
                }

            } catch (Exception e) {
                // Handle any database errors gracefully
                if (textTotalPatients != null) {
                    textTotalPatients.setText("0");
                }
                if (textMedicalRecords != null) {
                    textMedicalRecords.setText("0");
                }
                if (textMedications != null) {
                    textMedications.setText("0");
                }
                if (textFollowUps != null) {
                    textFollowUps.setText("0");
                }
            }
        }
    }

    private void openAddPatientActivity() {
        try {
            // Try to open AddEditPatientActivity
            Intent intent = new Intent(getContext(), Class.forName("com.david.patientrecords.activities.AddEditPatientActivity"));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            // Fallback if activity doesn't exist
            Toast.makeText(getContext(), "Add Patient feature coming soon!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open Add Patient form", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPatients() {
        try {
            // Try to navigate to PatientsFragment via MainActivity
            if (getActivity() != null) {
                // Use reflection to avoid circular import
                java.lang.reflect.Method navigateMethod = getActivity().getClass().getMethod("navigateToFragment", String.class);
                navigateMethod.invoke(getActivity(), "patients");
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Patients section coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardData();
    }

    /**
     * Public method to refresh dashboard data
     * Called by MainActivity when needed
     */
    public void refreshData() {
        loadDashboardData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        welcomeText = null;
        dateText = null;
        textTotalPatients = null;
        textMedicalRecords = null;
        textMedications = null;
        textFollowUps = null;
        textNoRecentRecords = null;
        cardTotalPatients = null;
        cardMedicalRecords = null;
        cardMedications = null;
        cardFollowUps = null;
        cardAddPatient = null;
        cardViewPatients = null;
    }
}