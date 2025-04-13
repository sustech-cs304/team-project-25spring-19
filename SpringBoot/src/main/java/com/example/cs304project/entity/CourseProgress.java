package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "Course_Progress")
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne
    @JoinColumn(name = "user_id")

    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id")

    private Course course;

    @ManyToOne
    @JoinColumn(name = "lecture_id")

    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "slide_id")

    private LectureSlide slide;

    @Column(name = "status")
    private String state;

    @Column(name = "finished")
    private BigDecimal finished;
    @Column(name = "started")
    private BigDecimal started;
    @Column(name = "not_start")
    private BigDecimal notStart;

    public CourseProgress(Long progressId, User user, Course course, Lecture lecture, LectureSlide slide, String state, BigDecimal finished, BigDecimal started, BigDecimal notStart) {
        this.progressId = progressId;
        this.user = user;
        this.course = course;
        this.lecture = lecture;
        this.slide = slide;
        this.state = state;
        this.finished = finished;
        this.started = started;
        this.notStart = notStart;
    }

    public BigDecimal getFinished() {
        return finished;
    }

    public void setFinished(BigDecimal finished) {
        this.finished = finished;
    }

    public BigDecimal getStarted() {
        return started;
    }

    public void setStarted(BigDecimal started) {
        this.started = started;
    }

    public BigDecimal getNotStart() {
        return notStart;
    }

    public void setNotStart(BigDecimal notStart) {
        this.notStart = notStart;
    }

    public Long getProgressId() {
        return progressId;
    }

    public void setProgressId(Long progressId) {
        this.progressId = progressId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public LectureSlide getSlide() {
        return slide;
    }

    public void setSlide(LectureSlide slide) {
        this.slide = slide;
    }

    public CourseProgress() {

    }
}
