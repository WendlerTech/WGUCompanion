package tech.wendler.wgucompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ViewTerms extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_terms);
        setActionBarTitle("Terms");

        Fragment termListFragment = TermList.newInstance();
        FragmentTransaction fragmentTransaction;
        if (getSupportFragmentManager() != null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.termsFrameLayout, termListFragment);
            fragmentTransaction.commit();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
