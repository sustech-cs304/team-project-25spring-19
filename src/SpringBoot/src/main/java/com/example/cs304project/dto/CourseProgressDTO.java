package com.example.cs304project.dto;

import lombok.*;

import java.math.BigDecimal;


public class CourseProgressDTO {

    private Long progressId;

    private Long userId;

    private Long courseId;

    private Long lectureId;

    private Long slideId;


    public Long getSlideId() {
        return slideId;
    }

    public void setSlideId(Long slideId) {
        this.slideId = slideId;
    }

    private String state;


    private BigDecimal finished;
    private BigDecimal started;
    private BigDecimal notStart;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public void setLectureId(Long lectureId) {
        this.lectureId = lectureId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


}
