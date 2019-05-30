package tech.wendler.wgucompanion;

import java.io.Serializable;

public class Course implements Serializable {

    private int courseID;
    private int mentorID;
    private int termID;
    private String courseStatus;
    private String courseTitle;
    private String courseInfo;
    private String startDate;
    private String endDate;

    public Course() {

    }

    public Course(int courseID, int mentorID, int termID, String status, String title,
                  String info, String start, String end) {
        this.courseID = courseID;
        this.mentorID = mentorID;
        this.termID = termID;
        this.courseTitle = title;
        this.courseStatus = status;
        this.courseInfo = info;
        this.startDate = start;
        this.endDate = end;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getMentorID() {
        return mentorID;
    }

    public void setMentorID(int mentorID) {
        this.mentorID = mentorID;
    }

    public int getTermID() {
        return termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    public String getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(String courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseInfo() {
        return courseInfo;
    }

    public void setCourseInfo(String courseInfo) {
        this.courseInfo = courseInfo;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
