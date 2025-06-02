package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Lecture_Slides")
public class LectureSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slide_id")
    private Long slideId;

    @ManyToOne
    @JoinColumn(name = "lecture_id")

    private Lecture lecture;


    @Column(name = "content")
    private String content;

    @Column(name = "file_url")
    private String url;

    @Column(name = "extracted_text",columnDefinition = "bytea")
    private byte[] extractedText;


    public LectureSlide(Long slideId, Lecture lecture, String content, String url, byte[] extractedText) {
        this.slideId = slideId;
        this.lecture = lecture;
        this.content = content;
        this.url = url;
        this.extractedText = extractedText;
    }



    public byte[] getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(byte[] extractedText) {
        this.extractedText = extractedText;
    }

    public Long getSlideId() {
        return slideId;
    }

    public void setSlideId(Long slideId) {
        this.slideId = slideId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public LectureSlide() {

    }
}
