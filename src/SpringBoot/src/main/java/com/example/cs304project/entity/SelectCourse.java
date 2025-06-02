package com.example.cs304project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SelectCourses")
public class SelectCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "select_id")
    private Long selectId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public SelectCourse(Long selectId, User student, Course course) {
        this.selectId = selectId;
        this.student = student;
        this.course = course;
    }

    public SelectCourse() {

    }

    public Long getSelectId() {
        return selectId;
    }

    public void setSelectId(Long selectId) {
        this.selectId = selectId;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
