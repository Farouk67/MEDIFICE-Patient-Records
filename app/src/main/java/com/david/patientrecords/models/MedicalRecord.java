package com.david.patientrecords.models;

public class MedicalRecord {
    private long id;
    private long patientId;
    private String visitDate;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String doctorName;
    private String doctorSpecialty;
    private String vitalSigns; // JSON format
    private String notes;
    private String followUpDate;
    private String visitType;
    private String createdAt;
    private String updatedAt;

    // Additional fields for display
    private String patientName; // When joining with Patient table

    // Constructors
    public MedicalRecord() {}

    public MedicalRecord(long patientId, String symptoms, String diagnosis,
                         String treatment, String doctorName) {
        this.patientId = patientId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.doctorName = doctorName;
    }

    public MedicalRecord(long id, long patientId, String visitDate, String symptoms,
                         String diagnosis, String treatment, String doctorName,
                         String doctorSpecialty, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.visitDate = visitDate;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.notes = notes;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialty() { return doctorSpecialty; }
    public void setDoctorSpecialty(String doctorSpecialty) { this.doctorSpecialty = doctorSpecialty; }

    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    // Utility methods
    public String getDoctorDisplayName() {
        if (doctorName != null && doctorSpecialty != null) {
            return doctorName + " - " + doctorSpecialty;
        }
        return doctorName != null ? doctorName : "Unknown Doctor";
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", visitDate='" + visitDate + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", doctorName='" + doctorName + '\'' +
                '}';
    }
}
