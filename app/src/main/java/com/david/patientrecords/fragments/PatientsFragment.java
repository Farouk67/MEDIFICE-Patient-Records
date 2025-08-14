package com.david.patientrecords.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.david.patientrecords.R;
import com.david.patientrecords.activities.AddEditPatientActivity;
import com.david.patientrecords.activities.PatientDetailActivity;
import com.david.patientrecords.activities.MedicalRecordActivity;
import com.david.patientrecords.adapters.PatientsAdapter;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.Patient;
import com.david.patientrecords.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PatientsFragment extends Fragment implements PatientsAdapter.OnPatientClickListener {

    private static final String TAG = "PatientsFragment";

    // UI Components
    private RecyclerView recyclerViewPatients;
    private PatientsAdapter patientsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private ProgressBar progressBar;
    private TextView textNoPatients;
    private TextView textPatientsCount;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabAddPatient;

    // Data
    private PatientRepository patientRepository;
    private List<Patient> allPatients;
    private String currentSearchQuery = "";
    private String currentFilter = "all"; // all, male, female, recent

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients, container, false);

        // Initialize repository
        patientRepository = PatientRepository.getInstance(requireContext());

        // Initialize views
        initViews(view);
        setupRecyclerView();
        setupSearchView();
        setupFilterChips();
        setupSwipeRefresh();
        setupFloatingActionButton();

        // Load data
        loadPatients();

        return view;
    }

    private void initViews(View view) {
        recyclerViewPatients = view.findViewById(R.id.recycler_view_patients);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        searchView = view.findViewById(R.id.search_view);
        progressBar = view.findViewById(R.id.progress_bar);
        textNoPatients = view.findViewById(R.id.text_no_patients);
        textPatientsCount = view.findViewById(R.id.text_patients_count);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        fabAddPatient = view.findViewById(R.id.fab_add_patient);
    }

    private void setupRecyclerView() {
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize with empty list
        allPatients = new ArrayList<>();
        patientsAdapter = new PatientsAdapter(getContext(), allPatients, this);
        recyclerViewPatients.setAdapter(patientsAdapter);

        // Add item decoration for spacing
        recyclerViewPatients.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterPatients();
                return true;
            }
        });

        // Set search hint
        searchView.setQueryHint("Search patients by name, phone, or blood type...");
    }

    private void setupFilterChips() {
        // Create filter chips
        String[] filters = {"All", "Male", "Female", "Recent"};
        String[] filterValues = {"all", "male", "female", "recent"};

        for (int i = 0; i < filters.length; i++) {
            Chip chip = new Chip(getContext());
            chip.setText(filters[i]);
            chip.setCheckable(true);
            chip.setTag(filterValues[i]);

            // Set first chip as checked
            if (i == 0) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int j = 0; j < chipGroupFilters.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroupFilters.getChildAt(j);
                        if (otherChip != buttonView) {
                            otherChip.setChecked(false);
                        }
                    }

                    currentFilter = (String) buttonView.getTag();
                    filterPatients();
                }
            });

            chipGroupFilters.addView(chip);
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_blue,
                R.color.success_green,
                R.color.warning_orange
        );

        swipeRefreshLayout.setOnRefreshListener(this::refreshPatients);
    }

    private void setupFloatingActionButton() {
        fabAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditPatientActivity.class);
            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, false);
            startActivityForResult(intent, Constants.REQUEST_ADD_PATIENT);
        });
    }

    private void loadPatients() {
        showLoading(true);

        // Load patients in background thread
        new Thread(() -> {
            try {
                List<Patient> patients = patientRepository.getAllPatients();

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allPatients = patients;
                        updatePatientsCount(patients.size());
                        filterPatients();
                        showLoading(false);
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showError("Error loading patients: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void refreshPatients() {
        // This method is called when user pulls down to refresh
        new Thread(() -> {
            try {
                // Simulate a small delay to show the refresh animation
                Thread.sleep(500);

                List<Patient> patients = patientRepository.getAllPatients();

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allPatients = patients;
                        updatePatientsCount(patients.size());
                        filterPatients();
                        swipeRefreshLayout.setRefreshing(false);

                        // Show a toast to indicate refresh completed
                        Toast.makeText(getContext(), "Patients list updated", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showError("Error refreshing patients: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void filterPatients() {
        if (allPatients == null) return;

        List<Patient> filteredList = new ArrayList<>();

        for (Patient patient : allPatients) {
            boolean matchesSearch = matchesSearchQuery(patient, currentSearchQuery);
            boolean matchesFilter = matchesFilter(patient, currentFilter);

            if (matchesSearch && matchesFilter) {
                filteredList.add(patient);
            }
        }

        // Update adapter
        patientsAdapter.updatePatients(filteredList);

        // Show/hide empty state
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }

        // Update count
        updateFilteredCount(filteredList.size());
    }

    private boolean matchesSearchQuery(Patient patient, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase().trim();

        return (patient.getPatientName() != null && patient.getPatientName().toLowerCase().contains(lowerQuery)) ||
                (patient.getPhone() != null && patient.getPhone().contains(lowerQuery)) ||
                (patient.getBloodType() != null && patient.getBloodType().toLowerCase().contains(lowerQuery));
    }

    private boolean matchesFilter(Patient patient, String filter) {
        switch (filter) {
            case "all":
                return true;
            case "male":
                return "Male".equalsIgnoreCase(patient.getGender());
            case "female":
                return "Female".equalsIgnoreCase(patient.getGender());
            case "recent":
                // Patients registered in the last 30 days
                return isRecentPatient(patient);
            default:
                return true;
        }
    }

    private boolean isRecentPatient(Patient patient) {
        if (patient.getRegistrationDate() == null) return false;

        try {
            // Check if registered within last 30 days
            String thirtyDaysAgo = com.david.patientrecords.utils.DateUtils.getDateMinusDays(30);
            return patient.getRegistrationDate().compareTo(thirtyDaysAgo) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewPatients.setVisibility(View.GONE);
            textNoPatients.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerViewPatients.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEmptyState() {
        textNoPatients.setVisibility(View.VISIBLE);
        recyclerViewPatients.setVisibility(View.GONE);

        // Update empty state message based on current filter/search
        if (!currentSearchQuery.isEmpty()) {
            textNoPatients.setText("No patients found for \"" + currentSearchQuery + "\"");
        } else if (!"all".equals(currentFilter)) {
            textNoPatients.setText("No " + currentFilter + " patients found");
        } else {
            textNoPatients.setText("No patients found.\nTap the + button to add your first patient!");
        }
    }

    private void hideEmptyState() {
        textNoPatients.setVisibility(View.GONE);
        recyclerViewPatients.setVisibility(View.VISIBLE);
    }

    private void updatePatientsCount(int count) {
        textPatientsCount.setText(count + " patients");
    }

    private void updateFilteredCount(int filteredCount) {
        if (allPatients != null && filteredCount != allPatients.size()) {
            textPatientsCount.setText(filteredCount + " of " + allPatients.size() + " patients");
        } else {
            updatePatientsCount(filteredCount);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    // PatientsAdapter.OnPatientClickListener implementation
    @Override
    public void onPatientClick(Patient patient) {
        try {
            Intent intent = new Intent(getActivity(), PatientDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
            intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Patient details will be available soon!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPatientEdit(Patient patient) {
        Intent intent = new Intent(getActivity(), AddEditPatientActivity.class);
        intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
        intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
        startActivityForResult(intent, Constants.REQUEST_EDIT_PATIENT);
    }

    @Override
    public void onPatientDelete(Patient patient) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + patient.getPatientName() + "?\n\nThis will also delete all associated medical records and medications.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePatient(patient);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPatientCall(Patient patient) {
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(android.net.Uri.parse("tel:" + patient.getPhone()));
            startActivity(callIntent);
        } else {
            Toast.makeText(getContext(), "No phone number available for " + patient.getPatientName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddMedicalRecord(Patient patient) {
        try {
            Intent intent = new Intent(getActivity(), MedicalRecordActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
            intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Medical records feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePatient(Patient patient) {
        showLoading(true);

        new Thread(() -> {
            try {
                int result = patientRepository.deletePatient(patient.getId());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);

                        if (result > 0) {
                            Toast.makeText(getContext(), Constants.SUCCESS_PATIENT_DELETED, Toast.LENGTH_SHORT).show();
                            // Refresh the list
                            loadPatients();
                        } else {
                            showError("Failed to delete patient");
                        }
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showError("Error deleting patient: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    // Public methods for external calls
    public void refreshData() {
        loadPatients();
    }

    public void clearSearch() {
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    public void scrollToTop() {
        if (recyclerViewPatients != null) {
            recyclerViewPatients.smoothScrollToPosition(0);
        }
    }

    public void addNewPatient(Patient patient) {
        if (patientsAdapter != null) {
            patientsAdapter.addPatient(patient);
            scrollToTop();
            updatePatientsCount(allPatients.size() + 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_ADD_PATIENT:
                    Toast.makeText(getContext(), Constants.SUCCESS_PATIENT_ADDED, Toast.LENGTH_SHORT).show();
                    refreshPatients(); // Use refresh method instead of loadPatients
                    break;

                case Constants.REQUEST_EDIT_PATIENT:
                    Toast.makeText(getContext(), Constants.SUCCESS_PATIENT_UPDATED, Toast.LENGTH_SHORT).show();
                    refreshPatients(); // Use refresh method instead of loadPatients
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible again
        if (allPatients != null && !allPatients.isEmpty()) {
            // Only do a light refresh if we already have data
            filterPatients();
        } else {
            // Do a full refresh if we don't have data
            loadPatients();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Clear search focus when leaving fragment
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    // Helper method to get current filter summary
    private String getFilterSummary() {
        switch (currentFilter) {
            case "male":
                return "Male patients";
            case "female":
                return "Female patients";
            case "recent":
                return "Recent patients (last 30 days)";
            default:
                return "All patients";
        }
    }

    // Method to export patient list (for future implementation)
    public void exportPatientList() {
        // TODO: Implement CSV export functionality
        Toast.makeText(getContext(), "Export functionality coming soon!", Toast.LENGTH_SHORT).show();
    }

    // Method to show patient statistics
    public void showPatientStatistics() {
        if (allPatients == null || allPatients.isEmpty()) {
            Toast.makeText(getContext(), "No patients data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate statistics
        int totalPatients = allPatients.size();
        int maleCount = 0, femaleCount = 0, otherCount = 0;

        for (Patient patient : allPatients) {
            if ("Male".equalsIgnoreCase(patient.getGender())) {
                maleCount++;
            } else if ("Female".equalsIgnoreCase(patient.getGender())) {
                femaleCount++;
            } else {
                otherCount++;
            }
        }

        String stats = "📊 Patient Statistics\n\n" +
                "Total Patients: " + totalPatients + "\n" +
                "👨 Male: " + maleCount + "\n" +
                "👩 Female: " + femaleCount + "\n" +
                "⚧ Other: " + otherCount;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Patient Statistics")
                .setMessage(stats)
                .setPositiveButton("OK", null)
                .show();
    }
}