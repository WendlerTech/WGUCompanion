package tech.wendler.wgucompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class AssessmentDetails extends Fragment {

    private Assessment selectedAssessment;
    private DatabaseHelper databaseHelper;
    private EditText txtTitle, txtGoalMonth, txtGoalYear, txtDueMonth, txtDueYear, txtInformation;
    private TextView lblCourseTitle;
    private RadioGroup assessmentTypeGroup;
    private RadioButton radBtnObjective, radBtnPerformance;
    private Course selectedCourse;
    private Fragment assessmentFragment;

    private AlarmManager startAlarmManager, endAlarmManager;
    private PendingIntent startPendingIntent, endPendingIntent;
    private Intent alarmIntent;
    private PackageManager packageManager;
    private ComponentName receiver;

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
        txtGoalMonth = getView().findViewById(R.id.txtViewAssessmentGoalMonth);
        txtGoalYear = getView().findViewById(R.id.txtViewAssessmentGoalYear);
        txtDueMonth = getView().findViewById(R.id.txtViewAssessmentDueMonth);
        txtDueYear = getView().findViewById(R.id.txtViewAssessmentDueYear);
        txtInformation = getView().findViewById(R.id.txtViewAssessmentInformation);
        lblCourseTitle = getView().findViewById(R.id.lblNewAssessmentCourseTitle);
        assessmentTypeGroup = getView().findViewById(R.id.radGrpViewAssessment);
        radBtnObjective = getView().findViewById(R.id.radBtnViewAssessmentObjective);
        radBtnPerformance = getView().findViewById(R.id.radBtnViewAssessmentPerformance);

        Button btnCreateAlert = getView().findViewById(R.id.btnViewAssessmentAlerts);
        Button btnCancel = getView().findViewById(R.id.btnViewAssessmentCancel);
        Button btnSubmit = getView().findViewById(R.id.btnViewAssessmentSubmit);

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
                        txtGoalMonth.getText().toString().length() > 0 &&
                        txtGoalYear.getText().toString().length() > 0 &&
                        txtDueMonth.getText().toString().length() > 0 &&
                        txtDueYear.getText().toString().length() > 0 &&
                        txtInformation.getText().toString().length() > 0) {
                    Assessment assessmentToUpdate = new Assessment();
                    assessmentToUpdate.setAssessmentID(selectedAssessment.getAssessmentID());
                    assessmentToUpdate.setCourseID(selectedAssessment.getCourseID());
                    assessmentToUpdate.setAssessmentTitle(txtTitle.getText().toString());
                    assessmentToUpdate.setAssessmentInfo(txtInformation.getText().toString());
                    assessmentToUpdate.setGoalDate(txtGoalMonth.getText().toString() + "-" +
                            txtGoalYear.getText().toString());
                    assessmentToUpdate.setDueDate(txtDueMonth.getText().toString() + "-" +
                            txtDueYear.getText().toString());
                    assessmentToUpdate.setObjective(radBtnObjective.isChecked());

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
                    if (txtGoalMonth.getText().toString().length() == 0) {
                        txtGoalMonth.setError("You must enter a month");
                    }
                    if (txtGoalYear.getText().toString().length() == 0) {
                        txtGoalYear.setError("You must enter a year");
                    }
                    if (txtDueMonth.getText().toString().length() == 0) {
                        txtDueMonth.setError("You must enter a month");
                    }
                    if (txtDueYear.getText().toString().length() == 0) {
                        txtDueYear.setError("You must enter a year");
                    }
                    if (txtInformation.getText().toString().length() == 0) {
                        txtInformation.setError("You must enter information");
                    }
                }
            }
        });

        packageManager = getActivity().getPackageManager();
        receiver = new ComponentName(getActivity(), DeviceBootReceiver.class);
        alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        startAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        endAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        btnCreateAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar alertCal = Calendar.getInstance();
                alertCal.setTimeInMillis(System.currentTimeMillis());


            }
        });
    }

    private void populateData() {
        String goalDate = selectedAssessment.getGoalDate();
        String dueDate = selectedAssessment.getDueDate();
        String goalMonth, goalYear, dueMonth, dueYear;

        if (goalDate.length() == 6) {
            goalMonth = goalDate.substring(0, 1);
            goalYear = goalDate.substring(2);
        } else {
            goalMonth = goalDate.substring(0, 2);
            goalYear = goalDate.substring(3);
        }

        if (dueDate.length() == 6) {
            dueMonth = dueDate.substring(0, 1);
            dueYear = dueDate.substring(2);
        } else {
            dueMonth = dueDate.substring(0, 2);
            dueYear = dueDate.substring(3);
        }

        txtTitle.setText(selectedAssessment.getAssessmentTitle());
        txtInformation.setText(selectedAssessment.getAssessmentInfo());
        txtGoalMonth.setText(goalMonth);
        txtGoalYear.setText(goalYear);
        txtDueMonth.setText(dueMonth);
        txtDueYear.setText(dueYear);

        if (selectedAssessment.isObjective()) {
            radBtnObjective.toggle();
        } else {
            radBtnPerformance.toggle();
        }
    }
}
