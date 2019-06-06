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

public class CourseList extends Fragment {

    private DatabaseHelper databaseHelper;
    private ArrayList<Course> courseArrayList;
    private FloatingActionButton floatingActionButton;
    private Term selectedTerm;
    private boolean showAllCourses = false;

    public CourseList() {

    }

    public static CourseList newInstance() {
        return new CourseList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);
        floatingActionButton = view.findViewById(R.id.courseFloatingBtn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCourse();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        databaseHelper = new DatabaseHelper(getActivity());
        Bundle selectedTermBundle = this.getArguments();
        if (selectedTermBundle != null) {
            this.selectedTerm = (Term) selectedTermBundle.getSerializable("selectedTerm");
            showAllCourses = false;
        } else {
            showAllCourses = true;
            floatingActionButton.hide();
        }

        courseArrayList = populateCourseList();
        openRecyclerView();
    }

    private ArrayList<Course> populateCourseList() {
        int courseID, mentorID, termID;
        String status, title, info, startDate, endDate;

        courseArrayList = new ArrayList<>();
        String queryString;

        if (!showAllCourses) {
            queryString = "SELECT * FROM Courses WHERE termID LIKE '" + selectedTerm.getTermID() + "';";
        } else {
            queryString = "SELECT * FROM Courses;";
        }

        try (Cursor cursor = databaseHelper.getData(queryString)) {
            if (!cursor.moveToFirst()) {
                Course emptyCourse = new Course();
                if (showAllCourses) {
                    emptyCourse.setCourseTitle("You haven't added any courses yet.");
                    emptyCourse.setCourseStatus("You can add a course by returning & selecting a term.");
                } else {
                    emptyCourse.setCourseTitle("You haven't added any courses yet.");
                    emptyCourse.setCourseStatus("You can add a course by clicking the button below.");
                }
                emptyCourse.setStartDate("");
                emptyCourse.setEndDate("");
                courseArrayList.add(emptyCourse);
            } else {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    courseID = cursor.getInt(cursor.getColumnIndex("courseID"));
                    mentorID = cursor.getInt(cursor.getColumnIndex("mentorID"));
                    termID = cursor.getInt(cursor.getColumnIndex("termID"));
                    status = cursor.getString(cursor.getColumnIndex("course_status"));
                    title = cursor.getString(cursor.getColumnIndex("course_title"));
                    info = cursor.getString(cursor.getColumnIndex("course_info"));
                    startDate = cursor.getString(cursor.getColumnIndex("start_date"));
                    endDate = cursor.getString(cursor.getColumnIndex("end_date"));

                    Course course = new Course(courseID, mentorID, termID, status, title, info,
                            startDate, endDate);

                    courseArrayList.add(course);
                }
            }
        }

        return courseArrayList;
    }

    public void openRecyclerView() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        RecyclerView recyclerView = getView().findViewById(R.id.courseRecyclerView);
        CourseRecyclerViewAdapter adapter = new CourseRecyclerViewAdapter(getContext(),
                courseArrayList, fragmentTransaction);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (!showAllCourses) {
            if (adapter.getItemCount() != 0) {
                Toast.makeText(getContext(), "Click to view course details," +
                        "\nor long press to delete the course.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "These are all of the courses you've added.\n" +
                    "Return & select a term to add more.", Toast.LENGTH_LONG).show();
        }
    }

    private void addNewCourse() {
        Fragment newCourseFragment = NewCourse.newInstance();
        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedTerm", selectedTerm);
            newCourseFragment.setArguments(bundle);
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.termsFrameLayout, newCourseFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showAllCourses) {
            if (getActivity() != null) {
                ((ViewCourses) getActivity()).setActionBarTitle("Courses From All Terms");
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (getActivity() != null) {
                ((ViewTerms) getActivity()).setActionBarTitle("Courses in " + selectedTerm.getTermTitle());
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
