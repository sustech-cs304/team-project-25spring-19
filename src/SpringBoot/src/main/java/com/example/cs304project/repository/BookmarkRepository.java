package com.example.cs304project.repository;


import com.example.cs304project.entity.Bookmark;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {

    List<Bookmark> findByUserAndLectureAndSlide(User user, Lecture lecture, LectureSlide slide);
}
