package tech.wendler.wgucompanion;

import java.io.Serializable;

public class Assessment implements Serializable {

    private int assessmentID;
    private int courseID;
    private String assessmentTitle;
    private String assessmentInfo;
    private String goalDate;
    private String dueDate;
    private boolean isObjective;

    public Assessment() {

    }

    public Assessment(int assessmentID, int courseID, String title, String info, String goalDate,
                      String dueDate, boolean isObjective) {
        this.assessmentID = assessmentID;
        this.courseID = courseID;
        this.assessmentTitle = title;
        this.assessmentInfo = info;
        this.goalDate = goalDate;
        this.dueDate = dueDate;
        this.isObjective = isObjective;
    }

    public int getAssessmentID() {
        return assessmentID;
    }

    public void setAssessmentID(int assessmentID) {
        this.assessmentID = assessmentID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
    }

    public String getAssessmentInfo() {
        return assessmentInfo;
    }

    public void setAssessmentInfo(String assessmentInfo) {
        this.assessmentInfo = assessmentInfo;
    }

    public String getGoalDate() {
        return goalDate;
    }

    public void setGoalDate(String goalDate) {
        this.goalDate = goalDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isObjective() {
        return isObjective;
    }

    public void setObjective(boolean objective) {
        isObjective = objective;
    }
}
