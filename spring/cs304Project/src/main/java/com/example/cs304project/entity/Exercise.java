package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Long exerciseId;

    @ManyToOne
    @JoinColumn(name = "course_id")

    private Course course;

    @ManyToOne
    @JoinColumn(name = "lecture_id")

    private Lecture lecture;

    @Column(name = "question")
    private String question;

    @Column(name = "answer")
    private String answer;
    public Exercise(Long exerciseId, Course course, Lecture lecture, String question, String answer) {
        this.exerciseId = exerciseId;
        this.course = course;
        this.lecture = lecture;
        this.question = question;
        this.answer = answer;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }




    public Exercise() {

    }
}
