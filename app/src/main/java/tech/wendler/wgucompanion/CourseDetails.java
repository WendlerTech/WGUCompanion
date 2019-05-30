package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class CourseDetails extends AppCompatActivity {

    private Course selectedCourse = null;
    private EditText txtCourseTitle, txtStartMonth, txtStartYear, txtEndMonth, txtEndYear,
            txtCourseInfo;
    private Spinner courseStatusSpinner, courseMentorSpinner;
    private DatabaseHelper databaseHelper;
    private ConstraintLayout parentLayout;

    private ArrayAdapter<Mentor> mentorListAdapter;
    private ArrayAdapter<CharSequence> courseStatusAdapter;
    private ArrayList<Mentor> mentorList;

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
        parentLayout.requestFocus();
    }

    private void populateData() {
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

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
