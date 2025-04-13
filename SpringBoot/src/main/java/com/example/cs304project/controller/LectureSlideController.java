package com.example.cs304project.controller;

import com.example.cs304project.dto.LectureSlideDTO;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.service.LectureSideService;
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
    private LectureSideService lectureSideService;
    //post /api/slides/{lectureId}/create 创建课件
    @PostMapping( "/{userId}/{lectureId}/create")
    public ResponseEntity<LectureSlideDTO> createSlide(@PathVariable Long userId,
                                                       @PathVariable Long lectureId,
                                                       @RequestBody LectureSlideDTO slideDTO){
        LectureSlide slide = new LectureSlide();
        slide.setUrl(slideDTO.getUrl());
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
                                                       @PathVariable Long slideId) throws IOException {

        LectureSlide lectureSlide = lectureSideService.uploadFile(file, userId,slideId);

        LectureSlideDTO dto = new LectureSlideDTO();
        dto.setSlideId(lectureSlide.getSlideId());
        dto.setLectureId(lectureSlide.getLecture().getLectureId());
        dto.setUrl(lectureSlide.getUrl());
        dto.setContent(lectureSlide.getContent());
        if (lectureSlide.getExtractedText() != null){
            String base64Data = Base64.getEncoder().encodeToString(lectureSlide.getExtractedText());
            dto.setExtractedText(base64Data);
        }

        return ResponseEntity.ok(dto);

    }

/*
* chatGpt o3
* 指令：如何实现文件下载的请求
* 方式：复制
*         // 根据实际情况设定 Content-Type，此处简单以 application/octet-stream 表示二进制数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("slide_" + slideId).build());
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
* */
    //get /api/slides/{slideId}/download 下载课件中的文件
    @GetMapping("/{slideId}/download")
    public ResponseEntity<byte[]> downloadSlideFile(@PathVariable Long slideId) {
        LectureSlide slide = lectureSideService.getSlideById(slideId);
        byte[] fileData = slide.getExtractedText();

        String fileName = slide.getContent();
        // 根据实际情况设定 Content-Type，此处简单以 application/octet-stream 表示二进制数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8).build());
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

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
        if (updated.getExtractedText() != null){
            String base64Data = Base64.getEncoder().encodeToString(updated.getExtractedText());
            dto.setExtractedText(base64Data);
        }
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
        if (slide.getExtractedText() != null){
            String base64Data = Base64.getEncoder().encodeToString(slide.getExtractedText());
            dto.setExtractedText(base64Data);
        }
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
            if (slide.getExtractedText() != null){
                String base64Data = Base64.getEncoder().encodeToString(slide.getExtractedText());
                dto.setExtractedText(base64Data);
            }
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
