package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @ManyToOne
    @JoinColumn(name = "course_id")

    private Course course;
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;
    @Column(name = "lecture_order")
    private Integer lectureOrder;


    public Long getLectureId() {
        return lectureId;
    }

    public void setLectureId(Long lectureId) {
        this.lectureId = lectureId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLectureOrder() {
        return lectureOrder;
    }

    public void setLectureOrder(Integer lectureOrder) {
        this.lectureOrder = lectureOrder;
    }




    public Lecture(Long lectureId, Course course, String title, String description, Integer lectureOrder) {
        this.lectureId = lectureId;
        this.course = course;
        this.title = title;
        this.description = description;
        this.lectureOrder = lectureOrder;

    }



    public Lecture() {

    }
}
