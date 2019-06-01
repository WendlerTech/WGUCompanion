package tech.wendler.wgucompanion;

import java.io.Serializable;

public class Note implements Serializable {

    private int noteID;
    private int courseID;
    private String note;
    private String noteTitle;

    public Note() {

    }

    public Note(int noteID, int courseID, String note, String noteTitle) {
        this.noteID = noteID;
        this.courseID = courseID;
        this.note = note;
        this.noteTitle = noteTitle;
    }

    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }
}
