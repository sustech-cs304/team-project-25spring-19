package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Discussion;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion,Long> {
    List<Discussion> findByCourse(Course course);

    List<Discussion> findByUser(User user);

    List<Discussion> findByCourseAndLecture(Course course,Lecture lecture);
}
