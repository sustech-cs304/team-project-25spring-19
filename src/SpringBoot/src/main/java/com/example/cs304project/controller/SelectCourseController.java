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
    //@GetMapping("/{courseId}/getByCourse")
    

    //根据学生查询课程
    //@GetMapping("/{user}/getByStudent")


    //删除选课
    //@DeleteMapping("/{selectId}/delete")

}
