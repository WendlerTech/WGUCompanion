package tech.wendler.wgucompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CourseRecyclerViewAdapter extends RecyclerView.Adapter<CourseRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Course> courseList;
    private Context mContext;
    private FragmentTransaction fragmentTransaction;
    private DatabaseHelper databaseHelper;
    private Term selectedTerm;

    CourseRecyclerViewAdapter(Context mContext, ArrayList<Course> courseList,
                              FragmentTransaction fragmentTransaction, Term selectedTerm) {
        this.mContext = mContext;
        this.courseList = courseList;
        this.fragmentTransaction = fragmentTransaction;
        this.selectedTerm = selectedTerm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_course_list_item,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        int termID;
        String status, title, startDate, endDate;

        termID = courseList.get(position).getTermID();
        status = courseList.get(position).getCourseStatus();
        title = courseList.get(position).getCourseTitle();
        startDate = "Start: " + courseList.get(position).getStartDate();
        endDate = "Due by: " + courseList.get(position).getEndDate();

        viewHolder.lblCourseTitle.setText(title);
        viewHolder.lblCourseStatus.setText(status);
        viewHolder.lblCourseStart.setText(startDate);
        viewHolder.lblCourseDue.setText(endDate);
        databaseHelper = new DatabaseHelper(mContext);

        if (termID != 0) {
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Course selectedCourse = courseList.get(position);
                    Intent intent = new Intent(mContext, CourseDetails.class);
                    intent.putExtra("selectedCourse", selectedCourse);
                    intent.putExtra("selectedTerm", selectedTerm);
                    mContext.startActivity(intent);
                }
            });

            viewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Delete Course");
                    dialog.setMessage("Warning!" + "\n\nAre you sure you want to delete this course? " +
                            "This action cannot be undone.");
                    dialog.setPositiveButton("Delete Course", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int courseIdToDelete = courseList.get(position).getCourseID();
                            databaseHelper.deleteCourse(courseIdToDelete);
                            Toast.makeText(mContext, "Course deleted successfully.", Toast.LENGTH_SHORT).show();
                            courseList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, courseList.size());
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
        return courseList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblCourseTitle, lblCourseStatus, lblCourseStart, lblCourseDue;
        ConstraintLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);

            lblCourseTitle = itemView.findViewById(R.id.lblListCourseTitle);
            lblCourseStatus = itemView.findViewById(R.id.lblListCourseStatus);
            lblCourseStart = itemView.findViewById(R.id.lblListCourseStart);
            lblCourseDue = itemView.findViewById(R.id.lblListCourseDue);
            parentLayout = itemView.findViewById(R.id.listCourseLayout);
        }
    }
}
