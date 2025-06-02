package com.example.cs304project.dto;

import lombok.*;


public class CodeSnippetDTO {

    private Long codeSnippetId;

    private Long slideId;

    private String context;

    private String result;

    private String language;




    public Long getCodeSnippetId() {
        return codeSnippetId;
    }

    public void setCodeSnippetId(Long codeSnippetId) {
        this.codeSnippetId = codeSnippetId;
    }

    public Long getSlideId() {
        return slideId;
    }

    public void setSlideId(Long slideId) {
        this.slideId = slideId;
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
}
