package tech.wendler.wgucompanion;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AssessmentDetails extends Fragment {

    private Assessment selectedAssessment;
    private Course selectedCourse;
    private DatabaseHelper databaseHelper;
    private EditText txtTitle, txtInformation;
    private TextView lblCourseTitle, lblSelectedGoalDate, lblDueDate,
            lblNoCourseSelected, lblHeader;
    private RadioButton radBtnObjective, radBtnPerformance;
    private Button btnCreateAlert, btnCancel, btnSubmit, btnSetGoalDate;
    private Fragment assessmentFragment;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent alarmIntent;
    private PackageManager packageManager;
    private ComponentName receiver;

    private int calStartMonth, calStartYear, calEndMonth, calEndYear;
    private boolean goalDateChosen = false, goalDateAlertCreated = false, noCourseSelected = false;
    private Calendar selectedGoalDateCalendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    public AssessmentDetails() {

    }

    public static AssessmentDetails newInstance() {
        return new AssessmentDetails();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_assessment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtTitle = getView().findViewById(R.id.txtViewAssessmentTitle);
        txtInformation = getView().findViewById(R.id.txtViewAssessmentInformation);
        lblSelectedGoalDate = getView().findViewById(R.id.lblViewAssessmentGoalDate);
        lblHeader = getView().findViewById(R.id.lblViewAssessmentHeader);
        lblNoCourseSelected = getView().findViewById(R.id.lblViewAssessmentNoCourseSelected);
        lblDueDate = getView().findViewById(R.id.lblViewAssessmentDueDate);
        lblCourseTitle = getView().findViewById(R.id.lblViewAssessmentCourseTitle);
        radBtnObjective = getView().findViewById(R.id.radBtnViewAssessmentObjective);
        radBtnPerformance = getView().findViewById(R.id.radBtnViewAssessmentPerformance);
        lblNoCourseSelected.setVisibility(View.INVISIBLE);

        btnCancel = getView().findViewById(R.id.btnViewAssessmentCancel);
        btnSubmit = getView().findViewById(R.id.btnViewAssessmentSubmit);
        btnSetGoalDate = getView().findViewById(R.id.btnViewAssessmentGoalDate);
        btnCreateAlert = getView().findViewById(R.id.btnViewAssessmentAlerts);

        databaseHelper = new DatabaseHelper(getContext());

        Bundle selectedAssessmentBundle = this.getArguments();
        if (selectedAssessmentBundle != null) {
            selectedAssessment = (Assessment) selectedAssessmentBundle.getSerializable("selectedAssessment");
            selectedCourse = (Course) selectedAssessmentBundle.getSerializable("selectedCourse");
            populateData();
        }

        if (selectedCourse == null) {
            lblCourseTitle.setText("your selected course");
        } else {
            lblCourseTitle.setText(selectedCourse.getCourseTitle());
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtTitle.getText().toString().length() > 0 &&
                        txtInformation.getText().toString().length() > 0) {
                    Assessment assessmentToUpdate = new Assessment();
                    assessmentToUpdate.setAssessmentID(selectedAssessment.getAssessmentID());
                    assessmentToUpdate.setCourseID(selectedAssessment.getCourseID());
                    assessmentToUpdate.setAssessmentTitle(txtTitle.getText().toString());
                    assessmentToUpdate.setAssessmentInfo(txtInformation.getText().toString());
                    assessmentToUpdate.setDueDate(selectedAssessment.getDueDate());
                    assessmentToUpdate.setObjective(radBtnObjective.isChecked());

                    if (goalDateChosen) {
                        assessmentToUpdate.setGoalDate(formatDate(selectedGoalDateCalendar));
                    } else {
                        assessmentToUpdate.setGoalDate(selectedAssessment.getGoalDate());
                    }

                    databaseHelper.updateAssessment(assessmentToUpdate);
                    Toast.makeText(getContext(), "Assessment updated successfully", Toast.LENGTH_SHORT).show();

                    assessmentFragment = AssessmentList.newInstance();
                    if (selectedCourse != null) {
                        Bundle courseBundle = new Bundle();
                        courseBundle.putSerializable("selectedCourse", selectedCourse);
                        assessmentFragment.setArguments(courseBundle);
                    }
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.assessmentsFrameLayout, assessmentFragment);
                    fragmentTransaction.commit();
                } else {
                    if (txtTitle.getText().toString().length() == 0) {
                        txtTitle.setError("You must enter a title");
                    }
                    if (txtInformation.getText().toString().length() == 0) {
                        txtInformation.setError("You must enter information");
                    }
                    if (!goalDateChosen) {
                        Toast.makeText(getContext(), "You must select a goal date", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnSetGoalDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();

                startCal.set(Calendar.YEAR, calStartYear);
                startCal.set(Calendar.MONTH, calStartMonth - 1);
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                endCal.set(Calendar.YEAR, calEndYear);
                endCal.set(Calendar.MONTH, calEndMonth - 1);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.CalendarDialogTheme);
                dialog.setOnDateSetListener(dateSetListener);
                dialog.getDatePicker().setMinDate(startCal.getTimeInMillis());
                dialog.getDatePicker().setMaxDate(endCal.getTimeInMillis());

                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                selectedGoalDateCalendar = calendar;
                selectedGoalDateCalendar.set(Calendar.HOUR_OF_DAY, 9);
                selectedGoalDateCalendar.set(Calendar.MINUTE, 30);
                selectedGoalDateCalendar.set(Calendar.SECOND, 0);

                lblSelectedGoalDate.setText("Goal Date: " + formatDate(calendar));
                goalDateChosen = true;
                //Resets alert if goal date is changed again
                if (goalDateAlertCreated) {
                    btnCreateAlert.performClick();
                }
            }
        };

        packageManager = getActivity().getPackageManager();
        receiver = new ComponentName(getActivity(), DeviceBootReceiver.class);
        alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        alarmIntent.putExtra("title", "Assessment Reminder");
        alarmIntent.putExtra("content", "Your assessment for " + selectedCourse.getCourseTitle() + " is starting soon!");

        btnCreateAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks to make sure only 1 alert can be created at once
                if (!goalDateAlertCreated) {
                    //Only creates alert if goal date has already been chosen
                    if (goalDateChosen) {
                        pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        if (alarmManager != null) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, selectedGoalDateCalendar.getTimeInMillis(), pendingIntent);
                            //Turns button green to show that alert was added successfully
                            btnCreateAlert.setTextColor(ContextCompat.getColor(getContext(), R.color.offWhite));
                            btnCreateAlert.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.greenBtn), PorterDuff.Mode.SRC);

                            Toast.makeText(getContext(), "You'll receive an alert on " +
                                    selectedGoalDateCalendar.getTime(), Toast.LENGTH_LONG).show();
                            goalDateAlertCreated = true;
                        }

                        packageManager.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    } else {
                        Toast.makeText(getContext(), "Please re-select your goal date first.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Cancels alert, re-colors button back to default
                    if (PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0) != null
                            && alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                    Toast.makeText(getContext(), "Alert cancelled", Toast.LENGTH_SHORT).show();
                    btnCreateAlert.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultBtnText));
                    btnCreateAlert.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.defaultBtnBg), PorterDuff.Mode.SRC);
                    goalDateAlertCreated = false;
                }
                packageManager.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });
    }

    private void populateData() {
        txtTitle.setText(selectedAssessment.getAssessmentTitle());
        txtInformation.setText(selectedAssessment.getAssessmentInfo());
        lblDueDate.setText(selectedAssessment.getDueDate());
        lblSelectedGoalDate.setText("Goal Date: " + selectedAssessment.getGoalDate());

        if (selectedAssessment.isObjective()) {
            radBtnObjective.toggle();
        } else {
            radBtnPerformance.toggle();
        }

        if (selectedCourse.getStartDate() == null) {
            noCourseSelected = true;
        }

        if (!noCourseSelected) {
            //Splits date strings into month/year
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

            try {
                //Converts month/year into integers
                calStartMonth = Integer.parseInt(startMonth);
                calStartYear = Integer.parseInt(startYear);
                calEndMonth = Integer.parseInt(endMonth);
                calEndYear = Integer.parseInt(endYear);
            } catch (NumberFormatException ignored) {

            }
        } else {
            lblHeader.setText("View Assessment Details");
            lblNoCourseSelected.setVisibility(View.VISIBLE);
            btnCreateAlert.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnSetGoalDate.setVisibility(View.INVISIBLE);
            btnSubmit.setVisibility(View.INVISIBLE);
        }
    }

    //Takes in calendar instance & returns a string formatted as such: November 23, 2015
    private String formatDate(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        return format.format(date.getTime());
    }
}
