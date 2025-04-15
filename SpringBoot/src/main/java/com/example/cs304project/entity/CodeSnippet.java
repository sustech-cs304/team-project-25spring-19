package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Code_Snippets")
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snippet_id")
    private Long codeSnippetId;

    @ManyToOne
    @JoinColumn(name = "slide_id")

    private LectureSlide slide;

    @Column(name = "code_content")
    private String context;

    @Column(name = "code_result")
    private String result;
    @Column(name = "language")
    private String language;


    public CodeSnippet(Long codeSnippetId, LectureSlide slide, String context, String result,String language) {
        this.codeSnippetId = codeSnippetId;
        this.slide = slide;
        this.context = context;
        this.result = result;

        this.language = language;
    }


    public Long getCodeSnippetId() {
        return codeSnippetId;
    }

    public void setCodeSnippetId(Long codeSnippetId) {
        this.codeSnippetId = codeSnippetId;
    }

    public LectureSlide getSlide() {
        return slide;
    }

    public void setSlide(LectureSlide slide) {
        this.slide = slide;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public CodeSnippet() {

    }
}
