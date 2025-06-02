package com.example.cs304project.service;


import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    //创建课程，对于老师
    public Course createCourse(Long userId,Course course){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定教师不存在")
        );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以创建课程");
        }
        course.setInstructor(user);
        return courseRepository.save(course);
    }
/*    //添加课程，对于学生
    public Course addCourse( Long userId, Long courseId){
        User student = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定学生不存在")
        );
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("指定课程不存在")
        );
        if (!(courseRepository.findCourseByTitleAndInstructor(course.getTitle(),student) == null)){
            throw new InvalidRequestException("课程已添加");
        }
        Course added = new Course();
        added.setTitle(course.getTitle());
        added.setInstructor(student);
        added.setDescription(course.getDescription());
        added.setLectureNum(course.getLectureNum());
        return courseRepository.save(added);
    }*/
    //更新课程信息
    public Course updateCourse(Long userId, Course newCourse){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定用户不存在")
        );
        Course oldCourse = courseRepository.findById(newCourse.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("要更新的课程不存在"));
        if(!oldCourse.getInstructor().getUserId().equals(user.getUserId())){
            throw new InvalidRequestException("只能更新自己的课程");
        }
        oldCourse.setTitle(newCourse.getTitle());
        oldCourse.setDescription(newCourse.getDescription());
        oldCourse.setLectureNum(newCourse.getLectureNum());
        return courseRepository.save(oldCourse);

    }

    //查询课程,根据Id
    public Course getCourseById(Long courseId){
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未发现课程"));
    }

    //查询课程，根据tittle
    public List<Course> getCourseByTittle(String tittle){
        List<Course> courses = courseRepository.findCourseByTittle(tittle);

        if (courses == null || courses.isEmpty()){
            throw new ResourceNotFoundException("课程不存在");
        }
        return courses;
    }

    //查询某学生或教师所有课程
    public List<Course> listAllCourse(Long id){
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("指定的用户不存在"));

        List<Course> courses = courseRepository.findCourseByInstructor(user);
        if (courses == null || courses.isEmpty()){
            throw new ResourceNotFoundException("指定的用户没有课程");
        }

        return courses;
    }

    //删除课程
    public void deleteCourse(Long userId,Long courseId){

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("要删除的课程不存在"));

        if (!course.getInstructor().getUserId().equals(userId)){
            throw new InvalidRequestException("只能删除自己的课程");
        }
        courseRepository.deleteById(courseId);
    }




}
