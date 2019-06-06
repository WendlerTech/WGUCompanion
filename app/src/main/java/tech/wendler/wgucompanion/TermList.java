package tech.wendler.wgucompanion;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
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

public class TermList extends Fragment {

    private boolean endDateAfterStart = false;
    private ArrayList<Term> termArrayList;
    private ArrayList<Course> courseArrayList;
    private DatabaseHelper databaseHelper;
    private TermRecyclerViewAdapter adapter;
    private Term emptyTerm;

    public TermList() {
        // Required empty public constructor
    }

    public static TermList newInstance() {
        return new TermList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_term_list, container, false);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.termFloatingBtn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewTermDialog();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        databaseHelper = new DatabaseHelper(getContext());

        termArrayList = populateTermList();
        openRecyclerView();
    }

    private ArrayList<Term> populateTermList() {
        String title, start, end;
        int termID, courseID;

        termArrayList = new ArrayList<>();
        courseArrayList = new ArrayList<>();

        String queryString = "SELECT courseID, termID FROM Courses;";

        try (Cursor courseCursor = databaseHelper.getData(queryString)) {
            while (courseCursor.moveToNext()) {
                courseID = courseCursor.getInt(courseCursor.getColumnIndex("courseID"));
                termID = courseCursor.getInt(courseCursor.getColumnIndex("termID"));

                Course course = new Course();
                course.setCourseID(courseID);
                course.setTermID(termID);

                courseArrayList.add(course);
            }
        }

        queryString = "SELECT * FROM Terms";

        try (Cursor cursor = databaseHelper.getData(queryString)) {
            if (!cursor.moveToFirst()) {
                emptyTerm = new Term();
                emptyTerm.setTermTitle("You haven't added any terms yet.");
                emptyTerm.setStartDate("");
                emptyTerm.setEndDate("");
                termArrayList.add(emptyTerm);
            } else {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    termID = cursor.getInt(cursor.getColumnIndex("termID"));
                    title = cursor.getString(cursor.getColumnIndex("term_title"));
                    start = cursor.getString(cursor.getColumnIndex("start_date"));
                    end = cursor.getString(cursor.getColumnIndex("end_date"));

                    Term term = new Term(termID, title, start, end);

                    for (Course course : courseArrayList) {
                        if (course.getTermID() == termID) {
                            term.incrementNumberOfCourses();
                        }
                    }

                    termArrayList.add(term);
                }
            }
        }

        return termArrayList;
    }

    public void openRecyclerView() {
        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getFragmentManager().beginTransaction();
            RecyclerView recyclerView = getView().findViewById(R.id.termRecyclerView);
            adapter = new TermRecyclerViewAdapter(getContext(), termArrayList,
                    fragmentTransaction);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (adapter.getItemCount() != 0) {
            Toast.makeText(getContext(), "Click to view a term's courses," +
                            "\nor long press to delete the term.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showNewTermDialog() {
        final AlertDialog.Builder newTermDialog = new AlertDialog.Builder(getContext());
        TextView lblTermTitle = new TextView(getContext());
        TextView lblStartDate = new TextView(getContext());
        TextView lblEndDate = new TextView(getContext());
        final EditText txtTermTitle = new EditText(getContext());
        final EditText txtMonthStart = new EditText(getContext());
        final EditText txtYearStart = new EditText(getContext());
        final EditText txtMonthEnd = new EditText(getContext());
        final EditText txtYearEnd = new EditText(getContext());

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

        //Start date text fields
        LinearLayout startDateLayout = new LinearLayout(getContext());
        startDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        startDateLayout.addView(txtMonthStart);
        startDateLayout.addView(txtYearStart);

        //End date text fields
        LinearLayout endDateLayout = new LinearLayout(getContext());
        endDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        endDateLayout.addView(txtMonthEnd);
        endDateLayout.addView(txtYearEnd);

        //Makes the soft keyboard next button work as intended
        txtTermTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearStart.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtMonthEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        txtYearEnd.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        ScrollView scrollViewLayout = new ScrollView(getContext());
        LinearLayout dialogLayout = new LinearLayout(getContext());
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

        newTermDialog.setTitle(R.string.new_term_dialog);

        newTermDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String termTitle, termStartDate, termEndDate;

                if (Integer.parseInt(txtYearStart.getText().toString()) <= 50) {
                    termStartDate = txtMonthStart.getText().toString() + "-20"
                            + txtYearStart.getText().toString();
                } else {
                    termStartDate = txtMonthStart.getText().toString() + "-"
                            + txtYearStart.getText().toString();
                }
                if (Integer.parseInt(txtYearEnd.getText().toString()) <= 50) {
                    termEndDate = txtMonthEnd.getText().toString() + "-20"
                            + txtYearEnd.getText().toString();
                } else {
                    termEndDate = txtMonthEnd.getText().toString() + "-"
                            + txtYearEnd.getText().toString();
                }

                termTitle = txtTermTitle.getText().toString();

                long dbInsertResult = databaseHelper.addNewTerm(termTitle, termStartDate, termEndDate);
                int newlyAddedTermID = Math.toIntExact(dbInsertResult);

                if (newlyAddedTermID != -1) {
                    Toast.makeText(getContext(), "Term added successfully.", Toast.LENGTH_SHORT).show();

                    //Adds new term to list & tells the recycler view to refresh
                    Term newlyAddedTerm = new Term(newlyAddedTermID, termTitle, termStartDate, termEndDate);
                    termArrayList.add(newlyAddedTerm);
                    if (termArrayList.contains(emptyTerm)) {
                        adapter.notifyItemRemoved(termArrayList.indexOf(emptyTerm));
                        termArrayList.remove(emptyTerm);
                    }
                    adapter.notifyItemInserted(termArrayList.indexOf(newlyAddedTerm));

                } else {
                    Toast.makeText(getContext(), "Error saving term data.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        newTermDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Do nothing
            }
        });

        final AlertDialog alert = newTermDialog.create();

        /*
        Start date text watchers keep dates within bounds & ensures end date is after start date
         */
        txtMonthStart.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
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
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
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
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
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
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
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
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
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

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((ViewTerms) getActivity()).setActionBarTitle("Terms");
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
