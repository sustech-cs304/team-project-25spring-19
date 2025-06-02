package com.example.cs304project.service;

import com.example.cs304project.entity.*;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.DiscussionRepository;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussionService {
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LectureRepository lectureRepository;

    //发布讨论
    public Discussion createDiscussion(Long userId, Long courseId, Long lectureId, Discussion discussion){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("要创建留言的用户不存在"));
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要创建留言的课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要创建留言的讲座不存在"));

        discussion.setUser(user);
        discussion.setCourse(course);
        discussion.setLecture(lecture);
;
        return discussionRepository.save(discussion);

    }
    //获取帖子的详情信息
    public Discussion getDiscussionById(Long discussionId){
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new InvalidRequestException("要查找的留言不存在"));
        return discussion;
    }

    //获取某课程的全部讨论内容
    public List<Discussion> getDiscussionByCourse(Long courseId){
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要查找分享的留言不存在"));
        List<Discussion> discussions = discussionRepository.findByCourse(course);
        if (discussions == null || discussions.isEmpty()){
            throw new ResourceNotFoundException("查找的课程没有留言");
        }
        return discussions;
    }

    //获取某节讲座的全部讨论内容
    public List<Discussion> getDiscussionByCourseLecture(Long courseId,Long lectureId){
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查找分享的留言不存在"));
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要查找分享的留言不存在"));
        List<Discussion> discussions = discussionRepository.findByCourseAndLecture(course,lecture);
        if (discussions == null || discussions.isEmpty()){
            throw new ResourceNotFoundException("查找的讲座没有留言");
        }
        return discussions;
    }

    //获取某用户的全部发言
    public List<Discussion> getDiscussionByUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("要查找留言的用户不存在"));
        List<Discussion> discussions = discussionRepository.findByUser(user);
        if (discussions == null || discussions.isEmpty()){
            throw new ResourceNotFoundException("查找的用户没有留言");
        }
        return discussions;
    }

    //删除发言或回复
    public void deleteDiscussion(Long discussionId){
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new InvalidRequestException("要删除的留言不存在"));
        discussionRepository.deleteById(discussionId);
    }

}
