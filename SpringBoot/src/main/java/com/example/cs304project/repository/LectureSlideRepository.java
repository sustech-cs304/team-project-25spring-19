package com.example.cs304project.repository;


import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.LectureSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureSlideRepository extends JpaRepository<LectureSlide,Long> {

    List<LectureSlide> findByLecture(Lecture lecture);
}
