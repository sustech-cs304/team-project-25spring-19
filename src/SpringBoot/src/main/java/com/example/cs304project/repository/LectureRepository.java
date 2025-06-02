package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture,Long> {


    List<Lecture> findByCourseAndLectureOrder(Course course, Integer lectureOrder);

    List<Lecture> findByCourse(Course course);
}
