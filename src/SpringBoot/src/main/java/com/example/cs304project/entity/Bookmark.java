package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Bookmarks")
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "slide_id")
    private LectureSlide slide;

    @Column(name = "page_num")
    private Integer pageNum;

    @Column(name = "annotation_data")
    private String data;

    public Bookmark(Long bookmarkId, User user, Lecture lecture, LectureSlide slide, Integer pageNum, String data) {
        this.bookmarkId = bookmarkId;
        this.user = user;
        this.lecture = lecture;
        this.slide = slide;
        this.pageNum = pageNum;
        this.data = data;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
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

    public LectureSlide getSlide() {
        return slide;
    }

    public void setSlide(LectureSlide slide) {
        this.slide = slide;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Bookmark() {

    }
}
