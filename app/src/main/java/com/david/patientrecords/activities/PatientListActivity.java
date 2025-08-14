package com.david.patientrecords.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.david.patientrecords.R;

public class PatientListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create simple placeholder layout
        TextView textView = new TextView(this);
        textView.setText("Patient List Activity\n\nComing Soon!");
        textView.setTextSize(18);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(50, 100, 50, 100);

        setContentView(textView);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
