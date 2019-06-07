package tech.wendler.wgucompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Locale;

public class CourseDetails extends AppCompatActivity {

    private EditText txtCourseTitle, txtCourseInfo;
    private TextView lblStartDate, lblEndDate;
    private Spinner courseStatusSpinner, courseMentorSpinner;
    private ConstraintLayout parentLayout;
    private Button btnStartDate, btnEndDate;

    private Calendar selectedStartCal, selectedEndCal;
    private DatePickerDialog.OnDateSetListener startDateSetListener, endDateSetListener;

    private ArrayAdapter<Mentor> mentorListAdapter;
    private ArrayAdapter<CharSequence> courseStatusAdapter;
    private ArrayList<Mentor> mentorList;
    private Term selectedTerm;
    private Mentor selectedMentor;
    private Course selectedCourse = null, updatedCourse;
    private DatabaseHelper databaseHelper;

    private boolean mentorDataEntered = false, startDateSelected = false, endDateSelected = false,
            termDatesAvailable = true;
    private int termStartMonth, termStartYear, termEndMonth, termEndYear;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        setActionBarTitle("View Course Details");

        Bundle selectedCourseBundle = this.getIntent().getExtras();
        if (selectedCourseBundle != null) {
            this.selectedCourse = (Course) selectedCourseBundle.getSerializable("selectedCourse");
            this.selectedTerm = (Term) selectedCourseBundle.getSerializable("selectedTerm");
        }

        Button btnEditMentorInfo, btnViewNotes, btnAssessments, btnAlertSettings, btnSubmit;
        databaseHelper = new DatabaseHelper(this);
        parentLayout = findViewById(R.id.viewCourseLayout);
        txtCourseTitle = findViewById(R.id.txtViewCourseTitle);
        txtCourseInfo = findViewById(R.id.txtViewCourseInfo);
        lblStartDate = findViewById(R.id.lblViewCourseStartDate);
        lblEndDate = findViewById(R.id.lblViewCourseEndDate);
        courseStatusSpinner = findViewById(R.id.viewCourseStatusSpinner);
        courseMentorSpinner = findViewById(R.id.viewCourseMentorSpinner);
        btnEditMentorInfo = findViewById(R.id.btnViewEditMentorInfo);
        btnViewNotes = findViewById(R.id.btnViewCourseNotes);
        btnAssessments = findViewById(R.id.btnViewCourseAssessments);
        btnAlertSettings = findViewById(R.id.btnViewCourseAlerts);
        btnSubmit = findViewById(R.id.btnViewCourseSaveChanges);
        btnStartDate = findViewById(R.id.btnViewCourseStartDate);
        btnEndDate = findViewById(R.id.btnViewCourseEndDate);

        //Populates course status spinner
        courseStatusAdapter = ArrayAdapter.createFromResource
                (this, R.array.course_status, R.layout.support_simple_spinner_dropdown_item);
        courseStatusAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        courseStatusSpinner.setAdapter(courseStatusAdapter);

        //Populates spinner with previously entered mentors
        mentorList = databaseHelper.getMentorList();
        mentorListAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, mentorList);
        mentorListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        courseMentorSpinner.setAdapter(mentorListAdapter);

        populateData();

        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();

                startCal.set(Calendar.YEAR, termStartYear);
                startCal.set(Calendar.MONTH, termStartMonth - 1);
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                endCal.set(Calendar.YEAR, termEndYear);
                endCal.set(Calendar.MONTH, termEndMonth - 1);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

                DatePickerDialog dialog = new DatePickerDialog(CourseDetails.this,
                        R.style.CalendarDialogTheme);
                dialog.setOnDateSetListener(startDateSetListener);
                dialog.getDatePicker().setMinDate(startCal.getTimeInMillis());
                dialog.getDatePicker().setMaxDate(endCal.getTimeInMillis());

                dialog.show();
            }
        });

        startDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                selectedStartCal = calendar;
                startDateSelected = true;
                lblStartDate.setText("Start Date: " + formatDate(calendar));
                lblStartDate.setVisibility(View.VISIBLE);

                if (selectedEndCal != null) {
                    if (selectedEndCal.getTimeInMillis() < calendar.getTimeInMillis()) {
                        Toast.makeText(CourseDetails.this, "Start date can't be before end date.",
                                Toast.LENGTH_SHORT).show();
                        selectedStartCal = null;
                        startDateSelected = false;
                        lblStartDate.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };

        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();

                startCal.set(Calendar.YEAR, termStartYear);
                startCal.set(Calendar.MONTH, termStartMonth - 1);
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                endCal.set(Calendar.YEAR, termEndYear);
                endCal.set(Calendar.MONTH, termEndMonth - 1);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

                DatePickerDialog dialog = new DatePickerDialog(CourseDetails.this,
                        R.style.CalendarDialogTheme);
                dialog.setOnDateSetListener(endDateSetListener);
                dialog.getDatePicker().setMinDate(startCal.getTimeInMillis());
                dialog.getDatePicker().setMaxDate(endCal.getTimeInMillis());

                dialog.show();
            }
        });

        endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                selectedEndCal = calendar;
                endDateSelected = true;
                lblEndDate.setText("End Date: " + formatDate(calendar));
                lblEndDate.setVisibility(View.VISIBLE);

                if (selectedStartCal != null) {
                    if (selectedStartCal.getTimeInMillis() > calendar.getTimeInMillis()) {
                        Toast.makeText(CourseDetails.this, "End date can't be after start date.",
                                Toast.LENGTH_SHORT).show();
                        selectedEndCal = null;
                        endDateSelected = false;
                        lblEndDate.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };

        btnEditMentorInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMentor = (Mentor) courseMentorSpinner.getSelectedItem();
                showNewMentorDialog();
            }
        });

        btnViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewNotes.class);
                intent.putExtra("selectedCourse", selectedCourse);
                startActivity(intent);
            }
        });

        btnAlertSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewCourseNotification.class);
                intent.putExtra("selectedCourse", selectedCourse);
                startActivity(intent);
            }
        });

        btnAssessments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewAssessments.class);
                intent.putExtra("selectedCourse", selectedCourse);
                startActivity(intent);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (termDatesAvailable) {
                    Mentor selectedMentor = (Mentor) courseMentorSpinner.getSelectedItem();
                    if ((startDateSelected && !endDateSelected) || (!startDateSelected && endDateSelected)) {
                        Toast.makeText(getApplicationContext(), "If changing dates, " +
                                "please re-select both a start & end date.", Toast.LENGTH_LONG).show();
                    } else {
                        if (txtCourseTitle.getText().toString().length() > 0 &&
                                txtCourseInfo.getText().toString().length() > 0 &&
                                courseStatusSpinner.getSelectedItem() != null &&
                                selectedMentor != null) {
                            updatedCourse = new Course();
                            updatedCourse.setCourseID(selectedCourse.getCourseID());
                            updatedCourse.setMentorID(selectedMentor.getMentorID());
                            updatedCourse.setTermID(selectedCourse.getTermID());
                            updatedCourse.setCourseStatus(courseStatusSpinner.getSelectedItem().toString());
                            updatedCourse.setCourseTitle(txtCourseTitle.getText().toString());
                            updatedCourse.setCourseInfo(txtCourseInfo.getText().toString());
                            if (startDateSelected) {
                                updatedCourse.setStartDate(formatDate(selectedStartCal));
                            } else {
                                updatedCourse.setStartDate(selectedCourse.getStartDate());
                            }
                            if (endDateSelected) {
                                updatedCourse.setEndDate(formatDate(selectedEndCal));
                            } else {
                                updatedCourse.setEndDate(selectedCourse.getEndDate());
                            }

                            databaseHelper.updateCourse(updatedCourse);
                            Toast.makeText(getApplicationContext(), "Course updated successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), ViewTerms.class);
                            startActivity(intent);
                        } else {
                            //Adds error messages highlighting blank fields
                            if (txtCourseTitle.getText().toString().length() == 0) {
                                txtCourseTitle.setError("Can't be blank.");
                            }
                            if (txtCourseInfo.getText().toString().length() == 0) {
                                txtCourseInfo.setError("Can't be blank.");
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "If you want to update course " +
                            "details, you must go through the terms page first.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Allows scrolling within edit text
        txtCourseInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (txtCourseInfo.hasFocus()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

        parentLayout.requestFocus();
    }

    private void populateData() {
        if (selectedCourse == null) {
            Intent intent = new Intent(getApplicationContext(), ViewTerms.class);
            startActivity(intent);
        } else {
            if (selectedTerm == null) {
                btnStartDate.setVisibility(View.INVISIBLE);
                btnEndDate.setVisibility(View.INVISIBLE);
                termDatesAvailable = false;
            } else {
                String startDate = selectedTerm.getStartDate();
                String endDate = selectedTerm.getEndDate();
                String startMonth, startYear, endMonth, endYear;

                if (startDate.length() == 6) {
                    startMonth = startDate.substring(0, 1);
                    startYear = startDate.substring(2);
                } else {
                    startMonth = startDate.substring(0, 2);
                    startYear = startDate.substring(3);
                }

                if (endDate.length() == 6) {
                    endMonth = endDate.substring(0, 1);
                    endYear = endDate.substring(2);
                } else {
                    endMonth = endDate.substring(0, 2);
                    endYear = endDate.substring(3);
                }

                try {
                    this.termStartMonth = Integer.parseInt(startMonth);
                    this.termStartYear = Integer.parseInt(startYear);
                    this.termEndMonth = Integer.parseInt(endMonth);
                    this.termEndYear = Integer.parseInt(endYear);

                } catch (InputMismatchException ignored) {

                }
            }
            for (Mentor selectedMentor : mentorList) {
                if (selectedMentor.getMentorID() == selectedCourse.getMentorID()) {
                    courseMentorSpinner.setSelection(mentorListAdapter.getPosition(selectedMentor));
                    this.selectedMentor = selectedMentor;
                }
            }
            txtCourseTitle.setText(selectedCourse.getCourseTitle());
            lblStartDate.setText("Start Date: " + selectedCourse.getStartDate());
            lblEndDate.setText("End Date: " + selectedCourse.getEndDate());
            txtCourseInfo.setText(selectedCourse.getCourseInfo());
            courseStatusSpinner.setSelection(courseStatusAdapter.getPosition(selectedCourse.getCourseStatus()));
        }
    }

    private void showNewMentorDialog() {
        final AlertDialog.Builder mentorDialog = new AlertDialog.Builder(this);
        TextView lblMentorName = new TextView(this);
        TextView lblMentorEmail = new TextView(this);
        TextView lblMentorPhone = new TextView(this);
        final EditText txtMentorName = new EditText(this);
        final EditText txtMentorEmail = new EditText(this);
        final EditText txtMentorPhone = new EditText(this);

        lblMentorName.setText(R.string.new_mentor_dialog_name);
        lblMentorEmail.setText(R.string.new_mentor_dialog_email);
        lblMentorPhone.setText(R.string.new_mentor_dialog_phone);

        txtMentorName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        txtMentorEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        txtMentorPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        txtMentorPhone.setMaxLines(1);

        lblMentorName.setPadding(0, 40, 0, 0);
        lblMentorEmail.setPadding(0, 40, 0, 0);
        lblMentorPhone.setPadding(0, 40, 0, 0);

        ScrollView scrollViewLayout = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(lblMentorName);
        dialogLayout.addView(txtMentorName);
        dialogLayout.addView(lblMentorEmail);
        dialogLayout.addView(txtMentorEmail);
        dialogLayout.addView(lblMentorPhone);
        dialogLayout.addView(txtMentorPhone);

        //Layout padding also shifts edit text fields
        dialogLayout.setPadding(40, 0, 40, 0);

        scrollViewLayout.addView(dialogLayout);
        mentorDialog.setView(scrollViewLayout);
        mentorDialog.setTitle("View/Update Mentor Information");

        txtMentorName.setText(selectedMentor.getMentorName());
        txtMentorEmail.setText(selectedMentor.getMentorEmail());
        txtMentorPhone.setText(selectedMentor.getMentorPhoneNum());

        mentorDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedMentor.setMentorName(txtMentorName.getText().toString());
                selectedMentor.setMentorEmail(txtMentorEmail.getText().toString());
                selectedMentor.setMentorPhoneNum(txtMentorPhone.getText().toString());

                databaseHelper.updateMentor(selectedMentor);
                mentorDataEntered = true;
                mentorListAdapter.notifyDataSetChanged();
                courseMentorSpinner.setSelection(mentorListAdapter.getPosition(selectedMentor));
                Toast.makeText(getApplicationContext(), "Mentor updated successfully.", Toast.LENGTH_SHORT).show();
                parentLayout.requestFocus(); //Minimizes keyboard after entering new mentor
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //User cancelled, do nothing
                    }
                });

        final AlertDialog alert = mentorDialog.create();

        txtMentorName.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
                handleSubmitButton();
            }
        });

        txtMentorEmail.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
                handleSubmitButton();

            }
        });

        txtMentorPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
                handleSubmitButton();
            }
        });

        alert.show();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }

    //Takes in calendar instance & returns a string formatted as such: 11-2015
    private String formatDate(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat("M-yyyy", Locale.US);
        return format.format(date.getTime());
    }
}
