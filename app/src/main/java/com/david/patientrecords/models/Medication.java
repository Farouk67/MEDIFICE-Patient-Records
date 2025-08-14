package com.david.patientrecords.models;

public class Medication {
    private long id;
    private long patientId;
    private String medicationName;
    private String genericName;
    private String dosage;
    private String frequency;
    private String startDate;
    private String endDate;
    private String prescribedBy;
    private String instructions;
    private String sideEffects;
    private boolean isActive;
    private int refillsRemaining;
    private String pharmacyName;
    private String createdAt;
    private String updatedAt;

    // Additional fields for comprehensive medication management
    private String medicationType;
    private String pharmacy;
    private String doctorName;
    private String notes;

    // Additional fields for display
    private String patientName; // When joining with Patient table

    // Constructors
    public Medication() {
        this.isActive = true;
    }

    public Medication(long patientId, String medicationName, String dosage,
                      String frequency, String prescribedBy) {
        this.patientId = patientId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.prescribedBy = prescribedBy;
        this.isActive = true;
    }

    public Medication(long id, long patientId, String medicationName, String genericName,
                      String dosage, String frequency, String startDate, String endDate,
                      String prescribedBy, String instructions) {
        this.id = id;
        this.patientId = patientId;
        this.medicationName = medicationName;
        this.genericName = genericName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.prescribedBy = prescribedBy;
        this.instructions = instructions;
        this.isActive = true;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPrescribedBy() {
        return prescribedBy;
    }

    public void setPrescribedBy(String prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRefillsRemaining() {
        return refillsRemaining;
    }

    public void setRefillsRemaining(int refillsRemaining) {
        this.refillsRemaining = refillsRemaining;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    // Additional fields getters and setters
    public String getMedicationType() {
        return medicationType;
    }

    public void setMedicationType(String medicationType) {
        this.medicationType = medicationType;
    }

    public String getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(String pharmacy) {
        this.pharmacy = pharmacy;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility methods
    public String getDosageFrequency() {
        StringBuilder result = new StringBuilder();
        if (dosage != null) result.append(dosage);
        if (frequency != null) {
            if (result.length() > 0) result.append(" - ");
            result.append(frequency);
        }
        return result.toString();
    }

    public String getDisplayName() {
        if (genericName != null && !genericName.equals(medicationName)) {
            return medicationName + " (" + genericName + ")";
        }
        return medicationName != null ? medicationName : "Unknown Medication";
    }

    public String getDoctorDisplayName() {
        if (doctorName != null && !doctorName.isEmpty()) {
            return "Dr. " + doctorName;
        } else if (prescribedBy != null && !prescribedBy.isEmpty()) {
            return "Dr. " + prescribedBy;
        }
        return "Unknown Doctor";
    }

    public boolean isExpired() {
        // This would need DateUtils to properly compare dates
        // For now, just return false
        return false;
    }

    public boolean isExpiringSoon() {
        // This would need DateUtils to check if medication expires within 7 days
        // For now, just return false
        return false;
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", medicationType='" + medicationType + '\'' +
                ", prescribedBy='" + prescribedBy + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", pharmacy='" + pharmacy + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}