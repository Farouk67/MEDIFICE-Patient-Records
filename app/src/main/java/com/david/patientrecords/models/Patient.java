package com.david.patientrecords.models;

public class Patient {
    private long id;
    private String patientName;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String bloodType;
    private String emergencyContact;
    private String emergencyPhone;
    private String medicalConditions;
    private String allergies;
    private String profileImage; // Keep for backward compatibility
    private String imagePath; // New field for image paths
    private String registrationDate;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    // Default constructor
    public Patient() {
        this.isActive = true;
    }

    // Constructor for new patient (without ID)
    public Patient(String patientName, int age, String gender, String bloodType,
                   String phone, String address, String emergencyContact, String emergencyPhone,
                   String medicalConditions, String allergies, String registrationDate, String imagePath) {
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.bloodType = bloodType;
        this.phone = phone;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.medicalConditions = medicalConditions;
        this.allergies = allergies;
        this.registrationDate = registrationDate;
        this.imagePath = imagePath;
        this.isActive = true;
    }

    // Constructor for existing patient (with ID)
    public Patient(long id, String patientName, int age, String gender, String bloodType,
                   String phone, String address, String emergencyContact, String emergencyPhone,
                   String medicalConditions, String allergies, String registrationDate, String imagePath) {
        this.id = id;
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.bloodType = bloodType;
        this.phone = phone;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.medicalConditions = medicalConditions;
        this.allergies = allergies;
        this.registrationDate = registrationDate;
        this.imagePath = imagePath;
        this.isActive = true;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    // Alias for compatibility with AddEditPatientActivity
    public String getName() {
        return patientName;
    }

    public void setName(String name) {
        this.patientName = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // New image path methods
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", patientName='" + patientName + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", phone='" + phone + '\'' +
                ", registrationDate='" + registrationDate + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}