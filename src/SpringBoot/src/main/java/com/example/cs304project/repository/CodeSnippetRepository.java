package com.example.cs304project.repository;


import com.example.cs304project.entity.CodeSnippet;
import com.example.cs304project.entity.LectureSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeSnippetRepository extends JpaRepository<CodeSnippet,Long> {


    List<CodeSnippet> findBySlide(LectureSlide slide);
}
