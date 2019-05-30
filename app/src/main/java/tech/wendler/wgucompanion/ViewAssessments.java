package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ViewAssessments extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assessments);
        getSupportActionBar().setTitle("Assessments");
    }
}
