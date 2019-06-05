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

public class AssessmentRecyclerViewAdapter extends RecyclerView.Adapter<AssessmentRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Assessment> assessmentList;
    private Context mContext;
    private FragmentTransaction fragmentTransaction;
    private DatabaseHelper databaseHelper;
    private Course selectedCourse;

    AssessmentRecyclerViewAdapter(Context mContext, ArrayList<Assessment> assessmentList,
                                  FragmentTransaction fragmentTransaction, Course selectedCourse) {
        this.mContext = mContext;
        this.assessmentList = assessmentList;
        this.fragmentTransaction = fragmentTransaction;
        this.selectedCourse = selectedCourse;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_assessment_list_item,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        int assessmentID, courseID;
        String title, info, goalDate, dueDate;
        boolean isObjective;

        databaseHelper = new DatabaseHelper(mContext);

        assessmentID = assessmentList.get(position).getAssessmentID();
        courseID = assessmentList.get(position).getCourseID();
        title = assessmentList.get(position).getAssessmentTitle();
        info = assessmentList.get(position).getAssessmentInfo();
        goalDate = "Goal date: " + assessmentList.get(position).getGoalDate();
        dueDate = assessmentList.get(position).getDueDate();
        isObjective = assessmentList.get(position).isObjective();

        viewHolder.lblAssessmentTitle.setText(title);
        viewHolder.lblGoalDate.setText(goalDate);
        if (isObjective) {
            viewHolder.lblAssessmentType.setText(R.string.objective_assessment);

        } else {
            viewHolder.lblAssessmentType.setText(R.string.performance_assessment);
        }

        if (assessmentID != 0) {
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Assessment selectedAssessment = assessmentList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectedAssessment", selectedAssessment);
                    bundle.putSerializable("selectedCourse", selectedCourse);
                    Fragment assessmentDetailsFragment = AssessmentDetails.newInstance();
                    assessmentDetailsFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.assessmentsFrameLayout, assessmentDetailsFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });

            viewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Delete Assessment");
                    dialog.setMessage("Warning!" + "\n\nAre you sure you want to delete this assessment? " +
                            "This action cannot be undone.");
                    dialog.setPositiveButton("Delete Assessment", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int assessmentIdToDelete = assessmentList.get(position).getAssessmentID();
                            databaseHelper.deleteAssessment(assessmentIdToDelete);
                            Toast.makeText(mContext, "Assessment deleted successfully.", Toast.LENGTH_SHORT).show();
                            assessmentList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, assessmentList.size());
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
        } else {
            viewHolder.lblAssessmentType.setText(R.string.empty_assessment_msg);
            viewHolder.lblGoalDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return assessmentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblAssessmentTitle, lblAssessmentType, lblGoalDate;
        ConstraintLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);

            lblAssessmentTitle = itemView.findViewById(R.id.lblListAssessmentTitle);
            lblAssessmentType = itemView.findViewById(R.id.lblListAssessmentType);
            lblGoalDate = itemView.findViewById(R.id.lblListAssessmentGoalDate);
            parentLayout = itemView.findViewById(R.id.listAssessmentLayout);
        }
    }
}
