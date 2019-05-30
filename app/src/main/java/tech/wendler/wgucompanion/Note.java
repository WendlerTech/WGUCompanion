package tech.wendler.wgucompanion;

public class Note {

    private int noteID;
    private int courseID;
    private String note;

    public Note() {

    }

    public Note(String note, int course) {
        courseID = course;
        this.note = note;
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
}
