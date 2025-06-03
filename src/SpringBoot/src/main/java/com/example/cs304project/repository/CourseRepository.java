package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Long> {

    List<Course> findCourseBytitle(String title);

    List<Course> findCourseByInstructor(User instructor);

    Course findCourseBytitleAndInstructor(String string,User user);
}
