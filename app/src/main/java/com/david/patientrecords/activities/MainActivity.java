package com.david.patientrecords.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.david.patientrecords.R;
import com.david.patientrecords.database.PatientRepository;
import com.david.patientrecords.fragments.DashboardFragment;
import com.david.patientrecords.fragments.PatientsFragment;
import com.david.patientrecords.fragments.ReportsFragment;
import com.david.patientrecords.utils.Constants;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton fab;

    // Data
    private PatientRepository patientRepository;
    private String currentFragmentTag = "dashboard";

    // Fragment tags
    private static final String FRAGMENT_DASHBOARD = "dashboard";
    private static final String FRAGMENT_PATIENTS = "patients";
    private static final String FRAGMENT_REPORTS = "reports";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository
        patientRepository = PatientRepository.getInstance(this);

        // Initialize UI components
        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFloatingActionButton();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), FRAGMENT_DASHBOARD, "Dashboard");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Dashboard");
        }
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // You can add custom behavior here
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // You can add custom behavior here
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Set default selected item
        navigationView.setCheckedItem(R.id.nav_dashboard);
    }

    private void setupFloatingActionButton() {
        fab.setOnClickListener(v -> {
            // Navigate to Add Patient Activity based on current fragment
            switch (currentFragmentTag) {
                case FRAGMENT_PATIENTS:
                    Intent addPatientIntent = new Intent(MainActivity.this, AddEditPatientActivity.class);
                    addPatientIntent.putExtra(Constants.EXTRA_IS_EDIT_MODE, false);
                    startActivityForResult(addPatientIntent, Constants.REQUEST_ADD_PATIENT);
                    break;
                case FRAGMENT_DASHBOARD:
                    // Show quick action dialog or navigate to patients
                    showQuickActionDialog();
                    break;
                default:
                    // Default action - add patient
                    Intent defaultIntent = new Intent(MainActivity.this, AddEditPatientActivity.class);
                    defaultIntent.putExtra(Constants.EXTRA_IS_EDIT_MODE, false);
                    startActivityForResult(defaultIntent, Constants.REQUEST_ADD_PATIENT);
                    break;
            }
        });

        // Set initial FAB icon based on dashboard
        fab.setImageResource(R.drawable.ic_add);
    }

    private void showQuickActionDialog() {
        // Create a simple dialog with quick actions
        String[] options = {"Add Patient", "View All Patients", "Reports"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Quick Actions")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Add Patient
                            Intent addIntent = new Intent(MainActivity.this, AddEditPatientActivity.class);
                            addIntent.putExtra(Constants.EXTRA_IS_EDIT_MODE, false);
                            startActivityForResult(addIntent, Constants.REQUEST_ADD_PATIENT);
                            break;
                        case 1: // View All Patients
                            loadFragment(new PatientsFragment(), FRAGMENT_PATIENTS, "Patients");
                            navigationView.setCheckedItem(R.id.nav_patients);
                            break;
                        case 2: // Reports
                            loadFragment(new ReportsFragment(), FRAGMENT_REPORTS, "Reports");
                            navigationView.setCheckedItem(R.id.nav_reports);
                            break;
                    }
                })
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_dashboard) {
            loadFragment(new DashboardFragment(), FRAGMENT_DASHBOARD, "Dashboard");
            updateFabIcon(R.drawable.ic_add);

        } else if (itemId == R.id.nav_patients) {
            loadFragment(new PatientsFragment(), FRAGMENT_PATIENTS, "Patients");
            updateFabIcon(R.drawable.ic_person_add);

        } else if (itemId == R.id.nav_patient_list) {
            // Try to launch PatientListActivity, fallback to patients fragment
            try {
                Intent intent = new Intent(this, PatientListActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to patients fragment if activity doesn't exist
                loadFragment(new PatientsFragment(), FRAGMENT_PATIENTS, "All Patients");
                updateFabIcon(R.drawable.ic_person_add);
            }

        } else if (itemId == R.id.nav_add_patient) {
            Intent intent = new Intent(this, AddEditPatientActivity.class);
            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, false);
            startActivityForResult(intent, Constants.REQUEST_ADD_PATIENT);

        } else if (itemId == R.id.nav_medical_records) {
            // Try to launch MedicalRecordActivity, show coming soon if not available
            try {
                Intent intent = new Intent(this, MedicalRecordActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Medical Records feature coming soon!", Toast.LENGTH_SHORT).show();
            }

        } else if (itemId == R.id.nav_medications) {
            // Try to launch MedicationActivity, show coming soon if not available
            try {
                Intent intent = new Intent(this, MedicationActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Medications feature coming soon!", Toast.LENGTH_SHORT).show();
            }

        } else if (itemId == R.id.nav_reports) {
            loadFragment(new ReportsFragment(), FRAGMENT_REPORTS, "Reports");
            updateFabIcon(R.drawable.ic_analytics);

        } else if (itemId == R.id.nav_settings) {
            showSettingsDialog();

        } else if (itemId == R.id.nav_about) {
            showAboutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String tag, String title) {
        currentFragmentTag = tag;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void updateFabIcon(int iconResource) {
        fab.setImageResource(iconResource);
    }

    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Settings")
                .setMessage("Settings functionality will be implemented in future updates.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("About Patient Records")
                .setMessage("Patient Records App v1.0\n\nA comprehensive healthcare management system for tracking patient information, medical records, and medications.\n\nDeveloped for healthcare professionals.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            // Navigate to patients fragment and focus search
            loadFragment(new PatientsFragment(), FRAGMENT_PATIENTS, "Search Patients");
            navigationView.setCheckedItem(R.id.nav_patients);
            updateFabIcon(R.drawable.ic_person_add);
            return true;

        } else if (itemId == R.id.action_notifications) {
            showNotificationsDialog();
            return true;

        } else if (itemId == R.id.action_backup) {
            showBackupDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNotificationsDialog() {
        // Get upcoming follow-ups and expiring medications
        new Thread(() -> {
            try {
                int[] stats = patientRepository.getDashboardStats();

                runOnUiThread(() -> {
                    String message = "📊 Current Status:\n\n" +
                            "👥 Total Patients: " + stats[0] + "\n" +
                            "📋 Medical Records: " + stats[1] + "\n" +
                            "💊 Active Medications: " + stats[2] + "\n\n" +
                            "No urgent notifications at this time.";

                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                    builder.setTitle("Notifications")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showBackupDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Backup Data")
                .setMessage("Backup functionality will be implemented in future updates. Your data is automatically saved locally.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_ADD_PATIENT:
                    Toast.makeText(this, Constants.SUCCESS_PATIENT_ADDED, Toast.LENGTH_SHORT).show();
                    refreshCurrentFragment();
                    break;
                case Constants.REQUEST_EDIT_PATIENT:
                    Toast.makeText(this, Constants.SUCCESS_PATIENT_UPDATED, Toast.LENGTH_SHORT).show();
                    refreshCurrentFragment();
                    break;
            }
        }
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        if (currentFragment != null) {
            if (currentFragment instanceof DashboardFragment) {
                ((DashboardFragment) currentFragment).refreshData();
            } else if (currentFragment instanceof PatientsFragment) {
                ((PatientsFragment) currentFragment).refreshData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // If we're not on dashboard, go back to dashboard
            if (!currentFragmentTag.equals(FRAGMENT_DASHBOARD)) {
                loadFragment(new DashboardFragment(), FRAGMENT_DASHBOARD, "Dashboard");
                navigationView.setCheckedItem(R.id.nav_dashboard);
                updateFabIcon(R.drawable.ic_add);
            } else {
                // Show exit confirmation
                showExitConfirmationDialog();
            }
        }
    }

    private void showExitConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Exit App")
                .setMessage("Are you sure you want to exit Patient Records?")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh current fragment data when returning to activity
        refreshCurrentFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connections
        if (patientRepository != null) {
            patientRepository.close();
        }
    }

    // Helper method to get current fragment
    public String getCurrentFragmentTag() {
        return currentFragmentTag;
    }

    /**
     * Method to programmatically navigate to specific fragments
     * Called from DashboardFragment when user clicks on cards
     */
    public void navigateToFragment(String fragmentTag) {
        switch (fragmentTag) {
            case "dashboard":
                loadFragment(new DashboardFragment(), "dashboard", "Dashboard");
                navigationView.setCheckedItem(R.id.nav_dashboard);
                break;

            case "patients":
                loadFragment(new PatientsFragment(), "patients", "Patients");
                navigationView.setCheckedItem(R.id.nav_patients);
                break;

            case "reports":
                // Create a simple placeholder fragment for now
                Fragment reportsFragment = new Fragment() {
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                        View view = inflater.inflate(android.R.layout.simple_list_item_1, container, false);
                        TextView textView = view.findViewById(android.R.id.text1);
                        textView.setText("Reports feature coming soon!");
                        textView.setGravity(android.view.Gravity.CENTER);
                        textView.setTextSize(18);
                        return view;
                    }
                };
                loadFragment(reportsFragment, "reports", "Reports");
                navigationView.setCheckedItem(R.id.nav_reports);
                break;

            default:
                // Default to dashboard
                loadFragment(new DashboardFragment(), "dashboard", "Dashboard");
                navigationView.setCheckedItem(R.id.nav_dashboard);
                break;
        }

        // Close drawer after navigation
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    }