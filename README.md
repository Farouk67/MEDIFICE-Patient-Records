# MEDIFICE - Professional Patient Records Management System

<div align="center">
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
  [![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://java.com)
  [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
  [![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
</div>

## 📱 Overview

MEDIFICE is a comprehensive Patient Records Management System developed for Android using Java and Android Studio. The application provides healthcare professionals with digital tools for patient information management, medical record keeping, and medication tracking. This project was developed as part of the Mobile Platforms and Application Development course (7052CEM) at Coventry University.

## ✨ Features

### 🏥 Patient Management
- **Complete Patient Registration** - Demographics, medical history, emergency contacts
- **Advanced Search & Filtering** - Multi-criteria patient discovery with real-time results
- **Profile Photo Integration** - Camera and gallery support with secure storage
- **Expandable Patient Cards** - Progressive disclosure design with statistics
- **Contact Management** - Emergency contact information and communication tools

### 📋 Medical Records
- **Structured Documentation** - Visit tracking, symptoms, diagnosis, treatment
- **Doctor Information Management** - Provider details and specializations
- **Medical History Timeline** - Chronological care progression
- **Follow-up Scheduling** - Appointment coordination and reminders
- **Clinical Notes** - Comprehensive documentation with rich text support

### 💊 Medication Management
- **Prescription Tracking** - Complete medication profiles with dosage information
- **Drug Safety Monitoring** - Allergy alerts and interaction warnings
- **Pharmacy Integration** - Prescription coordination and refill management
- **Medication Adherence** - Start/end date tracking with automated alerts
- **Instructions Management** - Detailed usage guidelines and side effects

### 📊 Dashboard Analytics
- **Real-time Statistics** - Patient counts, gender distribution, system metrics
- **Quick Actions** - Rapid access to frequently used functions
- **System Notifications** - Status updates, alerts, and important messages
- **Activity Tracking** - Recent registrations and medical record updates

## 🛠️ Technical Implementation

### **Architecture**
- **Pattern**: Layered Architecture with Repository Pattern for clean code organization
- **Database**: SQLite with normalized 3-table schema and referential integrity
- **UI Framework**: Material Design 3.0 for professional healthcare interface
- **Image Processing**: Camera integration with automatic optimization and secure storage

### **Technology Stack**
- **Platform**: Android (API Level 21+)
- **Programming Language**: Java
- **Database**: SQLite with foreign key constraints
- **Development Environment**: Android Studio
- **Design System**: Material Design Components
- **Architecture Components**: Repository Pattern, Singleton Pattern

### **Database Schema**
```sql
-- Patients Table (Primary Entity)
CREATE TABLE patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_name TEXT NOT NULL,
    age INTEGER,
    gender TEXT,
    phone TEXT,
    address TEXT,
    blood_type TEXT,
    emergency_contact TEXT,
    emergency_phone TEXT,
    medical_conditions TEXT,
    allergies TEXT,
    image_path TEXT,
    registration_date TEXT,
    is_active INTEGER DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Medical Records Table
CREATE TABLE medical_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    visit_date TEXT,
    visit_type TEXT,
    symptoms TEXT,
    diagnosis TEXT,
    treatment TEXT,
    doctor_name TEXT,
    doctor_specialty TEXT,
    vital_signs TEXT,
    notes TEXT,
    follow_up_date TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(patient_id) REFERENCES patients(id)
);

-- Medications Table
CREATE TABLE medications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    medication_name TEXT NOT NULL,
    generic_name TEXT,
    dosage TEXT,
    frequency TEXT,
    start_date TEXT,
    end_date TEXT,
    prescribed_by TEXT,
    instructions TEXT,
    side_effects TEXT,
    refills_remaining INTEGER DEFAULT 0,
    pharmacy_name TEXT,
    is_active INTEGER DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(patient_id) REFERENCES patients(id)
);

MIT License

Copyright (c) 2025 Farouk Bababunmi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
