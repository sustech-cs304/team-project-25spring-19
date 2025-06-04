package com.example.cs304project.service;

import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.SpringVersion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LectureService {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;


    //创建讲座,仅限教师
    public Lecture createLecture(Long userId,Long courseId, Lecture lecture){
        Course course = courseRepository.findById(courseId).
                orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以创建讲座");
        }
        lecture.setCourse(course);
        return lectureRepository.save(lecture);
    }

    //更新讲座信息
    public Lecture updateLecture(Long userId,Long courseId, Lecture newlecture){
        Course course = courseRepository.findById(courseId).
                orElseThrow(() -> new ResourceNotFoundException("要更新的课程不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以更新讲座");
        }
        Lecture oldLecture = lectureRepository.findById(newlecture.getLectureId()).
                orElseThrow(() -> new ResourceNotFoundException("要更新的讲座不存在"));
        oldLecture.setTitle(newlecture.getTitle());
        oldLecture.setDescription(newlecture.getDescription());
        oldLecture.setLectureOrder(newlecture.getLectureOrder());

        return lectureRepository.save(oldLecture);
    }

    //根据id获取讲座信息
    public Lecture getLectureById(Long lectureId){
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("未发现讲座"));

    }

    //根据order获取讲座信息
    public List<Lecture> getLectureByOrder(Long courseId,Integer order){
        Course course = courseRepository.findById(courseId).
                orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        List<Lecture> lectures = lectureRepository.findByCourseAndLectureOrder(course,order);
        if (lectures == null || lectures.isEmpty()){
            throw  new ResourceNotFoundException("讲座不存在");
        }
        return lectures;
    }

    //获取某课程中全部讲座信息
    public List<Lecture> getAllLecture(Long courseId){
        Course course = courseRepository.findById(courseId).
                orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        List<Lecture> lectures = lectureRepository.findByCourse(course);
        if (lectures == null || lectures.isEmpty()){
            throw new ResourceNotFoundException("指定课程没有讲座");
        }
        return lectures;
    }

    //删除指定讲座
    public void deleteLecture(Long userId,Long lectureId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以删除讲座");
        }
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("要删除的讲座不存在"));
        lectureRepository.deleteById(lectureId);
    }

}
