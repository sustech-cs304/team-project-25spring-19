package com.example.cs304project.repository;

import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.SelectCourse;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelectCourseRepository extends JpaRepository<SelectCourse,Long> {

    List<SelectCourse> findByStudent(User user);

    List<SelectCourse> findByCourse(Course course);
}
