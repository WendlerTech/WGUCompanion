package tech.wendler.wgucompanion;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "WGU_Companion_Database";
    private final static String COURSE_TABLE_NAME = "Courses";
    private final static String COURSE_COL0 = "courseID";
    private final static String COURSE_COL1 = "mentorID";
    private final static String COURSE_COL2 = "termID";
    private final static String COURSE_COL3 = "course_status";
    private final static String COURSE_COL4 = "course_title";
    private final static String COURSE_COL5 = "course_info";
    private final static String COURSE_COL6 = "start_date";
    private final static String COURSE_COL7 = "end_date";

    private final static String TERM_TABLE_NAME = "Terms";
    private final static String TERM_COL0 = "termID";
    private final static String TERM_COL1 = "term_title";
    private final static String TERM_COL2 = "start_date";
    private final static String TERM_COL3 = "end_date";

    private final static String ASSESSMENT_TABLE_NAME = "Assessments";
    private final static String ASSESSMENT_COL0 = "assessmentID";
    private final static String ASSESSMENT_COL1 = "courseID";
    private final static String ASSESSMENT_COL2 = "title";
    private final static String ASSESSMENT_COL3 = "goal_date";
    private final static String ASSESSMENT_COL4 = "due_date";
    private final static String ASSESSMENT_COL5 = "assessment_info";
    private final static String ASSESSMENT_COL6 = "is_objective";

    private final static String NOTE_TABLE_NAME = "Notes";
    private final static String NOTE_COL0 = "noteID";
    private final static String NOTE_COL1 = "courseID";
    private final static String NOTE_COL2 = "note";
    private final static String NOTE_COL3 = "noteTitle";

    private final static String MENTOR_TABLE_NAME = "Mentors";
    private final static String MENTOR_COL0 = "mentorID";
    private final static String MENTOR_COL1 = "name";
    private final static String MENTOR_COL2 = "email";
    private final static String MENTOR_COL3 = "phone_number";

    private final static String STUDENT_TABLE_NAME = "Student";
    private final static String STUDENT_COL0 = "student_name";
    private final static String STUDENT_COL1 = "student_degree";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTermTable = "CREATE TABLE " + TERM_TABLE_NAME + " (" +
                TERM_COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TERM_COL1 + " STRING, " +
                TERM_COL2 + " DATETIME, " +
                TERM_COL3 + " DATETIME);";

        String createCourseTable = "CREATE TABLE " + COURSE_TABLE_NAME + " (" +
                COURSE_COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COURSE_COL1 + " INTEGER, " +
                COURSE_COL2 + " INTEGER, " +
                COURSE_COL3 + " STRING, " +
                COURSE_COL4 + " STRING, " +
                COURSE_COL5 + " STRING, " +
                COURSE_COL6 + " DATETIME, " +
                COURSE_COL7 + " DATETIME);";

        String createAssessmentTable = "CREATE TABLE " + ASSESSMENT_TABLE_NAME + " (" +
                ASSESSMENT_COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ASSESSMENT_COL1 + " INTEGER, " +
                ASSESSMENT_COL2 + " STRING, " +
                ASSESSMENT_COL3 + " DATETIME, " +
                ASSESSMENT_COL4 + " DATETIME, " +
                ASSESSMENT_COL5 + " STRING, " +
                ASSESSMENT_COL6 + " BOOLEAN);";

        String createNoteTable = "CREATE TABLE " + NOTE_TABLE_NAME + " (" +
                NOTE_COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTE_COL1 + " INTEGER, " +
                NOTE_COL2 + " STRING, " +
                NOTE_COL3 + " STRING);";

        String createMentorTable = "CREATE TABLE " + MENTOR_TABLE_NAME + " (" +
                MENTOR_COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MENTOR_COL1 + " STRING, " +
                MENTOR_COL2 + " STRING, " +
                MENTOR_COL3 + " STRING);";

        String createStudentTable = "CREATE TABLE " + STUDENT_TABLE_NAME + " (" +
                STUDENT_COL0 + " STRING, " +
                STUDENT_COL1 + " STRING);";

        sqLiteDatabase.execSQL(createTermTable);
        sqLiteDatabase.execSQL(createCourseTable);
        sqLiteDatabase.execSQL(createAssessmentTable);
        sqLiteDatabase.execSQL(createNoteTable);
        sqLiteDatabase.execSQL(createMentorTable);
        sqLiteDatabase.execSQL(createStudentTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    Cursor getData(String queryString) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(queryString, null);
    }

    boolean addStudentData(String name, String degree) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(STUDENT_COL0, name);
        contentValues.put(STUDENT_COL1, degree);

        //Replaces data if it already exists
        long result = db.insertWithOnConflict(STUDENT_TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

        return result != -1;
    }

    //Returns user's name & degree
    Student getStudentData() {
        Student student = new Student();
        String queryString = "SELECT * FROM " + STUDENT_TABLE_NAME + ";";

        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(queryString, null)) {
            while (cursor.moveToNext()) {
                student.setStudentName(cursor.getString(cursor.getColumnIndex(STUDENT_COL0)));
                student.setStudentDegree(cursor.getString(cursor.getColumnIndex(STUDENT_COL1)));
            }
        }

        return student;
    }

    //Saves new term, returns newly created term ID
    long addNewTerm(String title, String start, String end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TERM_COL1, title);
        contentValues.put(TERM_COL2, start);
        contentValues.put(TERM_COL3, end);

        return db.insert(TERM_TABLE_NAME, null, contentValues);
    }

    void deleteTerm(int termIdToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + TERM_TABLE_NAME + " WHERE " +
                TERM_COL0 + " = " + termIdToDelete + ";";
        db.execSQL(queryString);
        db.close();
    }

    //Saves new mentor, returns newly created mentor ID
    long addNewMentor(String name, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MENTOR_COL1, name);
        contentValues.put(MENTOR_COL2, email);
        contentValues.put(MENTOR_COL3, phone);

        return db.insert(MENTOR_TABLE_NAME, null, contentValues);
    }

    void updateMentor(Mentor mentorToUpdate) {
        SQLiteDatabase db = this.getWritableDatabase();

        String name, email, phone;
        int mentorID;

        name = mentorToUpdate.getMentorName();
        email = mentorToUpdate.getMentorEmail();
        phone = mentorToUpdate.getMentorPhoneNum();
        mentorID = mentorToUpdate.getMentorID();

        String queryString = "UPDATE " + MENTOR_TABLE_NAME +
                " SET " + MENTOR_COL1 + " = '" + name + "', " +
                MENTOR_COL2 + " = '" + email + "', " +
                MENTOR_COL3 + " = '" + phone + "' WHERE " +
                MENTOR_COL0 + " = " + mentorID + ";";

        db.execSQL(queryString);
        db.close();
    }

    //Returns list of all existing mentors
    ArrayList<Mentor> getMentorList() {
        ArrayList<Mentor> mentorList = new ArrayList<>();
        String queryString = "SELECT * FROM " + MENTOR_TABLE_NAME + ";";
        String name, email, phone;
        int mentorId;

        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db .rawQuery(queryString, null)) {
            while (cursor.moveToNext()) {
                mentorId = cursor.getInt(cursor.getColumnIndex(MENTOR_COL0));
                name = cursor.getString(cursor.getColumnIndex(MENTOR_COL1));
                email = cursor.getString(cursor.getColumnIndex(MENTOR_COL2));
                phone = cursor.getString(cursor.getColumnIndex(MENTOR_COL3));

                Mentor mentor = new Mentor(mentorId, name, email, phone);

                mentorList.add(mentor);
            }
        }

        return mentorList;
    }

    //Saves new course, returns newly created course ID
    long addNewCourse(Course courseToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COURSE_COL1, courseToAdd.getMentorID());
        contentValues.put(COURSE_COL2, courseToAdd.getTermID());
        contentValues.put(COURSE_COL3, courseToAdd.getCourseStatus());
        contentValues.put(COURSE_COL4, courseToAdd.getCourseTitle());
        contentValues.put(COURSE_COL5, courseToAdd.getCourseInfo());
        contentValues.put(COURSE_COL6, courseToAdd.getStartDate());
        contentValues.put(COURSE_COL7, courseToAdd.getEndDate());

        return db.insert(COURSE_TABLE_NAME, null, contentValues);
    }

    void updateCourse(Course courseToUpdate) {
        SQLiteDatabase db = this.getWritableDatabase();

        int courseID, mentorID, termID;
        String status, title, info, startDate, endDate;

        courseID = courseToUpdate.getCourseID();
        mentorID = courseToUpdate.getMentorID();
        termID = courseToUpdate.getTermID();
        status = courseToUpdate.getCourseStatus();
        title = courseToUpdate.getCourseTitle();
        info = courseToUpdate.getCourseInfo();
        startDate = courseToUpdate.getStartDate();
        endDate = courseToUpdate.getEndDate();

        String queryString = "UPDATE " + COURSE_TABLE_NAME +
                " SET " + COURSE_COL1 + " = " + mentorID + ", " +
                COURSE_COL2 + " = " + termID + ", " +
                COURSE_COL3 + " = '" + status + "', "+
                COURSE_COL4 + " = '" + title + "', "+
                COURSE_COL5 + " = '" + info + "', "+
                COURSE_COL6 + " = '" + startDate + "', "+
                COURSE_COL7 + " = '" + endDate + "' WHERE " +
                COURSE_COL0 + " = " + courseID + ";";

        db.execSQL(queryString);
        db.close();
    }

    void deleteCourse(int courseIdToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + COURSE_TABLE_NAME + " WHERE " +
                COURSE_COL0 + " = " + courseIdToDelete + ";";
        db.execSQL(queryString);
        db.close();
    }

    //Saves new note, returns newly created note ID
    long addNewNote(Note noteToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTE_COL1, noteToAdd.getCourseID());
        contentValues.put(NOTE_COL2, noteToAdd.getNote());
        contentValues.put(NOTE_COL3, noteToAdd.getNoteTitle());

        return db.insert(NOTE_TABLE_NAME, null, contentValues);
    }

    void updateNote(Note noteToUpdate) {
        SQLiteDatabase db = this.getWritableDatabase();

        int noteID, courseID;
        String noteTitle, noteContent;

        noteID = noteToUpdate.getNoteID();
        courseID = noteToUpdate.getCourseID();
        noteTitle = noteToUpdate.getNoteTitle();
        noteContent = noteToUpdate.getNote();

        String queryString = "UPDATE " + NOTE_TABLE_NAME +
                " SET " + NOTE_COL1 + " = " + courseID + ", " +
                NOTE_COL2 + " = '" + noteContent + "', " +
                NOTE_COL3 + " = '" + noteTitle + "' WHERE " +
                NOTE_COL0 + " = " + noteID + ";";

        db.execSQL(queryString);
        db.close();
    }

    void deleteNote(int noteIdToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + NOTE_TABLE_NAME + " WHERE " +
                NOTE_COL0 + " = " + noteIdToDelete + ";";
        db.execSQL(queryString);
        db.close();
    }
}
