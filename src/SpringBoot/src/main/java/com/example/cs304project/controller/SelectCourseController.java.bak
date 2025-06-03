package com.example.cs304project.controller;

import com.example.cs304project.dto.LectureDTO;
import com.example.cs304project.dto.SelectCourseDTO;
import com.example.cs304project.dto.SelectDTO;
import com.example.cs304project.entity.SelectCourse;
import com.example.cs304project.repository.SelectCourseRepository;
import com.example.cs304project.service.SelectCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/selectCourse")
public class SelectCourseController {


    @Autowired
    private SelectCourseService selectCourseService;


    //创建选课关系
    @PostMapping("/{userId}/{courseId}/create")
    public ResponseEntity<SelectCourseDTO> createSelectCourse(@PathVariable Long userId,
                                                              @PathVariable Long courseId){
        SelectCourse selectCourse = new SelectCourse();
        SelectCourse created = selectCourseService.createSelectCourse(userId,courseId,selectCourse);

        SelectCourseDTO dto = new SelectCourseDTO();
        dto.setUsername(created.getStudent().getUserName());
        dto.setProfile(created.getStudent().getProfile());
        dto.setTittle(created.getCourse().getTitle());
        dto.setInstructor(created.getCourse().getInstructor().getUserName());

        return ResponseEntity.ok(dto);
    }

    //根据课程查询学生
    @GetMapping("/{courseId}/getByCourse")
    public ResponseEntity<List<SelectCourseDTO>> getByCourse(@PathVariable Long courseId){
        List<SelectCourse> selectCourses = selectCourseService.getByCourse(courseId);
        List<SelectCourseDTO> dtos = selectCourses.stream().map(selectCourse -> {
            SelectCourseDTO dto = new SelectCourseDTO();
            dto.setUsername(selectCourse.getStudent().getUserName());
            dto.setProfile(selectCourse.getStudent().getProfile());
            dto.setTittle(selectCourse.getCourse().getTitle());
            dto.setInstructor(selectCourse.getCourse().getInstructor().getUserName());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //根据学生查询课程
    @GetMapping("/{userId}/getByStudent")
    public ResponseEntity<List<SelectCourseDTO>> getByStudent(@PathVariable Long userId){
        List<SelectCourse> selectCourses = selectCourseService.getByStudent(userId);
        List<SelectCourseDTO> dtos = selectCourses.stream().map(selectCourse -> {
            SelectCourseDTO dto = new SelectCourseDTO();
            dto.setUsername(selectCourse.getStudent().getUserName());
            dto.setProfile(selectCourse.getStudent().getProfile());
            dto.setTittle(selectCourse.getCourse().getTitle());
            dto.setInstructor(selectCourse.getCourse().getInstructor().getUserName());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //删除选课关系
    @DeleteMapping("/{selectId}/delete")
    public ResponseEntity<String> deleteSelect(@PathVariable Long selectId){
        selectCourseService.deleteSelect(selectId);
        return ResponseEntity.ok("成功删除课程");
    }

}
