package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class NewAssessment extends Fragment {

    private Course selectedCourse;
    private DatabaseHelper databaseHelper;
    private TextView lblCourseTitle;
    private EditText txtTitle, txtGoalMonth, txtGoalYear, txtDueMonth, txtDueYear, txtInformation;
    private RadioGroup assessmentTypeGroup;
    private RadioButton radBtnObjective, radBtnPerformance;

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
        txtTitle = getView().findViewById(R.id.txtNewAssessmentTitle);
        txtGoalMonth = getView().findViewById(R.id.txtNewAssessmentGoalMonth);
        txtGoalYear = getView().findViewById(R.id.txtNewAssessmentGoalYear);
        txtDueMonth = getView().findViewById(R.id.txtNewAssessmentDueMonth);
        txtDueYear = getView().findViewById(R.id.txtNewAssessmentDueYear);
        txtInformation = getView().findViewById(R.id.txtNewAssessmentInformation);
        assessmentTypeGroup = getView().findViewById(R.id.radGrpNewAssessment);
        radBtnObjective = getView().findViewById(R.id.radBtnNewAssessmentObjective);
        radBtnPerformance = getView().findViewById(R.id.radBtnNewAssessmentPerformance);
        lblCourseTitle.setText(selectedCourse.getCourseTitle());

        Button btnCreateAlert = getView().findViewById(R.id.btnNewAssessmentAlerts);
        Button btnCancel = getView().findViewById(R.id.btnNewAssessmentCancel);
        Button btnSubmit = getView().findViewById(R.id.btnNewAssessmentSubmit);

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
                    Assessment assessmentToAdd = new Assessment();
                    assessmentToAdd.setCourseID(selectedCourse.getCourseID());
                    assessmentToAdd.setAssessmentTitle(txtTitle.getText().toString());
                    assessmentToAdd.setAssessmentInfo(txtInformation.getText().toString());
                    assessmentToAdd.setGoalDate(txtGoalMonth.getText().toString() + "-" +
                            txtGoalYear.getText().toString());
                    assessmentToAdd.setDueDate(txtDueMonth.getText().toString() + "-" +
                            txtDueYear.getText().toString());
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
    }
}
