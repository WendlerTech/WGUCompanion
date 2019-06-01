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

public class TermRecyclerViewAdapter extends RecyclerView.Adapter<TermRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Term> termList;
    private Context mContext;
    private FragmentTransaction fragmentTransaction;
    private Fragment courseListFragment = null;
    private DatabaseHelper databaseHelper;
    private Term selectedTerm;

    TermRecyclerViewAdapter(Context mContext, ArrayList<Term> termList,
                            FragmentTransaction fragmentTransaction) {
        this.termList = termList;
        this.mContext = mContext;
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_term_list_item,
                viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        String termTitle = termList.get(position).getTermTitle();
        String termStart = "Start: " + termList.get(position).getStartDate();
        String termEnd = "End: " + termList.get(position).getEndDate();
        int termID = termList.get(position).getTermID();

        viewHolder.lblTermTitle.setText(termTitle);
        viewHolder.lblTermStart.setText(termStart);
        viewHolder.lblTermEnd.setText(termEnd);

        databaseHelper = new DatabaseHelper(mContext);
        if (termID != 0) {
            String courseCount = "Number of courses: " + termList.get(position).getNumberOfCourses();
            viewHolder.lblTermCourses.setText(courseCount);

            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedTerm = termList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectedTerm", selectedTerm);
                    courseListFragment = CourseList.newInstance();
                    courseListFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.termsFrameLayout, courseListFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });

            viewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (termList.get(position).getNumberOfCourses() == 0) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                        dialog.setTitle("Delete Term");
                        dialog.setMessage("Warning!" + "\n\nAre you sure you want to delete this term? " +
                                "This action cannot be undone.");
                        dialog.setPositiveButton("Delete Term", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int termIdToDelete = termList.get(position).getTermID();
                                databaseHelper.deleteTerm(termIdToDelete);
                                Toast.makeText(mContext, "Term deleted successfully.", Toast.LENGTH_SHORT).show();
                                termList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, termList.size());
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
                    } else {
                        //Can't delete
                        AlertDialog.Builder failDeleteDialog = new AlertDialog.Builder(mContext);
                        failDeleteDialog.setTitle("Error");
                        failDeleteDialog.setMessage("You can't delete a term with courses assigned to it." +
                                "\n\nRemove the courses first, then try again.");

                        final AlertDialog alertDialog = failDeleteDialog.create();
                        alertDialog.show();
                        return true;
                    }
                }
            });
        } else {
            viewHolder.lblTermCourses.setText("You can add a new term by clicking below.");
        }
    }

    @Override
    public int getItemCount() {
        return termList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblTermTitle, lblTermCourses, lblTermStart, lblTermEnd;
        ConstraintLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);

            lblTermTitle = itemView.findViewById(R.id.lblListTermTitle);
            lblTermCourses = itemView.findViewById(R.id.lblListTermCourses);
            lblTermStart = itemView.findViewById(R.id.lblListTermStart);
            lblTermEnd = itemView.findViewById(R.id.lblListTermEnd);
            parentLayout = itemView.findViewById(R.id.listTermLayout);
        }
    }
}
