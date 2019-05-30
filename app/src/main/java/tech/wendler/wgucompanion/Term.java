package tech.wendler.wgucompanion;


public class Term {

    private int termID;
    private int numberOfCourses;
    private String termTitle;
    private String startDate;
    private String endDate;

    public Term() {

    }

    public Term(int id, String title, String start, String end) {
        termID = id;
        termTitle = title;
        startDate = start;
        endDate = end;
    }

    public int getTermID() {
        return termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    public String getTermTitle() {
        return termTitle;
    }

    public void setTermTitle(String termTitle) {
        this.termTitle = termTitle;
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

    public int getNumberOfCourses() {
        return numberOfCourses;
    }

    public void setNumberOfCourses(int numberOfCourses) {
        this.numberOfCourses = numberOfCourses;
    }

    public void incrementNumberOfCourses() {
        numberOfCourses++;
    }
}
