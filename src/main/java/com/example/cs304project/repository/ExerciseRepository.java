package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Exercise;
import com.example.cs304project.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise,Long> {
    List<Exercise> findByCourseAndLecture(Course course, Lecture lecture);
}
