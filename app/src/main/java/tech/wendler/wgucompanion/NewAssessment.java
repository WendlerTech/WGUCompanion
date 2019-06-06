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

public class NewAssessment extends Fragment {

    private int calStartMonth, calStartYear, calEndMonth, calEndYear;
    private boolean goalDateChosen = false, goalDateAlertCreated = false;
    private Course selectedCourse;
    private DatabaseHelper databaseHelper;

    private TextView lblCourseTitle, lblSelectedGoalDate, lblDueDate;
    private EditText txtTitle, txtInformation;
    private RadioButton radBtnObjective, radBtnPerformance;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar selectedGoalDateCalendar;
    private Button btnCreateAlert;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent alarmIntent;
    private PackageManager packageManager;
    private ComponentName receiver;

    public NewAssessment() {

    }

    public static NewAssessment newInstance() {
        return new NewAssessment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_assessment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle selectedCourseBundle = this.getArguments();
        if (selectedCourseBundle != null) {
            selectedCourse = (Course) selectedCourseBundle.getSerializable("selectedCourse");
        }

        databaseHelper = new DatabaseHelper(getContext());

        lblCourseTitle = getView().findViewById(R.id.lblNewAssessmentCourseTitle);
        lblSelectedGoalDate = getView().findViewById(R.id.lblNewAssessmentGoalDate);
        lblDueDate = getView().findViewById(R.id.lblNewAssessmentDueDate);
        txtTitle = getView().findViewById(R.id.txtNewAssessmentTitle);
        txtInformation = getView().findViewById(R.id.txtNewAssessmentInformation);
        radBtnObjective = getView().findViewById(R.id.radBtnNewAssessmentObjective);
        radBtnPerformance = getView().findViewById(R.id.radBtnNewAssessmentPerformance);

        lblCourseTitle.setText(selectedCourse.getCourseTitle());
        lblDueDate.setText(selectedCourse.getEndDate());
        lblSelectedGoalDate.setVisibility(View.INVISIBLE);

        btnCreateAlert = getView().findViewById(R.id.btnNewAssessmentAlerts);
        Button btnCancel = getView().findViewById(R.id.btnNewAssessmentCancel);
        Button btnSubmit = getView().findViewById(R.id.btnNewAssessmentSubmit);
        Button btnDatePicker = getView().findViewById(R.id.btnNewAssessmentGoalDate);

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

        //Date picker only allows a date within range of course start & end dates
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
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

                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                        R.style.CalendarDialogTheme);
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
                lblSelectedGoalDate.setVisibility(View.VISIBLE);
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
                        Toast.makeText(getContext(), "Set a goal date first", Toast.LENGTH_SHORT).show();
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
                if (txtTitle.getText().toString().length() > 0 && goalDateChosen
                        && txtInformation.getText().toString().length() > 0) {
                    Assessment assessmentToAdd = new Assessment();
                    assessmentToAdd.setCourseID(selectedCourse.getCourseID());
                    assessmentToAdd.setAssessmentTitle(txtTitle.getText().toString());
                    assessmentToAdd.setAssessmentInfo(txtInformation.getText().toString());
                    assessmentToAdd.setGoalDate(formatDate(selectedGoalDateCalendar));
                    assessmentToAdd.setDueDate(selectedCourse.getEndDate());
                    assessmentToAdd.setObjective(radBtnObjective.isChecked());

                    long newAssessmentId = databaseHelper.addNewAssessment(assessmentToAdd);
                    int newlyAddedAssessmentId = Math.toIntExact(newAssessmentId);

                    if (newlyAddedAssessmentId != -1) {
                        assessmentToAdd.setAssessmentID(newlyAddedAssessmentId);
                        Toast.makeText(getContext(), "Assessment added successfully", Toast.LENGTH_SHORT).show();

                        Bundle courseBundle = new Bundle();
                        courseBundle.putSerializable("selectedCourse", selectedCourse);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment listFragment = AssessmentList.newInstance();
                        listFragment.setArguments(courseBundle);
                        fragmentTransaction.replace(R.id.assessmentsFrameLayout, listFragment);
                        fragmentTransaction.commit();
                    }

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
    }

    //Takes in calendar instance & returns a string formatted as such: November 23, 2015
    private String formatDate(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        return format.format(date.getTime());
    }
}
