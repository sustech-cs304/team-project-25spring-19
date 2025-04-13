package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "Submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne
    @JoinColumn(name = "exercise_id")

    private Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "user_id")

    private User student;

    @Column(name = "submission_content")
    private String content;

    @Column(name = "submission_result")
    private String result;
    @Column(name = "score")
    private BigDecimal score;
    @Column(name = "language")
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }



    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public Submission() {

    }

    public Submission(Long submissionId, Exercise exercise, User student, String content, String result, String language, BigDecimal score) {
        this.submissionId = submissionId;
        this.exercise = exercise;
        this.student = student;
        this.content = content;
        this.result = result;

        this.language = language;
        this.score = score;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
