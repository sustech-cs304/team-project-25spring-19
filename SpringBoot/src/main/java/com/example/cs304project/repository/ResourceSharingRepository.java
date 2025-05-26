package com.example.cs304project.repository;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.ResourceSharing;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceSharingRepository extends JpaRepository<ResourceSharing,Long> {

    List<ResourceSharing> findByUser(User user);

    List<ResourceSharing> findByCourse(Course course);

    List<ResourceSharing> findByCourseAndLecture(Course course,Lecture lecture);


}
