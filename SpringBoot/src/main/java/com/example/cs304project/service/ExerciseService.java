package com.example.cs304project.service;

import com.example.cs304project.entity.*;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.ExerciseRepository;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private UserRepository userRepository;
    //创建练习题或作业
    public Exercise createExercise(Long userId,Long courseId, Long lectureId, Exercise exercise){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定教师不存在")
        );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以创建练习");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new InvalidRequestException("要创建练习的课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要创建练习的讲座不存在"));
        exercise.setCourse(course);
        exercise.setLecture(lecture);
        return exerciseRepository.save(exercise);
    }

    //更新练习题或作业内容
    public Exercise updateExercise(Long userId, Exercise newExercise){

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定教师不存在")
        );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以修改练习");
        }
        Exercise oldExercise = exerciseRepository.findById(newExercise.getExerciseId())
                .orElseThrow(() -> new InvalidRequestException("要更新的练习不存在"));
        oldExercise.setQuestion(newExercise.getQuestion());
        oldExercise.setAnswer(newExercise.getAnswer());
        return exerciseRepository.save(oldExercise);
    }

    //获取某课程某讲座全部练习题
    public List<Exercise> getExerciseByCourseLecture(Long courseId, Long lectureId){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new InvalidRequestException("要查找练习的课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查找练习的讲座不存在"));
        List<Exercise> exercises = exerciseRepository.findByCourseAndLecture(course,lecture);
        if (exercises == null || exercises.isEmpty()){
            throw new ResourceNotFoundException("要查找的练习不存在");
        }
        return exercises;
    }

    //根据id获取练习的内容
    public Exercise getExerciseById(long exerciseId){
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("要查找的练习不存在"));
        return exercise;
    }

    //删除练习题
    public void deleteExercise(Long userId,Long exerciseId){
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("指定教师不存在")
    );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以删除课程");
        }
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("要删除的练习不存在"));
        exerciseRepository.deleteById(exerciseId);
    }
}
