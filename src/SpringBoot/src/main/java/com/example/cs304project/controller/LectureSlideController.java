package com.example.cs304project.controller;

import com.example.cs304project.dto.LectureSlideDTO;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.service.LectureSideService;
import com.example.cs304project.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slides")
public class LectureSlideController {

    @Autowired
    private MinioService minioService;
    @Autowired
    private LectureSideService lectureSideService;
    //post /api/slides/{lectureId}/create 创建课件
    @PostMapping( "/{userId}/{lectureId}/create")
    public ResponseEntity<LectureSlideDTO> createSlide(@PathVariable Long userId,
                                                       @PathVariable Long lectureId,
                                                       @RequestBody LectureSlideDTO slideDTO){
        LectureSlide slide = new LectureSlide();

        slide.setContent(slideDTO.getContent());

        LectureSlide lectureSlide = lectureSideService.createSlide( userId,lectureId, slide);

        LectureSlideDTO dto = new LectureSlideDTO();
        dto.setSlideId(lectureSlide.getSlideId());
        dto.setLectureId(lectureSlide.getLecture().getLectureId());
        dto.setUrl(lectureSlide.getUrl());
        dto.setContent(lectureSlide.getContent());
        return ResponseEntity.ok(dto);

    }
    //post /api/slides/{lectureId}/create 上传文件
    @PostMapping( "/{userId}/{slideId}/updateFile")
    public ResponseEntity<LectureSlideDTO> uploadFile(@RequestParam(value = "file",required = false)MultipartFile file,
                                                       @PathVariable Long userId,
                                                       @PathVariable Long slideId) throws Exception {

        // 1. 拼一个 objectName，例如以 “pdfs/” 目录开头，并拼上原始文件名
        String originalFilename = file.getOriginalFilename(); // e.g., "example.pdf"
        String objectName = "pdfs/" + System.currentTimeMillis() + "_" + originalFilename;

        // 2. 调用 Service 上传
        minioService.uploadFile(file, objectName);

        // 3. 生成预签名 URL 并返回
        String url = minioService.generatePresignedUrl(objectName);


        LectureSlide lectureSlide = lectureSideService.UserUploadFile(userId,url,slideId);

        LectureSlideDTO dto = new LectureSlideDTO();
        dto.setSlideId(lectureSlide.getSlideId());
        dto.setLectureId(lectureSlide.getLecture().getLectureId());
        dto.setUrl(lectureSlide.getUrl());
        dto.setContent(lectureSlide.getContent());


        return ResponseEntity.ok(dto);

    }


    //get /api/slides/{slideId}/download 下载课件中的文件


    //put /api/slides/{slideId}/update 更新课件
    @PutMapping("/{userId}/{slideId}/update")
    public ResponseEntity<LectureSlideDTO> updateSlide(@PathVariable Long userId,
                                                       @PathVariable Long slideId,
                                                       @RequestBody LectureSlideDTO lectureSlideDTO){
        LectureSlide lectureSlide = new LectureSlide();
        lectureSlide.setSlideId(slideId);
        lectureSlide.setUrl(lectureSlideDTO.getUrl());
        lectureSlide.setContent(lectureSlideDTO.getContent());

        LectureSlide updated = lectureSideService.updateSlide(userId,slideId,lectureSlide);

        LectureSlideDTO dto = new LectureSlideDTO();
        dto.setSlideId(updated.getSlideId());
        dto.setLectureId(updated.getLecture().getLectureId());
        dto.setUrl(updated.getUrl());
        dto.setContent(updated.getContent());

        return ResponseEntity.ok(dto);

    }
    //get /api/slides/{slideId}/getById 根据id获取课件链接或课件内容
    @GetMapping("/{slideId}/getById")
    public ResponseEntity<LectureSlideDTO> getSlideById(@PathVariable Long slideId){
        LectureSlide slide = lectureSideService.getSlideById(slideId);
        LectureSlideDTO dto = new LectureSlideDTO();
        dto.setSlideId(slide.getSlideId());
        dto.setLectureId(slide.getLecture().getLectureId());
        dto.setUrl(slide.getUrl());
        dto.setContent(slide.getContent());

        return ResponseEntity.ok(dto);
    }
    //get /api/slides/{lectureId}/getAllSlides 获取讲座所有课件
    @GetMapping("/{lectureId}/getByLecture")
    public ResponseEntity<List<LectureSlideDTO>> getSlidesByLecture(@PathVariable Long lectureId){
        List<LectureSlide> slides = lectureSideService.getAllSlides(lectureId);
        List<LectureSlideDTO> dtos = slides.stream().map(slide -> {
            LectureSlideDTO dto = new LectureSlideDTO();
            dto.setSlideId(slide.getSlideId());
            dto.setLectureId(slide.getLecture().getLectureId());
            dto.setUrl(slide.getUrl());
            dto.setContent(slide.getContent());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //delete /api/slides/{slideId}/delete 删除课件
    @DeleteMapping("/{userId}/{slideId}/delete")
    public ResponseEntity<String> deleteLectureSlide(@PathVariable Long userId,
                                                     @PathVariable Long slideId){
        lectureSideService.deleteSlide(userId,slideId);
        return ResponseEntity.ok("课件成功删除");
    }
}
