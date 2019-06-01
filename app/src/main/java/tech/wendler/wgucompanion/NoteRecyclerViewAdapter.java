package tech.wendler.wgucompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Note> noteList;
    private Context mContext;
    private FragmentTransaction fragmentTransaction;
    private Fragment noteDetailsFragment;
    private DatabaseHelper databaseHelper;
    private Course selectedCourse;

    NoteRecyclerViewAdapter(Context mContext, ArrayList<Note> noteList,
                            FragmentTransaction fragmentTransaction, Course selectedCourse) {
        this.mContext = mContext;
        this.noteList = noteList;
        this.fragmentTransaction = fragmentTransaction;
        this.selectedCourse = selectedCourse;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_note_list_item,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        int noteID, courseID;
        String noteContent, noteTitle;

        noteID = noteList.get(position).getNoteID();
        courseID = noteList.get(position).getCourseID();
        noteContent = noteList.get(position).getNote();
        noteTitle = noteList.get(position).getNoteTitle();

        viewHolder.lblNoteTitle.setText(noteTitle);
        viewHolder.lblNoteContent.setText(noteContent);
        databaseHelper = new DatabaseHelper(mContext);

        if (noteID != 0) {
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    Note selectedNote = noteList.get(position);
                    bundle.putSerializable("selectedCourse", selectedCourse);
                    bundle.putSerializable("selectedNote", selectedNote);
                    noteDetailsFragment = NoteDetails.newInstance();
                    noteDetailsFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.notesFrameLayout, noteDetailsFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });


            viewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Delete Note");
                    dialog.setMessage("Warning!" + "\n\nAre you sure you want to delete this note? " +
                            "This action cannot be undone.");
                    dialog.setPositiveButton("Delete Note", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int noteIdToDelete = noteList.get(position).getNoteID();
                            databaseHelper.deleteNote(noteIdToDelete);
                            Toast.makeText(mContext, "Note deleted Successfully", Toast.LENGTH_SHORT).show();
                            noteList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, noteList.size());
                        }
                    })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //User cancelled, nothing happens
                                }
                            });
                    final AlertDialog alert = dialog.create();
                    alert.show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblNoteTitle, lblNoteContent;
        ConstraintLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);

            lblNoteTitle = itemView.findViewById(R.id.lblNoteListTitle);
            lblNoteContent = itemView.findViewById(R.id.lblNoteListMessage);
            parentLayout = itemView.findViewById(R.id.noteListLayout);
        }
    }
}
