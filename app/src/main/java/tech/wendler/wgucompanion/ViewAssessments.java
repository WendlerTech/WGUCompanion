package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ViewAssessments extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assessments);
        setActionBarTitle("Assessments");

        Bundle bundle = this.getIntent().getExtras();
        Fragment assessmentFragment = AssessmentList.newInstance();
        FragmentTransaction fragmentTransaction;

        if (getSupportFragmentManager() != null) {
            if (bundle != null) {
                Bundle assessmentBundle = new Bundle();
                assessmentBundle.putSerializable("selectedCourse", bundle.getSerializable("selectedCourse"));
                assessmentFragment.setArguments(assessmentBundle);
            }
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.assessmentsFrameLayout, assessmentFragment);
            fragmentTransaction.commit();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
