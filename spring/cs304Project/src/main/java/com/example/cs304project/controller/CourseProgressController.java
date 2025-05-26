package com.example.cs304project.controller;

import com.example.cs304project.dto.CourseProgressDTO;
import com.example.cs304project.entity.CourseProgress;
import com.example.cs304project.service.CourseProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/process")
public class CourseProgressController {
    @Autowired
    private CourseProgressService progressService;

    //post    /api/process/{userId}/{courseId}/{lectureId}/create     创建学习进度
    @PostMapping("/{userId}/{courseId}/{lectureId}/{slideId}/create")
    public ResponseEntity<CourseProgressDTO> createProcess(@PathVariable Long userId,
                                                           @PathVariable Long courseId,
                                                           @PathVariable Long lectureId,
                                                           @PathVariable Long slideId){
        CourseProgress progress = progressService.createProcess(userId,courseId,lectureId,slideId);
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setUserId(progress.getUser().getUserId());
        dto.setCourseId(progress.getCourse().getCourseId());
        dto.setLectureId(progress.getLecture().getLectureId());
        dto.setSlideId(progress.getSlide().getSlideId());
        dto.setState(progress.getState());
        dto.setFinished(progress.getFinished());
        dto.setStarted(progress.getStarted());
        dto.setNotStart(progress.getNotStart());
        return ResponseEntity.ok(dto);

    }

    //put       /api/process/{processId}/update       更新学习进度
    @PutMapping("/{processId}/update")
    public ResponseEntity<CourseProgressDTO> updateProcess(@PathVariable Long processId,
                                                           @RequestBody CourseProgressDTO courseProgressDTO){
        CourseProgress progress = progressService.updateProcess(processId,courseProgressDTO.getState());
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setUserId(progress.getUser().getUserId());
        dto.setCourseId(progress.getCourse().getCourseId());
        dto.setLectureId(progress.getLecture().getLectureId());
        dto.setSlideId(progress.getSlide().getSlideId());
        dto.setState(progress.getState());
        dto.setFinished(progress.getFinished());
        dto.setStarted(progress.getStarted());
        dto.setNotStart(progress.getNotStart());
        return ResponseEntity.ok(dto);
    }

    //get       /api/process/{processId}/getById       根据id获取学习进度
    @GetMapping("/{processId}/getById")
    public ResponseEntity<CourseProgressDTO> getById(@PathVariable Long processId){
        CourseProgress progress = progressService.getProcessById(processId);
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setUserId(progress.getUser().getUserId());
        dto.setCourseId(progress.getCourse().getCourseId());
        dto.setLectureId(progress.getLecture().getLectureId());
        dto.setSlideId(progress.getSlide().getSlideId());
        dto.setState(progress.getState());
        dto.setFinished(progress.getFinished());
        dto.setStarted(progress.getStarted());
        dto.setNotStart(progress.getNotStart());
        return ResponseEntity.ok(dto);
    }

    //get       /api/process/{userId}/getByUser      获取某学生的学习进度
    @GetMapping("/{userId}/getByUser")
    public ResponseEntity<List<CourseProgressDTO>> getByUser(@PathVariable Long userId){
        List<CourseProgress> progresses = progressService.getProcessByUser(userId);
        List<CourseProgressDTO> dtos = progresses.stream().map(progress -> {
            CourseProgressDTO dto = new CourseProgressDTO();
            dto.setUserId(progress.getUser().getUserId());
            dto.setCourseId(progress.getCourse().getCourseId());
            dto.setLectureId(progress.getLecture().getLectureId());
            dto.setSlideId(progress.getSlide().getSlideId());
            dto.setState(progress.getState());
            dto.setFinished(progress.getFinished());
            dto.setStarted(progress.getStarted());
            dto.setNotStart(progress.getNotStart());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get       /api/process/{userId}/{courseId}/{lectureId}/{slideId}/getByUserCourseLecture       获取某学生在某课程某讲座的某个课件学习进度
    @GetMapping("/{userId}/{courseId}/{lectureId}/{slideId}/getByUserCourseLecture")
    public ResponseEntity<List<CourseProgressDTO>> getByUserCourseLecture(@PathVariable Long userId,
                                                                          @PathVariable Long courseId,
                                                                          @PathVariable Long lectureId){
        List<CourseProgress> progresses = progressService.getProcessByUserCourseLecture(userId,courseId,lectureId);
        List<CourseProgressDTO> dtos = progresses.stream().map(progress -> {
            CourseProgressDTO dto = new CourseProgressDTO();
            dto.setUserId(progress.getUser().getUserId());
            dto.setCourseId(progress.getCourse().getCourseId());
            dto.setLectureId(progress.getLecture().getLectureId());
            dto.setSlideId(progress.getSlide().getSlideId());
            dto.setState(progress.getState());
            dto.setFinished(progress.getFinished());
            dto.setStarted(progress.getStarted());
            dto.setNotStart(progress.getNotStart());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //delete        /api/process/{processId}/delete       删除学习进度
    @DeleteMapping("/{processId}/delete")
    public ResponseEntity<String> delete(@PathVariable Long processId){
        progressService.deleteProcess(processId);
        return ResponseEntity.ok("删除指定进度记录");
    }


}
