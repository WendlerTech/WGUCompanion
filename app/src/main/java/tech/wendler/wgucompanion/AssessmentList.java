package tech.wendler.wgucompanion;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;

public class AssessmentList extends Fragment {

    private DatabaseHelper databaseHelper;
    private Course selectedCourse;
    private ArrayList<Assessment> assessmentList;
    private boolean showAllAssessments = false;
    private FloatingActionButton assessmentFloatingBtn;

    public AssessmentList() {

    }

    public static AssessmentList newInstance() {
        return new AssessmentList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assessment_list, container, false);
        assessmentFloatingBtn = view.findViewById(R.id.assessmentFloatingBtn);
        assessmentFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAssessment();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle selectedCourseBundle = this.getArguments();
        if (selectedCourseBundle != null) {
            this.selectedCourse = (Course) selectedCourseBundle.getSerializable("selectedCourse");
            showAllAssessments = false;
        } else {
            showAllAssessments = true;
            assessmentFloatingBtn.hide();
        }

        databaseHelper = new DatabaseHelper(getContext());

        assessmentList = populateAssessmentList();
        openRecyclerView();
    }

    private void addNewAssessment() {
        Fragment newAssessmentFragment = NewAssessment.newInstance();
        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedCourse", selectedCourse);
            newAssessmentFragment.setArguments(bundle);
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.assessmentsFrameLayout, newAssessmentFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private ArrayList<Assessment> populateAssessmentList() {
        assessmentList = new ArrayList<>();
        int assessmentID, courseID, isObjective;
        String title, info, goalDate, dueDate, queryString;
        boolean boolIsObjective;


        if (!showAllAssessments) {
            queryString = "SELECT * FROM Assessments WHERE courseID = " +
                    selectedCourse.getCourseID() + ";";
        } else {
            queryString = "SELECT * FROM Assessments;";
        }

        try (Cursor cursor = databaseHelper.getData(queryString)) {
            if (!cursor.moveToFirst()) {
                Assessment emptyAssessment = new Assessment();
                emptyAssessment.setAssessmentTitle("You haven't added any assessments yet.");
                assessmentList.add(emptyAssessment);
            } else {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    assessmentID = cursor.getInt(cursor.getColumnIndex("assessmentID"));
                    courseID = cursor.getInt(cursor.getColumnIndex("courseID"));
                    title = cursor.getString(cursor.getColumnIndex("title"));
                    info = cursor.getString(cursor.getColumnIndex("assessment_info"));
                    goalDate = cursor.getString(cursor.getColumnIndex("goal_date"));
                    dueDate = cursor.getString(cursor.getColumnIndex("due_date"));
                    isObjective = cursor.getInt(cursor.getColumnIndex("is_objective"));

                    boolIsObjective = isObjective == 1;

                    Assessment assessmentToAdd = new Assessment(assessmentID, courseID, title, info, goalDate, dueDate, boolIsObjective);

                    assessmentList.add(assessmentToAdd);
                }
            }
        }
        return assessmentList;
    }

    public void openRecyclerView() {
        if (getFragmentManager() != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            RecyclerView recyclerView = getView().findViewById(R.id.assessmentRecyclerView);
            AssessmentRecyclerViewAdapter adapter = new AssessmentRecyclerViewAdapter(getContext(),
                    assessmentList, fragmentTransaction, selectedCourse);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            if (!showAllAssessments) {
                if (adapter.getItemCount() != 0) {
                    Toast.makeText(getContext(), "Click to view assessment details," +
                            "\nor long press to delete the assessment.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "These are all of the assessments you've added.\n" +
                        "Return & select a course to add more.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showAllAssessments) {
            if (getActivity() != null) {
                ((ViewAssessments) getActivity()).setActionBarTitle("View All Assessments");
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (getActivity() != null) {
                ((ViewAssessments) getActivity()).setActionBarTitle("Assessments in " + selectedCourse.getCourseTitle());
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }
}
