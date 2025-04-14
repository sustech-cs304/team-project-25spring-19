package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteId;

    @ManyToOne
    @JoinColumn(name = "user_id")

    private User user;

    @ManyToOne
    @JoinColumn(name = "lecture_id")

    private Lecture lecture;
    @ManyToOne
    @JoinColumn(name = "slide_id")

    private LectureSlide slide;


    @Column(name = "content")
    private String content;


    public Note(Long noteId, User user, Lecture lecture, LectureSlide slide, String content) {
        this.noteId = noteId;
        this.user = user;
        this.lecture = lecture;
        this.slide = slide;
        this.content = content;
    }

    public LectureSlide getSlide() {
        return slide;
    }

    public void setSlide(LectureSlide slide) {
        this.slide = slide;
    }

    public Note() {

    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
