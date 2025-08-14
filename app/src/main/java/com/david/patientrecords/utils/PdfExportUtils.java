package com.david.patientrecords.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import com.david.patientrecords.models.MedicalRecord;
import com.david.patientrecords.models.Medication;
import com.david.patientrecords.models.Patient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfExportUtils {

    private static final String TAG = "PdfExportUtils";
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 50;
    private static final int LINE_HEIGHT = 20;

    public static class ExportResult {
        public boolean success;
        public String filePath;
        public String errorMessage;

        public ExportResult(boolean success, String filePath, String errorMessage) {
            this.success = success;
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }
    }

    public static ExportResult exportPatientToPdf(Context context, Patient patient,
                                                  List<MedicalRecord> medicalRecords,
                                                  List<Medication> medications) {
        try {
            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();

            // Create page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Setup paint for text
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(24);
            titlePaint.setColor(Color.BLACK);
            titlePaint.setAntiAlias(true);
            titlePaint.setFakeBoldText(true);

            Paint headerPaint = new Paint();
            headerPaint.setTextSize(18);
            headerPaint.setColor(Color.BLACK);
            headerPaint.setAntiAlias(true);
            headerPaint.setFakeBoldText(true);

            Paint normalPaint = new Paint();
            normalPaint.setTextSize(14);
            normalPaint.setColor(Color.BLACK);
            normalPaint.setAntiAlias(true);

            Paint labelPaint = new Paint();
            labelPaint.setTextSize(12);
            labelPaint.setColor(Color.GRAY);
            labelPaint.setAntiAlias(true);

            int currentY = MARGIN + 30;

            // Title
            canvas.drawText("Patient Medical Report", MARGIN, currentY, titlePaint);
            currentY += 40;

            // Patient Information Header
            canvas.drawText("PATIENT INFORMATION", MARGIN, currentY, headerPaint);
            currentY += 30;

            // Patient details
            currentY = drawPatientInfo(canvas, patient, normalPaint, labelPaint, currentY);
            currentY += 30;

            // Medical Records Header
            canvas.drawText("MEDICAL RECORDS", MARGIN, currentY, headerPaint);
            currentY += 30;

            // Medical records
            if (medicalRecords != null && !medicalRecords.isEmpty()) {
                for (MedicalRecord record : medicalRecords) {
                    currentY = drawMedicalRecord(canvas, record, normalPaint, labelPaint, currentY);
                    currentY += 20;

                    // Check if we need a new page
                    if (currentY > PAGE_HEIGHT - 100) {
                        pdfDocument.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create();
                        page = pdfDocument.startPage(pageInfo);
                        canvas = page.getCanvas();
                        currentY = MARGIN + 30;
                    }
                }
            } else {
                canvas.drawText("No medical records found.", MARGIN, currentY, normalPaint);
                currentY += 30;
            }

            // Medications Header
            canvas.drawText("CURRENT MEDICATIONS", MARGIN, currentY, headerPaint);
            currentY += 30;

            // Medications
            if (medications != null && !medications.isEmpty()) {
                for (Medication medication : medications) {
                    currentY = drawMedication(canvas, medication, normalPaint, labelPaint, currentY);
                    currentY += 20;

                    // Check if we need a new page
                    if (currentY > PAGE_HEIGHT - 100) {
                        pdfDocument.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 3).create();
                        page = pdfDocument.startPage(pageInfo);
                        canvas = page.getCanvas();
                        currentY = MARGIN + 30;
                    }
                }
            } else {
                canvas.drawText("No medications found.", MARGIN, currentY, normalPaint);
                currentY += 30;
            }

            // Footer with export date
            currentY = PAGE_HEIGHT - 50;
            canvas.drawText("Report generated on: " + getCurrentDateTime(), MARGIN, currentY, labelPaint);

            // Finish the page
            pdfDocument.finishPage(page);

            // Save to file
            String fileName = "Patient_" + patient.getPatientName().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

            // Save PDF in the app-specific Downloads directory
            File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            // Make sure the directory exists
            if (downloadsDir != null && !downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File pdfFile = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                pdfDocument.writeTo(fos);
                pdfDocument.close();

                Log.d(TAG, "PDF saved to: " + pdfFile.getAbsolutePath());
                return new ExportResult(true, pdfFile.getAbsolutePath(), null);
            } catch (IOException e) {
                Log.e(TAG, "Error saving PDF", e);
                return new ExportResult(false, null, "Error saving PDF: " + e.getMessage());
            }



        } catch (Exception e) {
            Log.e(TAG, "Unexpected error creating PDF", e);
            return new ExportResult(false, null, "Unexpected error: " + e.getMessage());
        }
    }

    private static int drawPatientInfo(Canvas canvas, Patient patient, Paint normalPaint, Paint labelPaint, int startY) {
        int currentY = startY;
        int labelX = MARGIN;
        int valueX = MARGIN + 150;

        // Name
        canvas.drawText("Name:", labelX, currentY, labelPaint);
        canvas.drawText(patient.getPatientName(), valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Age and Gender
        canvas.drawText("Age:", labelX, currentY, labelPaint);
        canvas.drawText(patient.getAge() + " years", valueX, currentY, normalPaint);
        canvas.drawText("Gender:", labelX + 250, currentY, labelPaint);
        canvas.drawText(patient.getGender() != null ? patient.getGender() : "Unknown", labelX + 350, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Blood Type
        canvas.drawText("Blood Type:", labelX, currentY, labelPaint);
        canvas.drawText(patient.getBloodType() != null ? patient.getBloodType() : "Unknown", valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Phone
        canvas.drawText("Phone:", labelX, currentY, labelPaint);
        canvas.drawText(patient.getPhone() != null ? patient.getPhone() : "No phone", valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Address
        canvas.drawText("Address:", labelX, currentY, labelPaint);
        String address = patient.getAddress() != null ? patient.getAddress() : "No address";
        if (address.length() > 50) {
            address = address.substring(0, 47) + "...";
        }
        canvas.drawText(address, valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Medical Conditions
        if (patient.getMedicalConditions() != null && !patient.getMedicalConditions().isEmpty()) {
            canvas.drawText("Conditions:", labelX, currentY, labelPaint);
            String conditions = patient.getMedicalConditions();
            if (conditions.length() > 50) {
                conditions = conditions.substring(0, 47) + "...";
            }
            canvas.drawText(conditions, valueX, currentY, normalPaint);
            currentY += LINE_HEIGHT;
        }

        // Allergies
        if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
            canvas.drawText("Allergies:", labelX, currentY, labelPaint);
            String allergies = patient.getAllergies();
            if (allergies.length() > 50) {
                allergies = allergies.substring(0, 47) + "...";
            }
            canvas.drawText(allergies, valueX, currentY, normalPaint);
            currentY += LINE_HEIGHT;
        }

        return currentY;
    }

    private static int drawMedicalRecord(Canvas canvas, MedicalRecord record, Paint normalPaint, Paint labelPaint, int startY) {
        int currentY = startY;
        int labelX = MARGIN;
        int valueX = MARGIN + 120;

        // Visit Date
        canvas.drawText("Visit Date:", labelX, currentY, labelPaint);
        canvas.drawText(DateUtils.formatDateForDisplay(record.getVisitDate()), valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Doctor
        canvas.drawText("Doctor:", labelX, currentY, labelPaint);
        canvas.drawText(record.getDoctorName() != null ? record.getDoctorName() : "Unknown", valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Symptoms
        canvas.drawText("Symptoms:", labelX, currentY, labelPaint);
        String symptoms = record.getSymptoms() != null ? record.getSymptoms() : "None recorded";
        if (symptoms.length() > 60) {
            symptoms = symptoms.substring(0, 57) + "...";
        }
        canvas.drawText(symptoms, valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Diagnosis
        canvas.drawText("Diagnosis:", labelX, currentY, labelPaint);
        String diagnosis = record.getDiagnosis() != null ? record.getDiagnosis() : "None recorded";
        if (diagnosis.length() > 60) {
            diagnosis = diagnosis.substring(0, 57) + "...";
        }
        canvas.drawText(diagnosis, valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Treatment
        canvas.drawText("Treatment:", labelX, currentY, labelPaint);
        String treatment = record.getTreatment() != null ? record.getTreatment() : "None recorded";
        if (treatment.length() > 60) {
            treatment = treatment.substring(0, 57) + "...";
        }
        canvas.drawText(treatment, valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        return currentY;
    }

    private static int drawMedication(Canvas canvas, Medication medication, Paint normalPaint, Paint labelPaint, int startY) {
        int currentY = startY;
        int labelX = MARGIN;
        int valueX = MARGIN + 120;

        // Medication Name
        canvas.drawText("Medication:", labelX, currentY, labelPaint);
        canvas.drawText(medication.getMedicationName(), valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Dosage and Frequency
        canvas.drawText("Dosage:", labelX, currentY, labelPaint);
        String dosageInfo = medication.getDosage() + " - " + medication.getFrequency();
        canvas.drawText(dosageInfo, valueX, currentY, normalPaint);
        currentY += LINE_HEIGHT;

        // Instructions
        if (medication.getInstructions() != null && !medication.getInstructions().isEmpty()) {
            canvas.drawText("Instructions:", labelX, currentY, labelPaint);
            String instructions = medication.getInstructions();
            if (instructions.length() > 60) {
                instructions = instructions.substring(0, 57) + "...";
            }
            canvas.drawText(instructions, valueX, currentY, normalPaint);
            currentY += LINE_HEIGHT;
        }

        return currentY;
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}