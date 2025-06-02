package com.example.cs304project.repository;


import com.example.cs304project.entity.Exercise;
import com.example.cs304project.entity.Submission;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission,Long> {

    List<Submission> findByStudent(User student);

    List<Submission> findByExercise(Exercise exercise);

    List<Submission> findByStudentAndExercise(User student, Exercise exercise);


}
