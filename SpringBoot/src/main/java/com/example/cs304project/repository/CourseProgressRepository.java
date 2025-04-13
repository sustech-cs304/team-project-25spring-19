package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.CourseProgress;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseProgressRepository extends JpaRepository<CourseProgress,Long> {
    List<CourseProgress> findByUser(User user);
    List<CourseProgress> findByUserAndCourse(User user, Course course);
    List<CourseProgress> findByUserAndCourseAndLecture(User user, Course course, Lecture lecture);
}
