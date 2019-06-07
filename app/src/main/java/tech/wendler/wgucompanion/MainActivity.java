package tech.wendler.wgucompanion;
/*

    Student scheduler app, made by Nick Wendler (Marcos Nicolas Wendler) from 5/16/2019-6/6/2019.
    App created as a school project while attending WGU; Mobile App Development - C196
    Website: https://wendler.tech/

 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private String studentName;
    private String studentDegree;
    private boolean nameEntered = false;
    private boolean degreeEntered = false;
    private boolean endDateAfterStart = false;
    private TextView lblWelcomeName, lblWelcomeDegree;
    private DatabaseHelper databaseHelper;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        createNotificationChannel();

        databaseHelper = new DatabaseHelper(this);
        lblWelcomeName = findViewById(R.id.lblWelcomeName);
        lblWelcomeDegree = findViewById(R.id.lblUserDegree);
        Button btnNewTerm = findViewById(R.id.btnNewTerm);

        btnNewTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewTermDialog();
            }
        });


        sp = getSharedPreferences("FirstTimeInApp", Context.MODE_PRIVATE);
        boolean initialDataEntered = sp.getBoolean("isInitialDataEntered", false);


        if (!initialDataEntered) {
            showStudentInfoDialog();
        }

        if (initialDataEntered) {
            Student student = databaseHelper.getStudentData();
            String welcomeLabel = "Welcome, " + capitalizeFirstLetter(student.getStudentName());
            String degreeLabel = "Degree: " + student.getStudentDegree();
            lblWelcomeName.setText(welcomeLabel);
            lblWelcomeDegree.setText(degreeLabel);
        }

        lblWelcomeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStudentInfoDialog();
            }
        });

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });
                } else {
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });
    }

    /*
    This 350+ line monster is here for the sake of course requirements wanting
    both declarative & programmatic methods to create the user interface.
     */
    private void showNewTermDialog() {
        final AlertDialog.Builder newTermDialog = new AlertDialog.Builder(this);
        TextView lblTermTitle = new TextView(this);
        TextView lblStartDate = new TextView(this);
        TextView lblEndDate = new TextView(this);
        final EditText txtTermTitle = new EditText(this);
        final EditText txtMonthStart = new EditText(this);
        final EditText txtYearStart = new EditText(this);
        final EditText txtMonthEnd = new EditText(this);
        final EditText txtYearEnd = new EditText(this);

        lblTermTitle.setPadding(0, 60, 40, 0);
        lblStartDate.setPadding(0, 40, 0, 0);
        lblEndDate.setPadding(0, 40, 0, 0);

        lblTermTitle.setText(R.string.new_term_dialog_title);
        lblStartDate.setText(R.string.new_term_dialog_start_date);
        lblEndDate.setText(R.string.new_term_dialog_end_date);

        txtMonthStart.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtYearStart.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtMonthEnd.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtYearEnd.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtTermTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        txtMonthStart.setHint("MM");
        txtYearStart.setHint("YYYY");
        txtMonthEnd.setHint("MM");
        txtYearEnd.setHint("YYYY");

        //Start date text fields
        LinearLayout startDateLayout = new LinearLayout(this);
        startDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        startDateLayout.addView(txtMonthStart);
        startDateLayout.addView(txtYearStart);

        //End date text fields
        LinearLayout endDateLayout = new LinearLayout(this);
        endDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        endDateLayout.addView(txtMonthEnd);
        endDateLayout.addView(txtYearEnd);

        //Makes the soft keyboard next button work as intended
        txtTermTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        ScrollView scrollViewLayout = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(lblTermTitle);
        dialogLayout.addView(txtTermTitle);
        dialogLayout.addView(lblStartDate);
        dialogLayout.addView(startDateLayout);
        dialogLayout.addView(lblEndDate);
        dialogLayout.addView(endDateLayout);

        dialogLayout.setPadding(40, 0, 40, 0);

        scrollViewLayout.addView(dialogLayout);
        newTermDialog.setView(scrollViewLayout);

        newTermDialog.setTitle(R.string.new_term_dialog);

        newTermDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String termTitle, termStartDate, termEndDate;

                if (Integer.parseInt(txtYearStart.getText().toString()) < 1000 ||
                        Integer.parseInt(txtYearEnd.getText().toString()) < 1000) {
                    Toast.makeText(getApplicationContext(), "Please enter a 4 digit year.", Toast.LENGTH_SHORT).show();
                } else {
                    termStartDate = txtMonthStart.getText().toString() + "-"
                            + txtYearStart.getText().toString();
                    termEndDate = txtMonthEnd.getText().toString() + "-"
                            + txtYearEnd.getText().toString();

                    termTitle = txtTermTitle.getText().toString();

                    if (databaseHelper.addNewTerm(termTitle, termStartDate, termEndDate) != -1) {
                        Toast.makeText(getApplicationContext(), "Term added successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ViewTerms.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error saving term data.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        newTermDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Do nothing
            }
        });

        final AlertDialog alert = newTermDialog.create();

        /*
        Start date text watchers keep dates within bounds & ensures end date is after start date
         */
        txtMonthStart.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtMonthStart.getText().toString().length() > 2) {
                    txtMonthStart.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtMonthStart.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtMonthStart.getText().toString()) > 12) {
                        txtMonthStart.setText("");
                    } else if (Integer.parseInt(txtMonthStart.getText().toString()) == 0) {
                        txtMonthStart.setText("");
                    }
                    if (txtMonthEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0 &&
                            txtYearEnd.getText().toString().length() > 0 &&
                            txtMonthStart.getText().toString().length() > 0) {
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                        //Disables save button if start year is after the end year
                        else if (Integer.parseInt(txtYearStart.getText().toString()) <
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = true;
                        } else {
                            endDateAfterStart = false;
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtYearStart.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtYearStart.getText().toString().length() > 4) {
                    txtYearStart.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtYearStart.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtYearStart.getText().toString()) > 2050) {
                        txtYearStart.setText("");
                    }
                    if (txtYearEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0) {
                        //Disables save button if start year is after the end year
                        if (Integer.parseInt(txtYearStart.getText().toString()) <
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = true;
                        } else {
                            endDateAfterStart = false;
                        }
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        /*
        End date text watchers keep dates within bounds & ensures end date is after start date
         */
        txtMonthEnd.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtMonthEnd.getText().toString().length() > 2) {
                    txtMonthEnd.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtMonthEnd.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtMonthEnd.getText().toString()) > 12) {
                        txtMonthEnd.setText("");
                    } else if (Integer.parseInt(txtMonthEnd.getText().toString()) == 0) {
                        txtMonthEnd.setText("");
                    }
                    if (txtMonthStart.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0 &&
                            txtYearEnd.getText().toString().length() > 0 &&
                            txtMonthEnd.getText().toString().length() > 0) {
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtYearEnd.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtYearEnd.getText().toString().length() > 4) {
                    txtYearEnd.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtYearEnd.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtYearEnd.getText().toString()) > 2050) {
                        txtYearEnd.setText("");
                    }
                    if (txtYearEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0) {
                        //Disables save button if start year is after the end year
                        if (Integer.parseInt(txtYearStart.getText().toString()) >
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = false;
                        } else {
                            endDateAfterStart = true;
                        }
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtTermTitle.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handleSubmitButton();
            }
        });

        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void showStudentInfoDialog() {
        //Creates & shows introduction dialog upon first open
        final AlertDialog.Builder introDialog = new AlertDialog.Builder(this);
        TextView lblMessage = new TextView(this);
        TextView lblName = new TextView(this);
        TextView lblDegree = new TextView(this);
        final EditText txtName = new EditText(this);
        final EditText txtDegree = new EditText(this);

        lblMessage.setText(R.string.intro_dialog_message);
        lblName.setText(R.string.intro_dialog_name_label);
        lblDegree.setText(R.string.intro_dialog_degree_label);
        txtName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        txtDegree.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        lblMessage.setPadding(30, 40, 30, 40);
        lblName.setPadding(0, 40, 0, 0);
        lblDegree.setPadding(0, 40, 0, 0);

        ScrollView scrollViewLayout = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(lblMessage);
        dialogLayout.addView(lblName);
        dialogLayout.addView(txtName);
        dialogLayout.addView(lblDegree);
        dialogLayout.addView(txtDegree);

        //Layout padding also shifts edit text fields
        dialogLayout.setPadding(40, 0, 40, 0);

        scrollViewLayout.addView(dialogLayout);
        introDialog.setView(scrollViewLayout);

        introDialog.setTitle(R.string.intro_dialog_title);

        introDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isInitialDataEntered", true);
                editor.apply();

                studentName = txtName.getText().toString();
                studentDegree = txtDegree.getText().toString();

                if (databaseHelper.addStudentData(studentName, studentDegree)) {
                    Toast.makeText(getApplicationContext(), "Tap your name to edit this again.", Toast.LENGTH_LONG).show();

                    String welcomeLabel = "Welcome, " + capitalizeFirstLetter(studentName);
                    String degreeLabel = "Degree: " + studentDegree;
                    lblWelcomeName.setText(welcomeLabel);
                    lblWelcomeDegree.setText(degreeLabel);
                }
            }
        });

        final AlertDialog alert = introDialog.create();

        //Text watchers enable/disable submit button if fields are left empty
        txtName.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (nameEntered && degreeEntered) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Returns true if length > 1
                nameEntered = txtName.getText().length() > 1;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtName.getText().toString().length() == 0) {
                    nameEntered = false;
                }
                handleSubmitButton();
            }
        });

        txtDegree.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (nameEntered && degreeEntered) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Returns true if length > 2
                degreeEntered = txtDegree.getText().toString().length() > 1;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtDegree.getText().toString().length() == 0) {
                    degreeEntered = false;
                }
                handleSubmitButton();
            }
        });

        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alert.setCancelable(false);

    }

    public String capitalizeFirstLetter(String original) {
        //Capitalizes first letter of a string
        if (original == null || original.length() == 0) {
            return original;
        } else {
            original = original.toLowerCase();
            return original.substring(0, 1).toUpperCase() + original.substring(1);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_terms:
                intent = new Intent(this, ViewTerms.class);
                startActivity(intent);
                break;
            case R.id.nav_courses:
                intent = new Intent(this, ViewCourses.class);
                startActivity(intent);
                break;
            case R.id.nav_assessments:
                intent = new Intent(this, ViewAssessments.class);
                startActivity(intent);
                break;
            case R.id.nav_mentor:
                intent = new Intent(this, ViewMentor.class);
                startActivity(intent);
                break;
        }
        //Returning false instead of true de-highlights option on back press
        return false;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("CourseNotifyID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        //Hides nav drawer on back button press
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
