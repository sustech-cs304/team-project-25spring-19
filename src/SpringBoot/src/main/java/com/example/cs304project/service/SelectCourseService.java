package com.example.cs304project.service;

import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.SelectCourse;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.SelectCourseRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectCourseService {

    @Autowired
    private SelectCourseRepository selectCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    //创建选课关系
    public SelectCourse createSelectCourse(Long userId, Long courseId, SelectCourse selectCourse){
        Course course = courseRepository.findById(courseId).
                orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        selectCourse.setStudent(user);
        selectCourse.setCourse(course);
        return selectCourseRepository.save(selectCourse);
    }
    //根据课程查询学生

    //根据学生查询课程

    //删除选课

}
