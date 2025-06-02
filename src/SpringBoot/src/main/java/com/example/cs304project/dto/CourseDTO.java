package com.example.cs304project.dto;

import lombok.*;


public class CourseDTO {

    private Long courseId;

    private String tittle;

    private String description;

    private Long instructorId;

    private String lectureNum;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return tittle;
    }

    public void setTitle(String title) {
        this.tittle = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public String getLectureNum() {
        return lectureNum;
    }

    public void setLectureNum(String lectureNum) {
        this.lectureNum = lectureNum;
    }
}
