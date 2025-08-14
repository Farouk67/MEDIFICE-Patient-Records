package com.david.patientrecords.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.david.patientrecords.R;
import com.david.patientrecords.activities.AddEditPatientActivity;
import com.david.patientrecords.activities.PatientDetailActivity;
import com.david.patientrecords.activities.MedicalRecordActivity;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.models.Patient;
import com.david.patientrecords.utils.Constants;
import com.david.patientrecords.utils.DateUtils;
import com.david.patientrecords.utils.ImageUtils;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.PatientViewHolder> {

    private static final String TAG = "PatientsAdapter";

    private Context context;
    private List<Patient> patients;
    private List<Patient> patientsFiltered; // For search functionality
    private PatientRepository patientRepository;
    private OnPatientClickListener onPatientClickListener;
    private int lastPosition = -1; // For animation

    // Interface for handling clicks
    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
        void onPatientEdit(Patient patient);
        void onPatientDelete(Patient patient);
        void onPatientCall(Patient patient);
        void onAddMedicalRecord(Patient patient);
    }

    public PatientsAdapter(Context context, List<Patient> patients, OnPatientClickListener listener) {
        this.context = context;
        this.patients = patients != null ? patients : new ArrayList<>();
        this.patientsFiltered = new ArrayList<>(this.patients);
        this.onPatientClickListener = listener;
        this.patientRepository = PatientRepository.getInstance(context);
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient_card, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientsFiltered.get(position);

        // Load patient image with fallback to initials
        ImageUtils.loadPatientImageWithFallback(
                holder.itemView.getContext(),
                patient.getImagePath(),
                patient.getName(),
                holder.imagePatientProfile
        );

        // Set patient information
        holder.textPatientName.setText(patient.getName());
        holder.textPatientAge.setText(patient.getAge() + " years");
        holder.textPatientGender.setText(patient.getGender());
        holder.textBloodType.setText(patient.getBloodType());
        holder.textPatientPhone.setText(patient.getPhone());
        holder.textRegistrationDate.setText("Registered: " + patient.getRegistrationDate());

        // Set medical conditions and allergies
        if (patient.getMedicalConditions() != null && !patient.getMedicalConditions().isEmpty()) {
            holder.chipMedicalConditions.setText(patient.getMedicalConditions());
            holder.chipMedicalConditions.setVisibility(View.VISIBLE);
        } else {
            holder.chipMedicalConditions.setVisibility(View.GONE);
        }

        if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
            holder.chipAllergies.setText(patient.getAllergies());
            holder.chipAllergies.setVisibility(View.VISIBLE);
        } else {
            holder.chipAllergies.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.buttonCallPatient.setOnClickListener(v -> {
            // Call patient
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + patient.getPhone()));
            holder.itemView.getContext().startActivity(callIntent);
        });

        holder.buttonMoreOptions.setOnClickListener(v -> {
            showMoreOptionsMenu(v, patient, position);
        });
        // Add animation
        setAnimation(holder.itemView, position);
    }

    private void showMoreOptionsMenu(View view, Patient patient, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);

        // Create menu items manually
        popup.getMenu().add(0, 1, 0, "Edit Patient");
        popup.getMenu().add(0, 2, 0, "View Details");
        popup.getMenu().add(0, 3, 0, "Delete Patient");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) { // Edit Patient
                Intent intent = new Intent(view.getContext(), AddEditPatientActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("patient_id", patient.getId());
                view.getContext().startActivity(intent);
                return true;
            } else if (itemId == 2) { // View Details
                Intent intent = new Intent(view.getContext(), PatientDetailActivity.class);
                intent.putExtra("patient_id", patient.getId());
                intent.putExtra("patient_name", patient.getName());
                view.getContext().startActivity(intent);
                return true;
            } else if (itemId == 3) { // Delete Patient
                showDeleteConfirmation(view.getContext(), patient, position);
                return true;
            }
            return false;
        });

        popup.show();
    }


    private void showDeleteConfirmation(Context context, Patient patient, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + patient.getName() + "?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete patient from database
                    new Thread(() -> {
                        try {
                            // Delete associated image file if exists
                            if (patient.getImagePath() != null && !patient.getImagePath().isEmpty()) {
                                File imageFile = new File(patient.getImagePath());
                                if (imageFile.exists()) {
                                    imageFile.delete();
                                }
                            }

                            // Delete from database
                            patientRepository.deletePatient(patient.getId());

                            // Update UI on main thread
                            ((Activity) context).runOnUiThread(() -> {
                                patientsFiltered.remove(position);
                                patients.remove(patient);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, patientsFiltered.size());

                                Toast.makeText(context, patient.getName() + " deleted successfully",
                                        Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            ((Activity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Error deleting patient: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    @Override
    public int getItemCount() {
        return patientsFiltered.size();
    }

    // Animation for smooth card appearance
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Update patient list
    public void updatePatients(List<Patient> newPatients) {
        this.patients = newPatients != null ? newPatients : new ArrayList<>();
        this.patientsFiltered = new ArrayList<>(this.patients);
        notifyDataSetChanged();
    }

    // Filter patients based on search query
    public void filter(String searchText) {
        patientsFiltered.clear();

        if (searchText.isEmpty()) {
            patientsFiltered.addAll(patients);
        } else {
            String filterPattern = searchText.toLowerCase().trim();

            for (Patient patient : patients) {
                if (patient.getPatientName().toLowerCase().contains(filterPattern) ||
                        (patient.getPhone() != null && patient.getPhone().contains(filterPattern)) ||
                        (patient.getBloodType() != null && patient.getBloodType().toLowerCase().contains(filterPattern))) {
                    patientsFiltered.add(patient);
                }
            }
        }

        notifyDataSetChanged();
    }

    // Item decoration for card spacing
    public static class PatientCardItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public PatientCardItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;

            // Add top margin only for the first item to avoid double spacing
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            }
        }
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {

        // UI Elements
        private CircleImageView imagePatientProfile;
        private View statusIndicator;
        private TextView textPatientName;
        private TextView textPatientAge;
        private TextView textPatientGender;
        private TextView textBloodType;
        private TextView textPatientPhone;
        private TextView chipMedicalConditions;
        private TextView chipAllergies;
        private TextView textRegistrationDate;
        private TextView textPatientId;
        private ImageButton buttonCallPatient;
        private ImageButton buttonMoreOptions;

        // Expandable section
        private LinearLayout layoutExpandableDetails;
        private TextView textRecordsCount;
        private TextView textMedicationsCount;
        private TextView textLastVisit;
        private com.google.android.material.button.MaterialButton buttonViewDetails;
        private com.google.android.material.button.MaterialButton buttonAddRecord;

        private boolean isExpanded = false;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all views first
            initViews();

            // Debug logging
            if (layoutExpandableDetails != null) {
                Log.d("PatientsAdapter", "✅ Expandable layout found successfully!");
            } else {
                Log.e("PatientsAdapter", "❌ ERROR: Expandable layout NOT found!");
            }

            // Initialize expansion state
            isExpanded = false;

            // CRITICAL: Setup click listeners for expansion
            setupClickListeners();
        }

        private void initViews() {
            // Basic patient info views
            imagePatientProfile = itemView.findViewById(R.id.image_patient_profile);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textPatientAge = itemView.findViewById(R.id.text_patient_age);
            textPatientGender = itemView.findViewById(R.id.text_patient_gender);
            textBloodType = itemView.findViewById(R.id.text_blood_type);
            textPatientPhone = itemView.findViewById(R.id.text_patient_phone);
            chipMedicalConditions = itemView.findViewById(R.id.chip_medical_conditions);
            chipAllergies = itemView.findViewById(R.id.chip_allergies);
            textRegistrationDate = itemView.findViewById(R.id.text_registration_date);
            textPatientId = itemView.findViewById(R.id.text_patient_id);
            buttonCallPatient = itemView.findViewById(R.id.button_call_patient);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);

            // CRITICAL: Expandable section views
            layoutExpandableDetails = itemView.findViewById(R.id.layout_expandable_details);
            textRecordsCount = itemView.findViewById(R.id.text_records_count);
            textMedicationsCount = itemView.findViewById(R.id.text_medications_count);
            textLastVisit = itemView.findViewById(R.id.text_last_visit);
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
            buttonAddRecord = itemView.findViewById(R.id.button_add_record);

            // Debug check
            Log.d("PatientsAdapter", "initViews completed. Expandable layout: " +
                    (layoutExpandableDetails != null ? "FOUND" : "NULL"));
        }

        private void setupClickListeners() {
            // MAIN CARD CLICK - Toggle expansion (NOT open profile)
            itemView.setOnClickListener(v -> {
                Log.d("PatientsAdapter", "Card clicked! Toggling expansion...");
                toggleExpansion();
            });

            // Call button click
            buttonCallPatient.setOnClickListener(v -> {
                Patient patient = patientsFiltered.get(getAdapterPosition());
                makePhoneCall(patient.getPhone());
            });

            // More options button click
            buttonMoreOptions.setOnClickListener(v -> {
                showMoreOptionsMenu(v);
            });

            // View details button click (in expandable section) - This opens profile
            if (buttonViewDetails != null) {
                buttonViewDetails.setOnClickListener(v -> {
                    Patient patient = patientsFiltered.get(getAdapterPosition());
                    Intent intent = new Intent(context, PatientDetailActivity.class);
                    intent.putExtra("patient_id", patient.getId());
                    intent.putExtra("patient_name", patient.getName());
                    context.startActivity(intent);
                });
            }

            // Add record button click (in expandable section)
            if (buttonAddRecord != null) {
                buttonAddRecord.setOnClickListener(v -> {
                    Patient patient = patientsFiltered.get(getAdapterPosition());
                    Toast.makeText(context, "Add medical record for " + patient.getName(), Toast.LENGTH_SHORT).show();
                });
            }
        }


        public void bind(Patient patient) {
            // Basic patient information
            textPatientName.setText(patient.getPatientName());
            textPatientAge.setText(patient.getAge() + " years");
            textPatientGender.setText(patient.getGender() != null ? patient.getGender() : "Not specified");
            textBloodType.setText(patient.getBloodType() != null ? patient.getBloodType() : "Unknown");
            textPatientPhone.setText(patient.getPhone() != null ? patient.getPhone() : "No phone");
            textPatientId.setText("#" + String.format("%03d", patient.getId()));

            // Registration date
            String registrationDate = DateUtils.formatDateForDisplay(patient.getRegistrationDate());
            textRegistrationDate.setText("Registered: " + registrationDate);

            // Set patient profile image with fallback
            ImageUtils.loadPatientImageWithFallback(context, patient.getImagePath(),
                    patient.getPatientName(), imagePatientProfile);

            // Medical conditions and allergies
            setupMedicalInfo(patient);

            // Load additional data for expandable section
            loadExpandableData(patient);

            // Status indicator (active patients show green)
            statusIndicator.setVisibility(patient.isActive() ? View.VISIBLE : View.GONE);
        }

        private void setupMedicalInfo(Patient patient) {
            // Medical conditions chip
            if (patient.getMedicalConditions() != null && !patient.getMedicalConditions().isEmpty()) {
                chipMedicalConditions.setText(patient.getMedicalConditions());
                chipMedicalConditions.setVisibility(View.VISIBLE);
            } else {
                chipMedicalConditions.setVisibility(View.GONE);
            }

            // Allergies chip
            if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
                chipAllergies.setText(patient.getAllergies());
                chipAllergies.setVisibility(View.VISIBLE);
            } else {
                chipAllergies.setVisibility(View.GONE);
            }

            // Show medical info layout if either chip is visible
            LinearLayout layoutMedicalInfo = itemView.findViewById(R.id.layout_medical_conditions);
            if (layoutMedicalInfo != null) {
                boolean showMedicalInfo = chipMedicalConditions.getVisibility() == View.VISIBLE ||
                        chipAllergies.getVisibility() == View.VISIBLE;
                layoutMedicalInfo.setVisibility(showMedicalInfo ? View.VISIBLE : View.GONE);
            }
        }

        private void loadExpandableData(Patient patient) {
            // Load data in background thread
            new Thread(() -> {
                try {
                    // Get medical records count
                    int recordsCount = 0;
                    try {
                        recordsCount = patientRepository.getMedicalRecordsByPatientId(patient.getId()).size();
                    } catch (Exception e) {
                        // Method might not exist yet
                    }

                    // Get medications count
                    int medicationsCount = 0;
                    try {
                        medicationsCount = patientRepository.getMedicationsByPatientId(patient.getId()).size();
                    } catch (Exception e) {
                        // Method might not exist yet
                    }

                    // Get last visit info
                    String lastVisit = "No visits";
                    try {
                        var latestRecord = patientRepository.getLatestMedicalRecord(patient.getId());
                        if (latestRecord != null) {
                            lastVisit = DateUtils.getRelativeTimeString(latestRecord.getVisitDate());
                        }
                    } catch (Exception e) {
                        // Method might not exist yet
                    }

                    // Update UI on main thread
                    final int finalRecordsCount = recordsCount;
                    final int finalMedicationsCount = medicationsCount;
                    final String finalLastVisit = lastVisit;

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (textRecordsCount != null) {
                                textRecordsCount.setText(String.valueOf(finalRecordsCount));
                            }
                            if (textMedicationsCount != null) {
                                textMedicationsCount.setText(String.valueOf(finalMedicationsCount));
                            }
                            if (textLastVisit != null) {
                                textLastVisit.setText(finalLastVisit);
                            }
                        });
                    }

                } catch (Exception e) {
                    // Handle error silently
                }
            }).start();
        }

        private void toggleExpansion() {
            if (layoutExpandableDetails == null) {
                Log.e("PatientsAdapter", "❌ Cannot expand - layoutExpandableDetails is NULL!");
                return;
            }

            isExpanded = !isExpanded;
            Log.d("PatientsAdapter", "🔄 Toggling expansion. New state: " + isExpanded);

            if (isExpanded) {
                Log.d("PatientsAdapter", "📈 SHOWING expandable section");
                layoutExpandableDetails.setVisibility(View.VISIBLE);

                // Add smooth animation
                layoutExpandableDetails.setAlpha(0f);
                layoutExpandableDetails.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();

                // Load expandable data
                Patient patient = patientsFiltered.get(getAdapterPosition());
                loadExpandableData(patient);

            } else {
                Log.d("PatientsAdapter", "📉 HIDING expandable section");
                layoutExpandableDetails.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutExpandableDetails.setVisibility(View.GONE))
                        .start();
            }
        }

        private void updateExpandableIndicator() {
            // You can add a small indicator icon that rotates when expanded
            // For now, we'll just use the existing UI

            // Optional: Change card elevation when expanded
            androidx.cardview.widget.CardView cardView = (androidx.cardview.widget.CardView) itemView;
            if (isExpanded) {
                cardView.setCardElevation(context.getResources().getDimension(R.dimen.card_elevation_large));
            } else {
                cardView.setCardElevation(context.getResources().getDimension(R.dimen.card_elevation_small));
            }
        }
        private void showMoreOptionsMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(context, view);

            // Create menu items manually since menu XML might not exist
            popupMenu.getMenu().add(0, R.id.menu_edit_patient, 0, "Edit Patient");
            popupMenu.getMenu().add(0, R.id.menu_share_info, 0, "Share Info");
            popupMenu.getMenu().add(0, R.id.menu_delete_patient, 0, "Delete Patient");

            popupMenu.setOnMenuItemClickListener(item -> {
                Patient patient = patientsFiltered.get(getAdapterPosition());
                int itemId = item.getItemId();

                if (itemId == R.id.menu_edit_patient) {
                    if (onPatientClickListener != null) {
                        onPatientClickListener.onPatientEdit(patient);
                    } else {
                        openEditPatient(patient);
                    }
                    return true;

                } else if (itemId == R.id.menu_share_info) {
                    sharePatientInfo(patient);
                    return true;

                } else if (itemId == R.id.menu_delete_patient) {
                    if (onPatientClickListener != null) {
                        onPatientClickListener.onPatientDelete(patient);
                    } else {
                        showDeleteConfirmation(patient);
                    }
                    return true;
                }

                return false;
            });

            popupMenu.show();
        }

        // Helper methods for actions
        private void makePhoneCall(String phoneNumber) {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            } else {
                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        }

        private void openPatientDetails(Patient patient) {
            try {
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Patient details feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        }

        private void openEditPatient(Patient patient) {
            Intent intent = new Intent(context, AddEditPatientActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
            context.startActivity(intent);
        }

        private void openAddMedicalRecord(Patient patient) {
            try {
                Intent intent = new Intent(context, MedicalRecordActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Medical records feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        }

        private void openMedicalRecords(Patient patient) {
            try {
                Intent intent = new Intent(context, MedicalRecordActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Medical records feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        }

        private void handleMedicationClick(Patient patient) {
            try {
                // Try to launch MedicationActivity if it exists
                Class<?> medicationActivityClass = Class.forName("com.david.patientrecords.activities.MedicationActivity");
                Intent intent = new Intent(context, medicationActivityClass);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                intent.putExtra(Constants.EXTRA_PATIENT_NAME, patient.getPatientName());
                context.startActivity(intent);
            } catch (ClassNotFoundException e) {
                // MedicationActivity doesn't exist yet, show coming soon message
                Toast.makeText(context, "Medications feature coming soon!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Any other error, show generic message
                Toast.makeText(context, "Unable to open medications. Feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        }

        private void sharePatientInfo(Patient patient) {
            String shareText = "Patient Information:\n\n" +
                    "Name: " + patient.getPatientName() + "\n" +
                    "Age: " + patient.getAge() + " years\n" +
                    "Gender: " + (patient.getGender() != null ? patient.getGender() : "Not specified") + "\n" +
                    "Blood Type: " + (patient.getBloodType() != null ? patient.getBloodType() : "Unknown") + "\n" +
                    "Phone: " + (patient.getPhone() != null ? patient.getPhone() : "No phone") + "\n" +
                    "Registration Date: " + DateUtils.formatDateForDisplay(patient.getRegistrationDate());

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Patient Information - " + patient.getPatientName());

            context.startActivity(Intent.createChooser(shareIntent, "Share Patient Information"));
        }

        private void showDeleteConfirmation(Patient patient) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setTitle("Delete Patient")
                    .setMessage("Are you sure you want to delete " + patient.getPatientName() + "?\n\nThis action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deletePatient(patient))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void deletePatient(Patient patient) {
            new Thread(() -> {
                try {
                    // Delete associated image file if exists
                    if (patient.getImagePath() != null && !patient.getImagePath().isEmpty()) {
                        File imageFile = new File(patient.getImagePath());
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }

                    int result = patientRepository.deletePatient(patient.getId());

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (result > 0) {
                                Toast.makeText(context, Constants.SUCCESS_PATIENT_DELETED, Toast.LENGTH_SHORT).show();
                                // Remove from list
                                int position = patientsFiltered.indexOf(patient);
                                if (position != -1) {
                                    patientsFiltered.remove(position);
                                    patients.remove(patient);
                                    notifyItemRemoved(position);
                                }
                            } else {
                                Toast.makeText(context, Constants.ERROR_DATABASE_ERROR, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, Constants.ERROR_DATABASE_ERROR, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        }
    }

    // Public methods for external control
    public void expandAllCards() {
        // Implementation for expanding all cards
        notifyDataSetChanged();
    }

    public void collapseAllCards() {
        // Implementation for collapsing all cards
        notifyDataSetChanged();
    }

    public Patient getPatientAt(int position) {
        if (position >= 0 && position < patientsFiltered.size()) {
            return patientsFiltered.get(position);
        }
        return null;
    }

    public List<Patient> getFilteredPatients() {
        return new ArrayList<>(patientsFiltered);
    }

    public int getPatientPosition(long patientId) {
        for (int i = 0; i < patientsFiltered.size(); i++) {
            if (patientsFiltered.get(i).getId() == patientId) {
                return i;
            }
        }
        return -1;
    }

    // Method to refresh a specific patient's data
    public void refreshPatient(long patientId) {
        int position = getPatientPosition(patientId);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    // Method to add a new patient
    public void addPatient(Patient patient) {
        patients.add(0, patient); // Add to beginning
        patientsFiltered.add(0, patient);
        notifyItemInserted(0);
    }

    // Method to update a patient
    public void updatePatient(Patient updatedPatient) {
        // Update in both lists
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId() == updatedPatient.getId()) {
                patients.set(i, updatedPatient);
                break;
            }
        }

        for (int i = 0; i < patientsFiltered.size(); i++) {
            if (patientsFiltered.get(i).getId() == updatedPatient.getId()) {
                patientsFiltered.set(i, updatedPatient);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PatientViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // Clear animations to prevent memory leaks
        holder.itemView.clearAnimation();
    }
}