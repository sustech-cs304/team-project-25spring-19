package com.example.cs304project.service;

import com.example.cs304project.entity.*;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.BookmarkRepository;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.LectureSlideRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkService {
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private LectureSlideRepository lectureSlideRepository;
    //创建书签
    public Bookmark createBookmark(Long userId, Long lectureId, Long slideId,Bookmark bookmark){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要创建书签的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要创建书签的讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要创建书签的课件不存在"));
        bookmark.setUser(user);
        bookmark.setLecture(lecture);
        bookmark.setSlide(slide);
        return bookmarkRepository.save(bookmark);
    }

/*
    //更新书签位置
    public Bookmark updateBookmark(Long userId, Long lectureId, Long slideId,Bookmark newMark){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要更新书签的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要更新书签的讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要更新书签的课件不存在"));
        Bookmark oldMark = bookmarkRepository.findById(newMark.getBookmarkId())
                .orElseThrow(() -> new InvalidRequestException("要更新的书签不存在"));
        oldMark.setBookmarkId(newMark.getBookmarkId());
        oldMark.setUser(user);
        oldMark.setLecture(lecture);
        oldMark.setSlide(slide);
        oldMark.setPageNum(newMark.getPageNum());
        oldMark.setData(newMark.getData());
        return bookmarkRepository.save(oldMark);
    }
*/

    //根据Id获取书签详情
    public Bookmark getBookmarkById(Long markId){
        return bookmarkRepository.findById(markId)
                .orElseThrow(() -> new InvalidRequestException("要查找的书签不存在"));
    }

    //获取某学生在某讲座的某课件的书签
    public List<Bookmark> getBookmarkByUser(Long userId, Long lectureId, Long slideId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查找书签的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查找书签的讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要查找书签的课件不存在"));
        List<Bookmark> marks = bookmarkRepository.findByUserAndLectureAndSlide(user,lecture,slide);
        if (marks == null || marks.isEmpty()){
            throw new ResourceNotFoundException("该学生在该课件没有书签标记");
        }
        return marks;
    }

    //删除书签
    public void deleteBookmark(Long markId){
        Bookmark bookmark = bookmarkRepository.findById(markId)
                .orElseThrow(() -> new InvalidRequestException("要删除的书签不存在"));
        bookmarkRepository.deleteById(markId);
    }


}
