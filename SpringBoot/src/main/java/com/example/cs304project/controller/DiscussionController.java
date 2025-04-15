package com.example.cs304project.controller;

import com.example.cs304project.dto.DiscussionDTO;
import com.example.cs304project.dto.ResourceDTO;
import com.example.cs304project.entity.Discussion;
import com.example.cs304project.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    //post /api/discussions/create 发布帖子或回复
    @PostMapping("/{userId}/{courseId}/{lectureId}/create")
    public ResponseEntity<DiscussionDTO> createDiscussion(@PathVariable Long userId,
                                 @PathVariable Long courseId,
                                 @PathVariable Long lectureId,
                                 @RequestBody DiscussionDTO discussionDTO){
        Discussion created = new Discussion();
        created.setContext(discussionDTO.getContext());
        Discussion discussion = discussionService.createDiscussion(userId,courseId,lectureId,created);

        DiscussionDTO dto = new DiscussionDTO();
        dto.setDiscussionId(discussion.getDiscussionId());
        dto.setCourseId(discussion.getCourse().getCourseId());
        dto.setUserId(discussion.getUser().getUserId());
        dto.setLectureId(discussion.getLecture().getLectureId());
        dto.setContext(discussion.getContext());

        return ResponseEntity.ok(dto);
    }

    //get /api/discussions/{discussionId}/getById 获取帖子详情信息
    @GetMapping("/{discussionId}/getById")
    public ResponseEntity<DiscussionDTO> getDiscussionById(@PathVariable Long discussionId){

        Discussion discussion = discussionService.getDiscussionById(discussionId);
        DiscussionDTO dto = new DiscussionDTO();
        dto.setDiscussionId(discussion.getDiscussionId());
        dto.setCourseId(discussion.getCourse().getCourseId());
        dto.setUserId(discussion.getUser().getUserId());
        dto.setLectureId(discussion.getLecture().getLectureId());
        dto.setContext(discussion.getContext());

        return ResponseEntity.ok(dto);
    }

    //get /api/discussions/{discussionId}/getDiscussionByCourse 获取某课程的全部讨论内容
    @GetMapping("/{courseId}/getDiscussionByCourse")
    public ResponseEntity<List<DiscussionDTO>> getDiscussionByCourse(@PathVariable Long courseId){

        List<Discussion> discussions = discussionService.getDiscussionByCourse(courseId);
        List<DiscussionDTO> dtos = discussions.stream().map(discussion -> {
            DiscussionDTO dto = new DiscussionDTO();
            dto.setDiscussionId(discussion.getDiscussionId());
            dto.setCourseId(discussion.getCourse().getCourseId());
            dto.setUserId(discussion.getUser().getUserId());
            dto.setLectureId(discussion.getLecture().getLectureId());
            dto.setContext(discussion.getContext());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/discussions/{lectureId}/getDiscussionByLecture 获取某讲座全部回复
    @GetMapping("/{courseId}/{lectureId}/getDiscussionByLecture")
    public ResponseEntity<List<DiscussionDTO>> getDiscussionByLecture(@PathVariable Long courseId,
                                                                      @PathVariable Long lectureId){

        List<Discussion> discussions = discussionService.getDiscussionByCourseLecture(courseId,lectureId);
        List<DiscussionDTO> dtos = discussions.stream().map(discussion -> {
            DiscussionDTO dto = new DiscussionDTO();
            dto.setDiscussionId(discussion.getDiscussionId());
            dto.setCourseId(discussion.getCourse().getCourseId());
            dto.setUserId(discussion.getUser().getUserId());
            dto.setLectureId(discussion.getLecture().getLectureId());
            dto.setContext(discussion.getContext());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/discussions/{userId}/getDiscussionByUser 获取某用户全部回复
    @GetMapping("/{userId}/getDiscussionByUser")
    public ResponseEntity<List<DiscussionDTO>> getDiscussionByUser(@PathVariable Long userId){
        List<Discussion> discussions = discussionService.getDiscussionByUser(userId);
        List<DiscussionDTO> dtos = discussions.stream().map(discussion -> {
            DiscussionDTO dto = new DiscussionDTO();
            dto.setDiscussionId(discussion.getDiscussionId());
            dto.setCourseId(discussion.getCourse().getCourseId());
            dto.setUserId(discussion.getUser().getUserId());
            dto.setLectureId(discussion.getLecture().getLectureId());
            dto.setContext(discussion.getContext());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //delete /api/discussions/{discussionId}/delete 删除回复
    @DeleteMapping("/{discussionId}/delete")
    public ResponseEntity<String> deleteDiscussion(@PathVariable Long discussionId){
        discussionService.deleteDiscussion(discussionId);
        return ResponseEntity.ok("成功删除留言");
    }


}
