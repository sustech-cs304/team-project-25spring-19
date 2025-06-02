package com.example.cs304project.controller;

import com.example.cs304project.dto.ResourceDTO;
import com.example.cs304project.entity.ResourceSharing;
import com.example.cs304project.service.ResourceSharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    @Autowired
    private ResourceSharingService sharingService;

    //post /api/resources/{resourceId}/create  创建资源共享
    @PostMapping("/{userId}/{courseId}/{lectureId}/create")
    public ResponseEntity<ResourceDTO> createSharing(@PathVariable Long userId,
                                                     @PathVariable Long courseId,
                                                     @PathVariable Long lectureId,
                                                     @RequestBody ResourceDTO resourceDTO){
        ResourceSharing created = new ResourceSharing();
        created.setDescription(resourceDTO.getDescription());
        created.setType(resourceDTO.getType());
        created.setLink(resourceDTO.getLink());
        ResourceSharing sharing = sharingService.createSharing(userId, courseId, lectureId, created);
        ResourceDTO dto = new ResourceDTO();
        dto.setResourceId(sharing.getResourceId());
        dto.setUserId(sharing.getUser().getUserId());
        dto.setCourseId(sharing.getCourse().getCourseId());
        dto.setLectureId(sharing.getLecture().getLectureId());
        dto.setDescription(sharing.getDescription());
        dto.setType(sharing.getType());
        dto.setLink(sharing.getLink());
        return ResponseEntity.ok(dto);
    }
    //put   /api/resources/{resourceId}/update     更新共享内容
    @PutMapping("/{userId}/{resourceId}/update")
    public ResponseEntity<ResourceDTO> updateSharing(@PathVariable Long userId,
                                                     @PathVariable Long resourceId,
                                                     @RequestBody ResourceDTO resourceDTO){
        ResourceSharing update = new ResourceSharing();
        update.setResourceId(resourceId);
        update.setDescription(resourceDTO.getDescription());
        update.setType(resourceDTO.getType());
        update.setLink(resourceDTO.getLink());
        ResourceSharing sharing = sharingService.updateSharing(userId,update);
        ResourceDTO dto = new ResourceDTO();
        dto.setResourceId(sharing.getResourceId());
        dto.setUserId(sharing.getUser().getUserId());
        dto.setCourseId(sharing.getCourse().getCourseId());
        dto.setLectureId(sharing.getLecture().getLectureId());
        dto.setDescription(sharing.getDescription());
        dto.setType(sharing.getType());
        dto.setLink(sharing.getLink());
        return ResponseEntity.ok(dto);
    }
    //get   /api/resources/{resourceId}/getById     根据id获取共享内容
    @GetMapping("/{resourceId}/getById")
    public ResponseEntity<ResourceDTO> getSharingById(@PathVariable Long resourceId){
        ResourceSharing sharing = sharingService.getSharingById(resourceId);
        ResourceDTO dto = new ResourceDTO();
        dto.setResourceId(sharing.getResourceId());
        dto.setUserId(sharing.getUser().getUserId());
        dto.setCourseId(sharing.getCourse().getCourseId());
        dto.setLectureId(sharing.getLecture().getLectureId());
        dto.setDescription(sharing.getDescription());
        dto.setType(sharing.getType());
        dto.setLink(sharing.getLink());
        return ResponseEntity.ok(dto);
    }
    //get   /api/resources/{courseId}/getSharingByCourse     获取某课程的全部共享
    @GetMapping("/{courseId}/getSharingByCourse")
    public ResponseEntity<List<ResourceDTO>> getSharingByCourse(@PathVariable Long courseId){
        List<ResourceSharing> sharings = sharingService.getSharingByCourse(courseId);
        List<ResourceDTO> dtos = sharings.stream().map(sharing -> {
            ResourceDTO dto = new ResourceDTO();
            dto.setResourceId(sharing.getResourceId());
            dto.setUserId(sharing.getUser().getUserId());
            dto.setCourseId(sharing.getCourse().getCourseId());
            dto.setLectureId(sharing.getLecture().getLectureId());
            dto.setDescription(sharing.getDescription());
            dto.setType(sharing.getType());
            dto.setLink(sharing.getLink());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    //get   /api/resources/{lectureId}/getSharingByLecture     获取某讲座的全部共享
    @GetMapping("/{courseId}/{lectureId}/getSharingByLecture")
    public ResponseEntity<List<ResourceDTO>> getSharingByLecture(@PathVariable Long courseId,
                                                                 @PathVariable Long lectureId){
        List<ResourceSharing> sharings = sharingService.getSharingByCourseLecture(courseId,lectureId);
        List<ResourceDTO> dtos = sharings.stream().map(sharing -> {
            ResourceDTO dto = new ResourceDTO();
            dto.setResourceId(sharing.getResourceId());
            dto.setUserId(sharing.getUser().getUserId());
            dto.setCourseId(sharing.getCourse().getCourseId());
            dto.setLectureId(sharing.getLecture().getLectureId());
            dto.setDescription(sharing.getDescription());
            dto.setType(sharing.getType());
            dto.setLink(sharing.getLink());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    //get   /api/resources/{userId}/getSharingByUser    获取某用户的全部共享
    @GetMapping("/{userId}/getSharingByUser")
    public ResponseEntity<List<ResourceDTO>> getSharingByUser(@PathVariable Long userId){
        List<ResourceSharing> sharings = sharingService.getSharingByUser(userId);
        List<ResourceDTO> dtos = sharings.stream().map(sharing -> {
            ResourceDTO dto = new ResourceDTO();
            dto.setResourceId(sharing.getResourceId());
            dto.setUserId(sharing.getUser().getUserId());
            dto.setCourseId(sharing.getCourse().getCourseId());
            dto.setLectureId(sharing.getLecture().getLectureId());
            dto.setDescription(sharing.getDescription());
            dto.setType(sharing.getType());
            dto.setLink(sharing.getLink());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    //delete    /api/resources/{resourceId}/delete     删除资源共享
    @DeleteMapping("/{resourceId}/delete")
    public ResponseEntity<String> deleteSharing(@PathVariable Long resourceId){
        sharingService.deleteSharing(resourceId);
        return ResponseEntity.ok("指定的资源已删除");

    }
}
