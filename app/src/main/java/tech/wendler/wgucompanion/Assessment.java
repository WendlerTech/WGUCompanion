package tech.wendler.wgucompanion;

import java.time.LocalDate;

public class Assessment {

    private int assessmentID;
    private int courseID;
    private String assessmentTitle;
    private String assessmentInfo;
    private LocalDate goalDate;
    private LocalDate dueDate;
    private boolean isObjective;

    public Assessment() {

    }

    public Assessment(String title, String info, LocalDate goal, LocalDate due, boolean isObjective,
                      int course) {
        assessmentTitle = title;
        assessmentInfo = info;
        goalDate = goal;
        dueDate = due;
        courseID = course;
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

    public LocalDate getGoalDate() {
        return goalDate;
    }

    public void setGoalDate(LocalDate goalDate) {
        this.goalDate = goalDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isObjective() {
        return isObjective;
    }

    public void setObjective(boolean objective) {
        isObjective = objective;
    }
}
