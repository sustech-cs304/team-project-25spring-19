package com.example.cs304project.controller;


import com.example.cs304project.dto.LectureDTO;
import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {
    @Autowired
    private LectureService lectureService;

    //post /api/lectures/{courseId}/create 创建新讲座
    @PostMapping("/{userId}/{courseId}/create")
    public ResponseEntity<LectureDTO> createLecture(@PathVariable Long userId,
                                                    @PathVariable Long courseId,
                                                    @RequestBody LectureDTO lectureDTO){

        Lecture lecture = new Lecture();
        lecture.setTittle(lectureDTO.getTittle());
        lecture.setDescription(lectureDTO.getDescription());
        lecture.setLectureOrder(lectureDTO.getLectureOrder());

        Lecture created = lectureService.createLecture(userId,courseId, lecture);

        LectureDTO dto = new LectureDTO();
        dto.setLectureId(created.getLectureId());
        dto.setCourseId(created.getCourse().getCourseId());
        dto.setTittle(created.getTittle());
        dto.setLectureOrder(created.getLectureOrder());
        dto.setDescription(created.getDescription());
        return ResponseEntity.ok(dto);
    }

    //put /api/lectures/{lectureId}/update 更新讲座信息
    @PutMapping("/{userId}/{courseId}/{lectureId}/update")
    public ResponseEntity<LectureDTO> updateLecture(@PathVariable Long userId,
                                                    @PathVariable Long lectureId,
                                                    @PathVariable Long courseId,
                                                     @RequestBody LectureDTO lectureDTO){

        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setLectureOrder(lectureDTO.getLectureOrder());
        lecture.setDescription(lectureDTO.getDescription());
        lecture.setTittle(lectureDTO.getTittle());

        Lecture updated = lectureService.updateLecture(userId,courseId,lecture);

        LectureDTO dto = new LectureDTO();
        dto.setLectureId(updated.getLectureId());
        dto.setCourseId(updated.getCourse().getCourseId());
        dto.setTittle(updated.getTittle());
        dto.setLectureOrder(updated.getLectureOrder());
        dto.setDescription(updated.getDescription());
        return ResponseEntity.ok(dto);
    }

    //get /api/lectures/{lectureId}/getById 根据id获取讲座信息
    @GetMapping("/{lectureId}/getById")
    public ResponseEntity<LectureDTO> getLectureById(@PathVariable Long lectureId){

        Lecture lecture = lectureService.getLectureById(lectureId);
        LectureDTO dto = new LectureDTO();
        dto.setLectureId(lecture.getLectureId());
        dto.setCourseId(lecture.getCourse().getCourseId());
        dto.setTittle(lecture.getTittle());
        dto.setLectureOrder(lecture.getLectureOrder());
        dto.setDescription(lecture.getDescription());
        return ResponseEntity.ok(dto);
    }

    //get /api/lectures/getByOrder 根据讲座顺序获取讲座信息
    @GetMapping("/{courseId}/{order}/getByOrder")
    public ResponseEntity<List<LectureDTO>> getLecturesByOrder(@PathVariable Long courseId,
                                                               @PathVariable Integer order){
        List<Lecture> lectures = lectureService.getLectureByOrder(courseId,order);
        List<LectureDTO> dtos = lectures.stream().map(lecture -> {
            LectureDTO dto = new LectureDTO();
            dto.setLectureId(lecture.getLectureId());
            dto.setCourseId(lecture.getCourse().getCourseId());
            dto.setTittle(lecture.getTittle());
            dto.setLectureOrder(lecture.getLectureOrder());
            dto.setDescription(lecture.getDescription());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    //get /api/lectures/{courseId}/getByCourse 获取某课程所有讲座
    @GetMapping("/{courseId}/getByCourse")
    public ResponseEntity<List<LectureDTO>> getLecturesByCourse(@PathVariable Long courseId){
        List<Lecture> lectures = lectureService.getAllLecture(courseId);
        List<LectureDTO> dtos = lectures.stream().map(lecture -> {
            LectureDTO dto = new LectureDTO();
            dto.setLectureId(lecture.getLectureId());
            dto.setCourseId(lecture.getCourse().getCourseId());
            dto.setTittle(lecture.getTittle());
            dto.setLectureOrder(lecture.getLectureOrder());
            dto.setDescription(lecture.getDescription());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    //delete /api/lectures/{lectureId}/delete 删除讲座
    @DeleteMapping("/{userId}/{lectureId}/delete")
    public ResponseEntity<String> deleteLecture(@PathVariable Long userId,
                                                @PathVariable Long lectureId){
        lectureService.deleteLecture(userId,lectureId);
        return ResponseEntity.ok("成功删除讲座");
    }
}
