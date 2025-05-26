package com.example.cs304project.controller;


import com.example.cs304project.dto.CourseDTO;
import com.example.cs304project.entity.Course;
import com.example.cs304project.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    //post /api/courses/create 创建课程
    @PostMapping("/{userId}/create")
    public ResponseEntity<CourseDTO> createCourse(@PathVariable Long userId,
                                                  @RequestBody CourseDTO courseDTO){
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setLectureNum(courseDTO.getLectureNum());

        Course created = courseService.createCourse(userId,course);

        CourseDTO resultDTO = new CourseDTO();
        resultDTO.setCourseId(created.getCourseId());
        resultDTO.setDescription(created.getDescription());
        resultDTO.setTitle(created.getTitle());
        resultDTO.setLectureNum(created.getLectureNum());
        resultDTO.setInstructorId(created.getInstructor().getUserId());

        return ResponseEntity.ok(resultDTO);

    }

/*    //post /api/courses/add
    @PostMapping("/{userId}/{courseId}/add")
    public ResponseEntity<CourseDTO> addCourse(@PathVariable Long userId,
                                               @PathVariable Long courseId){


        Course created = courseService.addCourse(userId,courseId);

        CourseDTO resultDTO = new CourseDTO();
        resultDTO.setCourseId(created.getCourseId());
        resultDTO.setDescription(created.getDescription());
        resultDTO.setTitle(created.getTitle());
        resultDTO.setLectureNum(created.getLectureNum());
        resultDTO.setInstructorId(created.getInstructor().getUserId());

        return ResponseEntity.ok(resultDTO);

    }*/
    //put /api/courses/{userId}/{courseId}/update 更新课程信息
    @PutMapping("/{userId}/{courseId}/update")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long userId,
                                                  @PathVariable Long courseId,
                                                  @RequestBody CourseDTO courseDTO){

        Course course = new Course();
        course.setCourseId(courseId);
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setLectureNum(courseDTO.getLectureNum());

        Course updated = courseService.updateCourse(userId,course);

        CourseDTO resultDTO = new CourseDTO();
        resultDTO.setCourseId(updated.getCourseId());
        resultDTO.setDescription(updated.getDescription());
        resultDTO.setTitle(updated.getTitle());
        resultDTO.setLectureNum(updated.getLectureNum());
        resultDTO.setInstructorId(updated.getInstructor().getUserId());

        return ResponseEntity.ok(resultDTO);
    }

    //get /api/courses/{courseId}/getById 根据id获取课程
    @GetMapping("/{courseId}/getById")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable("courseId") Long courseId){

        Course course = courseService.getCourseById(courseId);
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(course.getCourseId());
        courseDTO.setTitle(course.getTitle());
        courseDTO.setInstructorId(course.getInstructor().getUserId());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setLectureNum(course.getLectureNum());
        return ResponseEntity.ok(courseDTO);
    }

    //get /api/courses/getByTittle 根据课程名获取课程
    @GetMapping("/getByTittle")
    public ResponseEntity<List<CourseDTO>> getCourseByTittle(@RequestParam String tittle){

        List<Course> courses = courseService.getCourseByTittle(tittle);
        List<CourseDTO> courseDTOS = courses.stream().map(course -> {
            CourseDTO dto = new CourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setInstructorId(course.getInstructor().getUserId());
            dto.setDescription(course.getDescription());
            dto.setLectureNum(course.getLectureNum());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOS);
    }
    //get /api/courses/{userId}/getByUser 获取某个教师的所有课程
    @GetMapping("/{userId}/getByUser")
    public ResponseEntity<List<CourseDTO>> getCourseByUser(@PathVariable Long userId){


        List<Course> courses = courseService.listAllCourse(userId);
        List<CourseDTO> courseDTOS = courses.stream().map(course -> {
            CourseDTO dto = new CourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setInstructorId(course.getInstructor().getUserId());
            dto.setDescription(course.getDescription());
            dto.setLectureNum(course.getLectureNum());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOS);
    }

    //delete /api/courses/{courseId}/delete 删除课程
    @DeleteMapping("/{userId}/{courseId}/delete")
    public ResponseEntity<String> deleteCourse(@PathVariable Long userId,
                                               @PathVariable Long courseId){
        courseService.deleteCourse(userId,courseId);
        return ResponseEntity.ok("课程成功删除");
    }

}
