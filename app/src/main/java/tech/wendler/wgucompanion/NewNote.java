package tech.wendler.wgucompanion;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewNote extends Fragment {

    private EditText txtNewNoteTitle, txtNewNoteContent;
    private TextView lblNewNoteHeader;
    private ConstraintLayout parentLayout;
    private DatabaseHelper databaseHelper;
    private Note newNote;
    private Course selectedCourse;

    public NewNote() {

    }

    public static NewNote newInstance() {
        return new NewNote();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_note, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle courseBundle = this.getArguments();
        if (courseBundle != null) {
            this.selectedCourse = (Course) courseBundle.getSerializable("selectedCourse");
            lblNewNoteHeader = getView().findViewById(R.id.lblNewNoteHeader);
            lblNewNoteHeader.setText("Create new note for:\n" + selectedCourse.getCourseTitle());
        }

        Button btnClear, btnCancel, btnSubmit;

        databaseHelper = new DatabaseHelper(getContext());
        parentLayout = getView().findViewById(R.id.newNoteLayout);
        txtNewNoteTitle = getView().findViewById(R.id.txtNewNoteTitle);
        txtNewNoteContent = getView().findViewById(R.id.txtNewNoteContent);
        btnClear = getView().findViewById(R.id.btnNewNoteClear);
        btnCancel = getView().findViewById(R.id.btnNewNoteCancel);
        btnSubmit = getView().findViewById(R.id.btnNewNoteSubmit);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtNewNoteContent.setText("");
            }
        });

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
                if (txtNewNoteTitle.getText().toString().length() > 0 &&
                        txtNewNoteContent.getText().toString().length() > 0) {
                    newNote = new Note();
                    newNote.setNoteTitle(txtNewNoteTitle.getText().toString());
                    newNote.setNote(txtNewNoteContent.getText().toString());
                    newNote.setCourseID(selectedCourse.getCourseID());

                    long newlyAddedNoteId = databaseHelper.addNewNote(newNote);
                    int newNoteId = Math.toIntExact(newlyAddedNoteId);
                    if (newNoteId != -1) {
                        newNote.setNoteID(newNoteId);
                        Toast.makeText(getContext(), "Note added successfully.", Toast.LENGTH_SHORT).show();
                        Fragment noteListFragment = NoteList.newInstance();
                        FragmentTransaction fragmentTransaction;
                        if (getFragmentManager() != null) {
                            fragmentTransaction = getFragmentManager().beginTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("selectedCourse", selectedCourse);
                            noteListFragment.setArguments(bundle);
                            fragmentTransaction.replace(R.id.notesFrameLayout, noteListFragment);
                            fragmentTransaction.commit();
                        }
                    } else {
                        Toast.makeText(getContext(), "There was an error saving note data.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (txtNewNoteTitle.getText().length() == 0) {
                        txtNewNoteTitle.setError("You must enter a title.");
                    }
                    if (txtNewNoteContent.getText().length() == 0) {
                        txtNewNoteContent.setError("Notes are optional, but require a body if created.");
                    }
                }
            }
        });
        parentLayout.requestFocus();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((ViewNotes) getActivity()).setActionBarTitle("New Note");
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
