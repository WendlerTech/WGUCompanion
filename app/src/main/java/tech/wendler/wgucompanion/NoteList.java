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

public class NoteList extends Fragment {

    private FloatingActionButton noteFloatingButton;
    private DatabaseHelper databaseHelper;
    private Course selectedCourse;
    private ArrayList<Note> noteList;

    public NoteList() {

    }

    public static NoteList newInstance() {
        return new NoteList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        noteFloatingButton = view.findViewById(R.id.noteFloatingBtn);
        noteFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewNote();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle selectedCourseBundle = this.getArguments();
        if (selectedCourseBundle != null) {
            this.selectedCourse = (Course) selectedCourseBundle.getSerializable("selectedCourse");
        }
        databaseHelper = new DatabaseHelper(getContext());

        noteList = populateNoteList();
        openRecyclerView();
    }

    private ArrayList<Note> populateNoteList() {
        noteList = new ArrayList<>();
        int noteID, courseID;
        String note, noteTitle;
        String queryString = "SELECT * FROM Notes WHERE courseID = " + selectedCourse.getCourseID() + ";";

        try (Cursor cursor = databaseHelper.getData(queryString)) {
            if (!cursor.moveToFirst()) {
                Note emptyNote = new Note();
                emptyNote.setNoteTitle("You haven't added any notes yet.");
                emptyNote.setNote("You can add notes by clicking the button below.");
                noteList.add(emptyNote);
            } else {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    noteID = cursor.getInt(cursor.getColumnIndex("noteID"));
                    courseID = cursor.getInt(cursor.getColumnIndex("courseID"));
                    note = cursor.getString(cursor.getColumnIndex("note"));
                    noteTitle = cursor.getString(cursor.getColumnIndex("noteTitle"));

                    Note noteToAdd = new Note(noteID, courseID, note, noteTitle);

                    noteList.add(noteToAdd);
                }
            }
        }

        return noteList;
    }

    private void addNewNote() {
        Fragment newNoteFragment = NewNote.newInstance();
        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedCourse", selectedCourse);
            newNoteFragment.setArguments(bundle);
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.notesFrameLayout, newNoteFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void openRecyclerView() {
        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getFragmentManager().beginTransaction();
            RecyclerView recyclerView = getView().findViewById(R.id.noteRecyclerView);
            NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(getContext(), noteList, fragmentTransaction, selectedCourse);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            if (adapter.getItemCount() != 0) {
                Toast.makeText(getContext(), "Click to view note details," +
                        "\nor long press to delete the note.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((ViewNotes)getActivity()).setActionBarTitle("Class Notes");
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
