package com.example.cs304project.dto;

import lombok.*;


public class LectureDTO {

    private Long lectureId;

    private Long courseId;

    private String tittle;

    private String description;

    private Integer lectureOrder;



    public Long getLectureId() {
        return lectureId;
    }

    public void setLectureId(Long lectureId) {
        this.lectureId = lectureId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
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


}
