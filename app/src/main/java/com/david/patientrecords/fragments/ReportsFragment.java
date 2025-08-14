package com.david.patientrecords.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ReportsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText("Reports & Analytics\n\nComing Soon!");
        textView.setTextSize(18);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(50, 100, 50, 100);
        return textView;
    }
}