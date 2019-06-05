package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ViewNotes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);
        setActionBarTitle("Notes");

        Bundle bundle = this.getIntent().getExtras();
        Fragment notesFragment = NoteList.newInstance();
        FragmentTransaction fragmentTransaction;

        if (getSupportFragmentManager() != null) {
            if (bundle != null) {
                Bundle notesBundle = new Bundle();
                notesBundle.putSerializable("selectedCourse", bundle.getSerializable("selectedCourse"));
                notesFragment.setArguments(notesBundle);
            }
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.notesFrameLayout, notesFragment);
            fragmentTransaction.commit();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
