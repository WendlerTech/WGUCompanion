package tech.wendler.wgucompanion;

public class Mentor {

    private int mentorID;
    private String mentorName;
    private String mentorEmail;
    private String mentorPhoneNum;

    public Mentor() {

    }

    public Mentor(int id, String name, String email, String number) {
        mentorID = id;
        mentorName = name;
        mentorEmail = email;
        mentorPhoneNum = number;
    }

    public int getMentorID() {
        return mentorID;
    }

    public void setMentorID(int mentorID) {
        this.mentorID = mentorID;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getMentorEmail() {
        return mentorEmail;
    }

    public void setMentorEmail(String mentorEmail) {
        this.mentorEmail = mentorEmail;
    }

    public String getMentorPhoneNum() {
        return mentorPhoneNum;
    }

    public void setMentorPhoneNum(String mentorPhoneNum) {
        this.mentorPhoneNum = mentorPhoneNum;
    }

    @Override
    public String toString() {
        return mentorName;
    }
}
