package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ViewProgress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);
        getSupportActionBar().setTitle("Progress");
    }
}
