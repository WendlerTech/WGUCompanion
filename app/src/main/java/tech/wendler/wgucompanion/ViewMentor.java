package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewMentor extends AppCompatActivity {

    private Spinner spinnerMentor;
    private TextView lblName, lblEmail, lblPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mentor);
        getSupportActionBar().setTitle("Mentor");

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        lblName = findViewById(R.id.lblMentorName);
        lblEmail = findViewById(R.id.lblMentorEmail);
        lblPhone = findViewById(R.id.lblMentorPhone);
        spinnerMentor = findViewById(R.id.spinnerMentorList);

        //Populates spinner with previously entered mentors
        ArrayList<Mentor> mentorList = databaseHelper.getMentorList();
        ArrayAdapter<Mentor> mentorListAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, mentorList);
        mentorListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerMentor.setAdapter(mentorListAdapter);

        spinnerMentor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Mentor selectedMentor = (Mentor) adapterView.getSelectedItem();
                lblName.setText("Name: " + selectedMentor.getMentorName());
                lblEmail.setText("Email address: " + selectedMentor.getMentorEmail());
                lblPhone.setText("Phone number: " + selectedMentor.getMentorPhoneNum());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
