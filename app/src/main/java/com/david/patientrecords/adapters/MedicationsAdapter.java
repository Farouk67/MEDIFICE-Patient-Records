package com.david.patientrecords.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.david.patientrecords.R;
import com.david.patientrecords.models.Medication;
import com.david.patientrecords.utils.DateUtils;

import java.util.List;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.MedicationViewHolder> {

    private Context context;
    private List<Medication> medications;
    private OnMedicationClickListener listener;

    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
        void onMedicationEdit(Medication medication);
        void onMedicationDelete(Medication medication);
    }

    public MedicationsAdapter(Context context, List<Medication> medications, OnMedicationClickListener listener) {
        this.context = context;
        this.medications = medications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication_card, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.bind(medication);
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public void updateMedications(List<Medication> newMedications) {
        this.medications = newMedications;
        notifyDataSetChanged();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView textMedicationName;
        TextView textDosage;
        TextView textFrequency;
        TextView textPrescribedBy;
        TextView textStartDate;
        TextView textInstructions;
        TextView textRefills;
        TextView textEndDate;
        TextView textPharmacyName;
        LinearLayout layoutInstructions;
        LinearLayout layoutPharmacyInfo;
        View buttonMoreOptions;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicationName = itemView.findViewById(R.id.text_medication_name);
            textDosage = itemView.findViewById(R.id.text_dosage);
            textFrequency = itemView.findViewById(R.id.text_frequency);
            textPrescribedBy = itemView.findViewById(R.id.text_prescribed_by);
            textStartDate = itemView.findViewById(R.id.text_start_date);
            textInstructions = itemView.findViewById(R.id.text_instructions);
            textRefills = itemView.findViewById(R.id.text_refills);
            textEndDate = itemView.findViewById(R.id.text_end_date);
            textPharmacyName = itemView.findViewById(R.id.text_pharmacy_name);
            layoutInstructions = itemView.findViewById(R.id.layout_instructions);
            layoutPharmacyInfo = itemView.findViewById(R.id.layout_pharmacy_info);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }

        public void bind(Medication medication) {
            // Medication name
            if (medication.getMedicationName() != null) {
                textMedicationName.setText(medication.getMedicationName());
            } else {
                textMedicationName.setText("Unknown Medication");
            }

            // Dosage
            if (medication.getDosage() != null) {
                textDosage.setText(medication.getDosage());
            } else {
                textDosage.setText("No dosage");
            }

            // Frequency
            if (medication.getFrequency() != null) {
                textFrequency.setText(medication.getFrequency());
            } else {
                textFrequency.setText("As needed");
            }

            // Prescribed by
            String doctorName = medication.getDoctorName();
            if (doctorName == null || doctorName.isEmpty()) {
                doctorName = medication.getPrescribedBy();
            }
            if (doctorName != null && !doctorName.isEmpty()) {
                if (!doctorName.startsWith("Dr.")) {
                    doctorName = "Dr. " + doctorName;
                }
                textPrescribedBy.setText(doctorName);
            } else {
                textPrescribedBy.setText("Unknown Doctor");
            }

            // Start date
            if (medication.getStartDate() != null) {
                textStartDate.setText(DateUtils.formatDateForDisplay(medication.getStartDate()));
            } else {
                textStartDate.setText("No date");
            }

            // Instructions
            if (medication.getInstructions() != null && !medication.getInstructions().isEmpty()) {
                textInstructions.setText(medication.getInstructions());
                layoutInstructions.setVisibility(View.VISIBLE);
            } else {
                layoutInstructions.setVisibility(View.GONE);
            }

            // Refills
            int refills = medication.getRefillsRemaining();
            if (refills > 0) {
                textRefills.setText(refills + " refills left");
            } else {
                textRefills.setText("No refills");
            }

            // End date
            if (medication.getEndDate() != null && !medication.getEndDate().isEmpty()) {
                textEndDate.setText("Ends: " + DateUtils.formatDateForDisplay(medication.getEndDate()));
                textEndDate.setVisibility(View.VISIBLE);
            } else {
                textEndDate.setVisibility(View.GONE);
            }

            // Pharmacy info
            if (medication.getPharmacyName() != null && !medication.getPharmacyName().isEmpty()) {
                textPharmacyName.setText(medication.getPharmacyName());
                layoutPharmacyInfo.setVisibility(View.VISIBLE);
            } else {
                layoutPharmacyInfo.setVisibility(View.GONE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicationClick(medication);
                }
            });

            if (buttonMoreOptions != null) {
                buttonMoreOptions.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMedicationEdit(medication);
                    }
                });
            }
        }
    }
}
