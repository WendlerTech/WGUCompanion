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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private boolean endDateAfterStart = false;

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
                        selectedTerm = termList.get(position);
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
                                .setNeutralButton("Edit Term", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        showEditTermDialog();
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
                        selectedTerm = termList.get(position);
                        AlertDialog.Builder failDeleteDialog = new AlertDialog.Builder(mContext);
                        failDeleteDialog.setTitle("Error");
                        failDeleteDialog.setMessage("You can't delete a term with courses assigned to it." +
                                "\n\nRemove the courses first, then try again.");
                        failDeleteDialog.setNeutralButton("Edit Term", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showEditTermDialog();

                            }
                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //User cancelled, nothing happens
                                    }
                                });

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

    private void showEditTermDialog() {
        final android.support.v7.app.AlertDialog.Builder newTermDialog = new android.support.v7.app.AlertDialog.Builder(mContext);
        TextView lblTermTitle = new TextView(mContext);
        TextView lblStartDate = new TextView(mContext);
        TextView lblEndDate = new TextView(mContext);
        final EditText txtTermTitle = new EditText(mContext);
        final EditText txtMonthStart = new EditText(mContext);
        final EditText txtYearStart = new EditText(mContext);
        final EditText txtMonthEnd = new EditText(mContext);
        final EditText txtYearEnd = new EditText(mContext);

        lblTermTitle.setPadding(0, 60, 40, 0);
        lblStartDate.setPadding(0, 40, 0, 0);
        lblEndDate.setPadding(0, 40, 0, 0);

        lblTermTitle.setText(R.string.new_term_dialog_title);
        lblStartDate.setText(R.string.new_term_dialog_start_date);
        lblEndDate.setText(R.string.new_term_dialog_end_date);

        txtMonthStart.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtYearStart.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtMonthEnd.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtYearEnd.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtTermTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        txtMonthStart.setHint("MM");
        txtYearStart.setHint("YYYY");
        txtMonthEnd.setHint("MM");
        txtYearEnd.setHint("YYYY");

        txtTermTitle.setText(selectedTerm.getTermTitle());

        //Start date text fields
        LinearLayout startDateLayout = new LinearLayout(mContext);
        startDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        startDateLayout.addView(txtMonthStart);
        startDateLayout.addView(txtYearStart);

        //End date text fields
        LinearLayout endDateLayout = new LinearLayout(mContext);
        endDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        endDateLayout.addView(txtMonthEnd);
        endDateLayout.addView(txtYearEnd);

        //Makes the soft keyboard next button work as intended
        txtTermTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        ScrollView scrollViewLayout = new ScrollView(mContext);
        LinearLayout dialogLayout = new LinearLayout(mContext);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(lblTermTitle);
        dialogLayout.addView(txtTermTitle);
        dialogLayout.addView(lblStartDate);
        dialogLayout.addView(startDateLayout);
        dialogLayout.addView(lblEndDate);
        dialogLayout.addView(endDateLayout);

        dialogLayout.setPadding(40, 0, 40, 0);

        scrollViewLayout.addView(dialogLayout);
        newTermDialog.setView(scrollViewLayout);

        newTermDialog.setTitle("Update Term");

        newTermDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String termTitle, termStartDate, termEndDate;

                if (Integer.parseInt(txtYearStart.getText().toString()) < 1000 ||
                        Integer.parseInt(txtYearEnd.getText().toString()) < 1000) {
                    Toast.makeText(mContext, "Please enter a 4 digit year.", Toast.LENGTH_SHORT).show();
                } else {
                    termStartDate = txtMonthStart.getText().toString() + "-"
                            + txtYearStart.getText().toString();
                    termEndDate = txtMonthEnd.getText().toString() + "-"
                            + txtYearEnd.getText().toString();

                    termTitle = txtTermTitle.getText().toString();

                    Term termToUpdate = new Term();
                    termToUpdate.setTermID(selectedTerm.getTermID());
                    termToUpdate.setTermTitle(termTitle);
                    termToUpdate.setStartDate(termStartDate);
                    termToUpdate.setEndDate(termEndDate);

                    databaseHelper.updateTerm(termToUpdate);

                    termList.set(termList.indexOf(selectedTerm), termToUpdate);
                    selectedTerm = termToUpdate;
                    notifyItemChanged(termList.indexOf(selectedTerm));
                }
            }
        });

        newTermDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Do nothing
            }
        });

        final android.support.v7.app.AlertDialog alert = newTermDialog.create();

        /*
        Start date text watchers keep dates within bounds & ensures end date is after start date
         */
        txtMonthStart.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtMonthStart.getText().toString().length() > 2) {
                    txtMonthStart.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtMonthStart.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtMonthStart.getText().toString()) > 12) {
                        txtMonthStart.setText("");
                    } else if (Integer.parseInt(txtMonthStart.getText().toString()) == 0) {
                        txtMonthStart.setText("");
                    }
                    if (txtMonthEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0 &&
                            txtYearEnd.getText().toString().length() > 0 &&
                            txtMonthStart.getText().toString().length() > 0) {
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                        //Disables save button if start year is after the end year
                        else if (Integer.parseInt(txtYearStart.getText().toString()) <
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = true;
                        } else {
                            endDateAfterStart = false;
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtYearStart.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtYearStart.getText().toString().length() > 4) {
                    txtYearStart.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtYearStart.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtYearStart.getText().toString()) > 2050) {
                        txtYearStart.setText("");
                    }
                    if (txtYearEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0) {
                        //Disables save button if start year is after the end year
                        if (Integer.parseInt(txtYearStart.getText().toString()) <
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = true;
                        } else {
                            endDateAfterStart = false;
                        }
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        /*
        End date text watchers keep dates within bounds & ensures end date is after start date
         */
        txtMonthEnd.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtMonthEnd.getText().toString().length() > 2) {
                    txtMonthEnd.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtMonthEnd.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtMonthEnd.getText().toString()) > 12) {
                        txtMonthEnd.setText("");
                    } else if (Integer.parseInt(txtMonthEnd.getText().toString()) == 0) {
                        txtMonthEnd.setText("");
                    }
                    if (txtMonthStart.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0 &&
                            txtYearEnd.getText().toString().length() > 0 &&
                            txtMonthEnd.getText().toString().length() > 0) {
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtYearEnd.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtYearEnd.getText().toString().length() > 4) {
                    txtYearEnd.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtYearEnd.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtYearEnd.getText().toString()) > 2050) {
                        txtYearEnd.setText("");
                    }
                    if (txtYearEnd.getText().toString().length() > 0 &&
                            txtYearStart.getText().toString().length() > 0) {
                        //Disables save button if start year is after the end year
                        if (Integer.parseInt(txtYearStart.getText().toString()) >
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            endDateAfterStart = false;
                        } else {
                            endDateAfterStart = true;
                        }
                        //If years match, disables save button if start month is after end month
                        if (Integer.parseInt(txtYearStart.getText().toString()) ==
                                Integer.parseInt(txtYearEnd.getText().toString())) {
                            if (Integer.parseInt(txtMonthStart.getText().toString()) >=
                                    Integer.parseInt(txtMonthEnd.getText().toString())) {
                                endDateAfterStart = false;
                            } else {
                                endDateAfterStart = true;
                            }
                        }
                    }
                } else {
                    endDateAfterStart = false;
                }
                handleSubmitButton();
            }
        });

        txtTermTitle.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                if (endDateAfterStart && txtTermTitle.getText().toString().length() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handleSubmitButton();
            }
        });

        alert.show();

        alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

}
