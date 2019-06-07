package tech.wendler.wgucompanion;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NoteDetails extends Fragment {

    private EditText txtViewNoteTitle, txtViewNoteContent;
    private TextView lblViewNoteHeader;
    private ConstraintLayout parentLayout;
    private DatabaseHelper databaseHelper;
    private Note currentNote;
    private Course selectedCourse;

    public NoteDetails() {

    }

    public static NoteDetails newInstance() {
        return new NoteDetails();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_note, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle courseBundle = this.getArguments();
        if (courseBundle != null) {
            this.currentNote = (Note) courseBundle.getSerializable("selectedNote");
            this.selectedCourse = (Course) courseBundle.getSerializable("selectedCourse");
            lblViewNoteHeader = getView().findViewById(R.id.lblViewNoteHeader);
            lblViewNoteHeader.setText("View note from:\n" + selectedCourse.getCourseTitle());
        }

        Button btnClear, btnCancel, btnSubmit, btnShareNote;
        btnClear = getView().findViewById(R.id.btnViewNoteClear);
        btnCancel = getView().findViewById(R.id.btnViewNoteCancel);
        btnSubmit = getView().findViewById(R.id.btnViewNoteSubmit);
        btnShareNote = getView().findViewById(R.id.btnViewNoteShare);
        txtViewNoteTitle = getView().findViewById(R.id.txtViewNoteTitle);
        txtViewNoteContent = getView().findViewById(R.id.txtViewNoteContent);
        parentLayout = getView().findViewById(R.id.viewNoteLayout);
        databaseHelper = new DatabaseHelper(getContext());

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtViewNoteContent.setText("");
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
                if (txtViewNoteTitle.getText().toString().length() > 0 &&
                        txtViewNoteContent.getText().toString().length() > 0) {
                    Note updatedNote = new Note();
                    updatedNote.setNoteID(currentNote.getNoteID());
                    updatedNote.setCourseID(selectedCourse.getCourseID());
                    updatedNote.setNoteTitle(txtViewNoteTitle.getText().toString());
                    updatedNote.setNote(txtViewNoteContent.getText().toString());

                    databaseHelper.updateNote(updatedNote);
                    Toast.makeText(getContext(), "Note updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), CourseDetails.class);
                    intent.putExtra("selectedCourse", selectedCourse);
                    startActivity(intent);
                } else {
                    if (txtViewNoteTitle.getText().toString().length() == 0) {
                        txtViewNoteTitle.setError("You must enter a title.");
                    }
                    if (txtViewNoteContent.getText().toString().length() == 0) {
                        txtViewNoteContent.setError("Note body can't be left blank.");
                    }
                }
            }
        });

        btnShareNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, currentNote.getNoteTitle() +
                        "\n" + currentNote.getNote());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        populateData();
        parentLayout.requestFocus();
    }

    private void populateData() {
        txtViewNoteContent.setText(currentNote.getNote());
        txtViewNoteTitle.setText(currentNote.getNoteTitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((ViewNotes) getActivity()).setActionBarTitle("View Note");
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
