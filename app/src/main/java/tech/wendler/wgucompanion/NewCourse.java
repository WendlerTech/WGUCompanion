package tech.wendler.wgucompanion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NewCourse extends Fragment {

    private EditText txtCourseTitle, txtStartMonth, txtStartYear, txtEndMonth, txtEndYear, txtCourseInfo;
    private Spinner existingMentorSpinner, courseStatusSpinner;
    private DatabaseHelper databaseHelper;
    private boolean mentorDataEntered = false;
    private int enteredMentorId;
    private Term selectedTerm;
    private Course newCourse;
    private ConstraintLayout newCourseLayout;

    private ArrayAdapter<Mentor> mentorListAdapter;

    public NewCourse() {
        // Required empty public constructor
    }


    public static NewCourse newInstance() {
        return new NewCourse();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_course, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button btnAddMentor, btnCancel, btnSubmit;

        Bundle selectedTermBundle = this.getArguments();
        if (selectedTermBundle != null) {
            this.selectedTerm = (Term)selectedTermBundle.getSerializable("selectedTerm");
        }

        databaseHelper = new DatabaseHelper(getActivity());
        newCourseLayout = getView().findViewById(R.id.newCourseLayout);
        existingMentorSpinner = getView().findViewById(R.id.addCourseMentorSpinner);
        courseStatusSpinner = getView().findViewById(R.id.addCourseStatusSpinner);
        txtCourseTitle = getView().findViewById(R.id.txtNewCourseTitle);
        txtStartMonth = getView().findViewById(R.id.txtNewCourseStartMonth);
        txtStartYear = getView().findViewById(R.id.txtNewCourseStartYear);
        txtEndMonth = getView().findViewById(R.id.txtNewCourseEndMonth);
        txtEndYear = getView().findViewById(R.id.txtNewCourseEndYear);
        txtCourseInfo = getView().findViewById(R.id.txtNewCourseInfo);
        btnAddMentor = getView().findViewById(R.id.btnAddNewMentorInfo);
        btnCancel = getView().findViewById(R.id.btnCancelNewCourse);
        btnSubmit = getView().findViewById(R.id.btnSubmitNewCourse);

        //Populates course status spinner
        ArrayAdapter<CharSequence> courseStatusAdapter = ArrayAdapter.createFromResource
                (getContext(), R.array.course_status, R.layout.support_simple_spinner_dropdown_item);
        courseStatusAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        courseStatusSpinner.setAdapter(courseStatusAdapter);

        //Populates spinner with previously entered mentors
        ArrayList<Mentor> mentorList = databaseHelper.getMentorList();
        mentorListAdapter = new ArrayAdapter<>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, mentorList);
        mentorListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        existingMentorSpinner.setAdapter(mentorListAdapter);

        btnAddMentor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewMentorDialog();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goes back to course list
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mentor selectedMentor = (Mentor) existingMentorSpinner.getSelectedItem();
                if (txtCourseTitle.getText().toString().length() > 0 &&
                        txtStartMonth.getText().toString().length() > 0 &&
                        txtStartYear.getText().toString().length() > 0 &&
                        txtEndMonth.getText().toString().length() > 0 &&
                        txtEndYear.getText().toString().length() > 0 &&
                        txtCourseInfo.getText().toString().length() > 0 &&
                        courseStatusSpinner.getSelectedItem() != null &&
                        selectedMentor != null) {
                    newCourse = new Course();
                    newCourse.setMentorID(selectedMentor.getMentorID());
                    newCourse.setTermID(selectedTerm.getTermID());
                    newCourse.setCourseStatus(courseStatusSpinner.getSelectedItem().toString());
                    newCourse.setCourseTitle(txtCourseTitle.getText().toString());
                    newCourse.setCourseInfo(txtCourseInfo.getText().toString());
                    newCourse.setStartDate(txtStartMonth.getText().toString() +
                            "-" + txtStartYear.getText().toString());
                    newCourse.setEndDate(txtEndMonth.getText().toString() +
                            "-" + txtEndYear.getText().toString());

                    long newlyAddedCourseID = databaseHelper.addNewCourse(newCourse);
                    int courseID = Math.toIntExact(newlyAddedCourseID);
                    if (courseID != -1) {
                        newCourse.setCourseID(courseID);
                        Toast.makeText(getContext(), "Course added successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), ViewTerms.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Error saving course data.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Adds error messages highlighting blank fields
                    if (txtCourseTitle.getText().toString().length() == 0) {
                        txtCourseTitle.setError("Can't be blank.");
                    }
                    if (txtStartMonth.getText().toString().length() == 0) {
                        txtStartMonth.setError("Can't be blank.");
                    }
                    if (txtStartYear.getText().toString().length() == 0) {
                        txtStartYear.setError("Can't be blank.");
                    } else if (txtStartYear.getText().toString().length() < 4) {
                        txtStartYear.setError("Please enter a 4 digit year.");
                    }
                    if (txtEndMonth.getText().toString().length() == 0) {
                        txtEndMonth.setError("Can't be blank.");
                    }
                    if (txtEndYear.getText().toString().length() == 0) {
                        txtEndYear.setError("Can't be blank.");
                    } else if (txtEndYear.getText().toString().length() < 4) {
                        txtEndYear.setError("Please enter a 4 digit year.");
                    }
                    if (txtCourseInfo.getText().toString().length() == 0) {
                        txtCourseInfo.setError("Can't be blank.");
                    }
                    if (selectedMentor == null) {
                        Toast.makeText(getContext(), "Please add a course mentor.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        populateDates();
        handleDateTextWatchers();
    }

    private void populateDates() {
        String startDate = selectedTerm.getStartDate();
        String endDate = selectedTerm.getEndDate();
        String startMonth, startYear, endMonth, endYear;

        if (startDate.length() == 6) {
            startMonth = startDate.substring(0, 1);
            startYear = startDate.substring(2);
        } else {
            startMonth = startDate.substring(0, 2);
            startYear = startDate.substring(3);
        }

        if (endDate.length() == 6) {
            endMonth = endDate.substring(0, 1);
            endYear = endDate.substring(2);

        } else {
            endMonth = endDate.substring(0, 2);
            endYear = endDate.substring(3);
        }

        txtStartMonth.setText(startMonth);
        txtStartYear.setText(startYear);
        txtEndMonth.setText(endMonth);
        txtEndYear.setText(endYear);
    }

    private void handleDateTextWatchers() {
        txtStartMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtStartMonth.getText().toString().length() > 2) {
                    txtStartMonth.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtStartMonth.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtStartMonth.getText().toString()) > 12) {
                        txtStartMonth.setText("");
                    } else if (Integer.parseInt(txtStartMonth.getText().toString()) == 0) {
                        txtStartMonth.setText("");
                    }

                }
            }
        });

        txtStartYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtStartYear.getText().toString().length() > 4) {
                    txtStartYear.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtStartYear.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtStartYear.getText().toString()) > 2050) {
                        txtStartYear.setText("");
                    }
                }
            }
        });

        txtEndMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtEndMonth.getText().toString().length() > 2) {
                    txtEndMonth.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtEndMonth.getText().toString().length() > 0) {
                    //Disallows numbers outside of 1-12
                    if (Integer.parseInt(txtEndMonth.getText().toString()) > 12) {
                        txtEndMonth.setText("");
                    } else if (Integer.parseInt(txtEndMonth.getText().toString()) == 0) {
                        txtEndMonth.setText("");
                    }

                }
            }
        });

        txtEndYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtEndYear.getText().toString().length() > 4) {
                    txtEndYear.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtEndYear.getText().toString().length() > 0) {
                    //Disallows high, unrealistic years
                    if (Integer.parseInt(txtEndYear.getText().toString()) > 2050) {
                        txtEndYear.setText("");
                    }
                }
            }
        });
    }

    private void showNewMentorDialog() {
        final AlertDialog.Builder mentorDialog = new AlertDialog.Builder(getContext());
        TextView lblMentorName = new TextView(getContext());
        TextView lblMentorEmail = new TextView(getContext());
        TextView lblMentorPhone = new TextView(getContext());
        final EditText txtMentorName = new EditText(getContext());
        final EditText txtMentorEmail = new EditText(getContext());
        final EditText txtMentorPhone = new EditText(getContext());

        lblMentorName.setText(R.string.new_mentor_dialog_name);
        lblMentorEmail.setText(R.string.new_mentor_dialog_email);
        lblMentorPhone.setText(R.string.new_mentor_dialog_phone);

        txtMentorName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        txtMentorEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        txtMentorPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        txtMentorPhone.setMaxLines(1);

        lblMentorName.setPadding(0, 40, 0, 0);
        lblMentorEmail.setPadding(0, 40, 0, 0);
        lblMentorPhone.setPadding(0, 40, 0, 0);

        ScrollView scrollViewLayout = new ScrollView(getContext());
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(lblMentorName);
        dialogLayout.addView(txtMentorName);
        dialogLayout.addView(lblMentorEmail);
        dialogLayout.addView(txtMentorEmail);
        dialogLayout.addView(lblMentorPhone);
        dialogLayout.addView(txtMentorPhone);

        //Layout padding also shifts edit text fields
        dialogLayout.setPadding(40, 0, 40, 0);

        scrollViewLayout.addView(dialogLayout);
        mentorDialog.setView(scrollViewLayout);
        mentorDialog.setTitle(R.string.new_mentor_dialog_title);

        mentorDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = txtMentorName.getText().toString();
                String email = txtMentorEmail.getText().toString();
                String phone = txtMentorPhone.getText().toString();

                long newMentorId = databaseHelper.addNewMentor(name, email, phone);
                enteredMentorId = Math.toIntExact(newMentorId);

                if (enteredMentorId != -1) {
                    mentorDataEntered = true;
                    //Adds new mentor to the drop down spinner & selects it without DB re-read
                    Mentor newlyAddedMentor = new Mentor(enteredMentorId, name, email, phone);
                    mentorListAdapter.add(newlyAddedMentor);
                    mentorListAdapter.notifyDataSetChanged();
                    existingMentorSpinner.setSelection(mentorListAdapter.getPosition(newlyAddedMentor));
                    Toast.makeText(getContext(), "Mentor saved successfully.", Toast.LENGTH_SHORT).show();
                    newCourseLayout.requestFocus(); //Minimizes keyboard after entering new mentor
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //User cancelled, do nothing
                    }
                });

        final AlertDialog alert = mentorDialog.create();

        txtMentorName.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
                handleSubmitButton();
            }
        });

        txtMentorEmail.addTextChangedListener(new TextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
                handleSubmitButton();

            }
        });

        txtMentorPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            private void handleSubmitButton() {
                final Button btnSubmit = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mentorDataEntered) {
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
                if (txtMentorName.getText().toString().length() > 0 &&
                        txtMentorEmail.getText().toString().length() > 0 &&
                        txtMentorPhone.getText().toString().length() > 9) {
                    mentorDataEntered = true;
                } else {
                    mentorDataEntered = false;
                }
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
            ((ViewTerms) getActivity()).setActionBarTitle("New Course");
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
