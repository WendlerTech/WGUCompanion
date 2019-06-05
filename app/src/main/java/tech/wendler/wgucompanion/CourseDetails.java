package tech.wendler.wgucompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class CourseDetails extends AppCompatActivity {

    private Course selectedCourse = null, updatedCourse;
    private EditText txtCourseTitle, txtStartMonth, txtStartYear, txtEndMonth, txtEndYear,
            txtCourseInfo;
    private Spinner courseStatusSpinner, courseMentorSpinner;
    private DatabaseHelper databaseHelper;
    private ConstraintLayout parentLayout;
    private boolean mentorDataEntered = false;
    private ArrayAdapter<Mentor> mentorListAdapter;
    private ArrayAdapter<CharSequence> courseStatusAdapter;
    private ArrayList<Mentor> mentorList;
    private Mentor selectedMentor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        Button btnEditMentorInfo, btnViewNotes, btnAssessments, btnAlertSettings, btnSubmit;
        setActionBarTitle("View Course Details");

        Bundle selectedCourseBundle = this.getIntent().getExtras();
        if (selectedCourseBundle != null) {
            this.selectedCourse = (Course) selectedCourseBundle.getSerializable("selectedCourse");
        }

        databaseHelper = new DatabaseHelper(this);
        parentLayout = findViewById(R.id.viewCourseLayout);
        txtCourseTitle = findViewById(R.id.txtViewCourseTitle);
        txtStartMonth = findViewById(R.id.txtViewCourseStartMonth);
        txtStartYear = findViewById(R.id.txtViewCourseStartYear);
        txtEndMonth = findViewById(R.id.txtViewCourseEndMonth);
        txtEndYear = findViewById(R.id.txtViewCourseEndYear);
        txtCourseInfo = findViewById(R.id.txtViewCourseInfo);
        courseStatusSpinner = findViewById(R.id.viewCourseStatusSpinner);
        courseMentorSpinner = findViewById(R.id.viewCourseMentorSpinner);
        btnEditMentorInfo = findViewById(R.id.btnViewEditMentorInfo);
        btnViewNotes = findViewById(R.id.btnViewCourseNotes);
        btnAssessments = findViewById(R.id.btnViewCourseAssessments);
        btnAlertSettings = findViewById(R.id.btnViewCourseAlerts);
        btnSubmit = findViewById(R.id.btnViewCourseSaveChanges);

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
        handleDateTextWatchers();

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
                Mentor selectedMentor = (Mentor) courseMentorSpinner.getSelectedItem();
                if (txtCourseTitle.getText().toString().length() > 0 &&
                        txtStartMonth.getText().toString().length() > 0 &&
                        txtStartYear.getText().toString().length() > 0 &&
                        txtEndMonth.getText().toString().length() > 0 &&
                        txtEndYear.getText().toString().length() > 0 &&
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
                    updatedCourse.setStartDate(txtStartMonth.getText().toString() +
                            "-" + txtStartYear.getText().toString());
                    updatedCourse.setEndDate(txtEndMonth.getText().toString() +
                            "-" + txtEndYear.getText().toString());

                    databaseHelper.updateCourse(updatedCourse);
                    Toast.makeText(getApplicationContext(), "Course updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ViewTerms.class);
                    startActivity(intent);
                } else {
                    //Adds error messages highlighting blank fields
                    if (txtCourseTitle.getText().toString().length() == 0) {
                        txtCourseTitle.setError("Can't be blank.");
                    }
                    if (txtStartMonth.getText().toString().length() == 0) {
                        txtStartMonth.setError("Can't be blank.");
                    }
                    if (txtStartYear.getText().toString().length() == 0) {
                        txtStartYear.setError("Can't be blank.");
                    } else if (txtStartYear.getText().toString().length() < 4) {
                        txtStartYear.setError("Please enter a 4 digit year.");
                    }
                    if (txtEndMonth.getText().toString().length() == 0) {
                        txtEndMonth.setError("Can't be blank.");
                    }
                    if (txtEndYear.getText().toString().length() == 0) {
                        txtEndYear.setError("Can't be blank.");
                    } else if (txtEndYear.getText().toString().length() < 4) {
                        txtEndYear.setError("Please enter a 4 digit year.");
                    }
                    if (txtCourseInfo.getText().toString().length() == 0) {
                        txtCourseInfo.setError("Can't be blank.");
                    }
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
            String startDate = selectedCourse.getStartDate();
            String endDate = selectedCourse.getEndDate();
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

            for (Mentor selectedMentor : mentorList) {
                if (selectedMentor.getMentorID() == selectedCourse.getMentorID()) {
                    courseMentorSpinner.setSelection(mentorListAdapter.getPosition(selectedMentor));
                    this.selectedMentor = selectedMentor;
                }
            }
            txtCourseTitle.setText(selectedCourse.getCourseTitle());
            txtStartMonth.setText(startMonth);
            txtStartYear.setText(startYear);
            txtEndMonth.setText(endMonth);
            txtEndYear.setText(endYear);
            txtCourseInfo.setText(selectedCourse.getCourseInfo());
            courseStatusSpinner.setSelection(courseStatusAdapter.getPosition(selectedCourse.getCourseStatus()));
        }
    }

    private void handleDateTextWatchers() {
        txtStartMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtStartMonth.getText().toString().length() > 2) {
                    txtStartMonth.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtStartMonth.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtStartMonth.getText().toString()) > 12) {
                        txtStartMonth.setText("");
                    } else if (Integer.parseInt(txtStartMonth.getText().toString()) == 0) {
                        txtStartMonth.setText("");
                    }

                }
            }
        });

        txtStartYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtStartYear.getText().toString().length() > 4) {
                    txtStartYear.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtStartYear.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtStartYear.getText().toString()) > 2050) {
                        txtStartYear.setText("");
                    }
                }
            }
        });

        txtEndMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtEndMonth.getText().toString().length() > 2) {
                    txtEndMonth.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtEndMonth.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtEndMonth.getText().toString()) > 12) {
                        txtEndMonth.setText("");
                    } else if (Integer.parseInt(txtEndMonth.getText().toString()) == 0) {
                        txtEndMonth.setText("");
                    }

                }
            }
        });

        txtEndYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtEndYear.getText().toString().length() > 4) {
                    txtEndYear.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtEndYear.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtEndYear.getText().toString()) > 2050) {
                        txtEndYear.setText("");
                    }
                }
            }
        });
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
}
