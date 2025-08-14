package com.david.patientrecords.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.david.patientrecords.R;
import com.david.patientrecords.models.MedicalRecord;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsAdapter extends RecyclerView.Adapter<MedicalRecordsAdapter.MedicalRecordViewHolder> {

    private static final String TAG = "MedicalRecordsAdapter";

    private Context context;
    private List<MedicalRecord> medicalRecords;
    private boolean isCompactMode;
    private OnMedicalRecordClickListener listener;

    // Interface for handling click events
    public interface OnMedicalRecordClickListener {
        void onMedicalRecordClick(MedicalRecord record);
        void onMedicalRecordEdit(MedicalRecord record);
        void onMedicalRecordDelete(MedicalRecord record);
    }

    // Constructor
    public MedicalRecordsAdapter(Context context, List<MedicalRecord> records, boolean compact) {
        this.context = context;
        this.medicalRecords = records != null ? records : new ArrayList<>();
        this.isCompactMode = compact;
    }

    // Constructor with listener
    public MedicalRecordsAdapter(Context context, List<MedicalRecord> records, boolean compact, OnMedicalRecordClickListener listener) {
        this.context = context;
        this.medicalRecords = records != null ? records : new ArrayList<>();
        this.isCompactMode = compact;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicalRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isCompactMode) {
            // Use a simple layout for compact mode (dashboard)
            view = LayoutInflater.from(context).inflate(R.layout.item_medical_record_compact, parent, false);
        } else {
            // Use full layout for detailed view
            view = LayoutInflater.from(context).inflate(R.layout.item_medical_record, parent, false);
        }
        return new MedicalRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalRecordViewHolder holder, int position) {
        MedicalRecord record = medicalRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    // Update the records list
    public void updateRecords(List<MedicalRecord> newRecords) {
        this.medicalRecords = newRecords != null ? newRecords : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Add a new record
    public void addRecord(MedicalRecord record) {
        if (record != null) {
            medicalRecords.add(0, record); // Add to top
            notifyItemInserted(0);
        }
    }

    // Remove a record
    public void removeRecord(int position) {
        if (position >= 0 && position < medicalRecords.size()) {
            medicalRecords.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Set click listener
    public void setOnMedicalRecordClickListener(OnMedicalRecordClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class
    public class MedicalRecordViewHolder extends RecyclerView.ViewHolder {

        private TextView textPatientName;
        private TextView textVisitDate;
        private TextView textDiagnosis;
        private TextView textDoctorName;
        private TextView textSymptoms;
        private TextView textTreatment;
        private TextView textVisitType;
        private View layoutDetails;

        public MedicalRecordViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views - handle both compact and full layouts
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textVisitDate = itemView.findViewById(R.id.text_visit_date);
            textDiagnosis = itemView.findViewById(R.id.text_diagnosis);
            textDoctorName = itemView.findViewById(R.id.text_doctor_name);

            // These might not exist in compact mode
            textSymptoms = itemView.findViewById(R.id.text_symptoms);
            textTreatment = itemView.findViewById(R.id.text_treatment);
            textVisitType = itemView.findViewById(R.id.text_visit_type);
            layoutDetails = itemView.findViewById(R.id.layout_details);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onMedicalRecordClick(medicalRecords.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    showContextMenu(v, getAdapterPosition());
                }
                return true;
            });
        }

        public void bind(MedicalRecord record) {
            // Set patient name (if available)
            if (textPatientName != null) {
                String patientName = record.getPatientName();
                if (patientName != null && !patientName.isEmpty()) {
                    textPatientName.setText(patientName);
                    textPatientName.setVisibility(View.VISIBLE);
                } else {
                    textPatientName.setVisibility(View.GONE);
                }
            }

            // Set visit date
            if (textVisitDate != null) {
                String visitDate = record.getVisitDate();
                if (visitDate != null && !visitDate.isEmpty()) {
                    textVisitDate.setText(formatDate(visitDate));
                } else {
                    textVisitDate.setText("No date");
                }
            }

            // Set diagnosis
            if (textDiagnosis != null) {
                String diagnosis = record.getDiagnosis();
                if (diagnosis != null && !diagnosis.isEmpty()) {
                    textDiagnosis.setText(diagnosis);
                } else {
                    textDiagnosis.setText("No diagnosis recorded");
                }
            }

            // Set doctor name
            if (textDoctorName != null) {
                String doctorInfo = getDoctorDisplayInfo(record);
                textDoctorName.setText(doctorInfo);
            }

            // Set symptoms (if view exists)
            if (textSymptoms != null) {
                String symptoms = record.getSymptoms();
                if (symptoms != null && !symptoms.isEmpty()) {
                    textSymptoms.setText(symptoms);
                    textSymptoms.setVisibility(View.VISIBLE);
                } else {
                    textSymptoms.setVisibility(View.GONE);
                }
            }

            // Set treatment (if view exists)
            if (textTreatment != null) {
                String treatment = record.getTreatment();
                if (treatment != null && !treatment.isEmpty()) {
                    textTreatment.setText(treatment);
                    textTreatment.setVisibility(View.VISIBLE);
                } else {
                    textTreatment.setVisibility(View.GONE);
                }
            }

            // Set visit type (if view exists)
            if (textVisitType != null) {
                String visitType = record.getVisitType();
                if (visitType != null && !visitType.isEmpty()) {
                    textVisitType.setText(visitType);
                    setVisitTypeStyle(textVisitType, visitType);
                } else {
                    textVisitType.setText("Regular");
                    setVisitTypeStyle(textVisitType, "Regular");
                }
            }
        }

        private String getDoctorDisplayInfo(MedicalRecord record) {
            String doctorName = record.getDoctorName();
            String specialty = record.getDoctorSpecialty();

            if (doctorName != null && !doctorName.isEmpty()) {
                if (specialty != null && !specialty.isEmpty()) {
                    return doctorName + " - " + specialty;
                } else {
                    return doctorName;
                }
            } else {
                return "Unknown Doctor";
            }
        }

        private void setVisitTypeStyle(TextView textView, String visitType) {
            // Set different colors/styles based on visit type
            switch (visitType.toLowerCase()) {
                case "emergency":
                    textView.setTextColor(context.getResources().getColor(R.color.danger_red));
                    break;
                case "follow-up":
                    textView.setTextColor(context.getResources().getColor(R.color.warning_orange));
                    break;
                case "regular":
                default:
                    textView.setTextColor(context.getResources().getColor(R.color.primary_blue));
                    break;
            }
        }

        private String formatDate(String dateString) {
            try {
                // Simple date formatting - you can enhance this
                if (dateString != null && dateString.length() >= 10) {
                    return dateString.substring(0, 10); // YYYY-MM-DD format
                }
                return dateString;
            } catch (Exception e) {
                return dateString;
            }
        }

        private void showContextMenu(View view, int position) {
            // Create a simple context menu
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, view);

            // Manually add menu items since we don't have a menu XML
            popup.getMenu().add(0, 1, 0, "Edit");
            popup.getMenu().add(0, 2, 0, "Delete");

            popup.setOnMenuItemClickListener(item -> {
                MedicalRecord record = medicalRecords.get(position);
                switch (item.getItemId()) {
                    case 1: // Edit
                        if (listener != null) {
                            listener.onMedicalRecordEdit(record);
                        }
                        return true;
                    case 2: // Delete
                        if (listener != null) {
                            listener.onMedicalRecordDelete(record);
                        }
                        return true;
                }
                return false;
            });

            popup.show();
        }
    }

    // Helper method to check if records list is empty
    public boolean isEmpty() {
        return medicalRecords.isEmpty();
    }

    // Helper method to get record at position
    public MedicalRecord getRecord(int position) {
        if (position >= 0 && position < medicalRecords.size()) {
            return medicalRecords.get(position);
        }
        return null;
    }

    // Helper method to get all records
    public List<MedicalRecord> getAllRecords() {
        return new ArrayList<>(medicalRecords);
    }
}