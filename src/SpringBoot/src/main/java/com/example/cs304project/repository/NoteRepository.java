package com.example.cs304project.repository;


import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.entity.Note;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note,Long> {

    List<Note> findByUser(User user);

    List<Note> findByUserAndLecture(User user, Lecture lecture);
    List<Note> findByUserAndLectureAndSlide(User user, Lecture lecture, LectureSlide slide);
}
