package com.example.cs304project.controller;

import com.example.cs304project.dto.ExerciseDTO;
import com.example.cs304project.entity.Exercise;
import com.example.cs304project.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;
    //post /api/exercises/create 创建作业练习
    @PostMapping("/{userId}/{courseId}/{lectureId}/create")
    public ResponseEntity<ExerciseDTO> createExercise(@PathVariable Long userId,
                                                      @PathVariable Long courseId,
                                                      @PathVariable Long lectureId,
                                                      @RequestBody ExerciseDTO exerciseDTO){
        Exercise created = new Exercise();
        created.setAnswer(exerciseDTO.getAnswer());
        created.setQuestion(exerciseDTO.getQuestion());
        Exercise exercise = exerciseService.createExercise(userId,courseId, lectureId,created);
        ExerciseDTO dto = new ExerciseDTO();
        dto.setExerciseId(exercise.getExerciseId());
        dto.setCourseId(exercise.getCourse().getCourseId());
        dto.setLectureId(exercise.getLecture().getLectureId());
        dto.setQuestion(exercise.getQuestion());
        dto.setAnswer(exercise.getAnswer());
        return ResponseEntity.ok(dto);

    }

    //put /api/exercises/{exerciseId}/update 修改练习内容
    @PutMapping("/{userId}/{exerciseId}/update")
    public ResponseEntity<ExerciseDTO> updateExercise(@PathVariable Long userId,
                                                      @PathVariable Long exerciseId,
                                                      @RequestBody ExerciseDTO exerciseDTO){
        Exercise updated = new Exercise();
        updated.setExerciseId(exerciseId);
        updated.setAnswer(exerciseDTO.getAnswer());
        updated.setQuestion(exerciseDTO.getQuestion());
        Exercise exercise = exerciseService.updateExercise(userId,updated);
        ExerciseDTO dto = new ExerciseDTO();
        dto.setExerciseId(exercise.getExerciseId());
        dto.setCourseId(exercise.getCourse().getCourseId());
        dto.setLectureId(exercise.getLecture().getLectureId());
        dto.setQuestion(exercise.getQuestion());
        dto.setAnswer(exercise.getAnswer());
        return ResponseEntity.ok(dto);
    }

    //get /api/exercises/{courseId}/{lectureId}/getByCourseLecture 获取某课程某讲座的所有练习
    @GetMapping("/{courseId}/{lectureId}/getByCourseLecture")
    public ResponseEntity<List<ExerciseDTO>> getByCourseLecture(@PathVariable Long courseId,
                                                                @PathVariable Long lectureId){
        List<Exercise> exercises = exerciseService.getExerciseByCourseLecture(courseId,lectureId);
        List<ExerciseDTO> dtos = exercises.stream().map(exercise -> {
            ExerciseDTO dto = new ExerciseDTO();
            dto.setExerciseId(exercise.getExerciseId());
            dto.setCourseId(exercise.getCourse().getCourseId());
            dto.setLectureId(exercise.getLecture().getLectureId());
            dto.setQuestion(exercise.getQuestion());
            dto.setAnswer(exercise.getAnswer());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/exercises/{exerciseId}/getById 根据id获取练习内容
    @GetMapping("/{exerciseId}/getById")
    public ResponseEntity<ExerciseDTO> getById(@PathVariable Long exerciseId){
        Exercise exercise = exerciseService.getExerciseById(exerciseId);
        ExerciseDTO dto = new ExerciseDTO();
        dto.setExerciseId(exercise.getExerciseId());
        dto.setCourseId(exercise.getCourse().getCourseId());
        dto.setLectureId(exercise.getLecture().getLectureId());
        dto.setQuestion(exercise.getQuestion());
        dto.setAnswer(exercise.getAnswer());
        return ResponseEntity.ok(dto);
    }

    //delete /api/exercises/{exerciseId}/delete 删除练习
    @DeleteMapping("/{userId}/{exerciseId}/delete")
    public ResponseEntity<String> deleteExercise(@PathVariable Long userId,
                                                 @PathVariable Long exerciseId){
        exerciseService.deleteExercise(userId,exerciseId);
        return ResponseEntity.ok("成功删除练习");
    }

}
