package tech.wendler.wgucompanion;

public class Student {

    private String studentName, studentDegree;

    public Student() {

    }

    public Student(String name, String degree) {
        studentName = name;
        studentDegree = degree;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentDegree() {
        return studentDegree;
    }

    public void setStudentDegree(String studentDegree) {
        this.studentDegree = studentDegree;
    }
}
