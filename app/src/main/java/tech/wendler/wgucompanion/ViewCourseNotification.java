package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ViewCourseNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_notification);
        setActionBarTitle("Course Notifications");

        Bundle bundle = this.getIntent().getExtras();
        Fragment courseNotifyFragment = NewCourseNotification.newInstance();
        FragmentTransaction fragmentTransaction;

        if (getSupportFragmentManager() != null) {
            if (bundle != null) {
                Bundle courseNotifyBundle = new Bundle();
                courseNotifyBundle.putSerializable("selectedCourse", bundle.getSerializable("selectedCourse"));
                courseNotifyFragment.setArguments(courseNotifyBundle);
            }
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.courseNotifyFrameLayout, courseNotifyFragment);
            fragmentTransaction.commit();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
