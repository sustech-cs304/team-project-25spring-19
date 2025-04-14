package com.example.cs304project.service;

import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.LectureSlideRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class LectureSideService {

    @Autowired
    private LectureSlideRepository lectureSlideRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserRepository userRepository;
    //创建新课件
    /*
    * ChatGPT o3
    * 指令：如何实现lectureslidecervice中存储课件的功能
    * 方式：仿写
    * */
    public LectureSlide createSlide(Long userId,Long lectureId,LectureSlide slide)  {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定讲座"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以创建课件");
        }
        slide.setLecture(lecture);
        slide.setExtractedText(new byte[0]);
        return lectureSlideRepository.save(slide);

    }

    //上传文件
    public LectureSlide uploadFile(MultipartFile file,Long userId,Long slideId) throws IOException {

        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要上传文件的课件不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以上传课件");
        }
        if (file != null && !file.isEmpty()){
            slide.setExtractedText(file.getBytes());
        }

        return lectureSlideRepository.save(slide);

    }
    //更改课件内容或顺序
    public LectureSlide updateSlide(Long userId,Long slideId, LectureSlide newSlide){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以修改课件");
        }
        LectureSlide oldSlide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要更新的课件不存在"));
        oldSlide.setContent(newSlide.getContent());
        oldSlide.setUrl(newSlide.getUrl());

        return lectureSlideRepository.save(oldSlide);

    }

    //根据id获取课件内容
    public LectureSlide getSlideById(Long slideId){
        return lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定课件"));
    }


    //获取讲座的所有课件
    public List<LectureSlide> getAllSlides(Long lectureId){
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new ResourceNotFoundException("找不到指定讲座"));
        List<LectureSlide> slides= lectureSlideRepository.findByLecture(lecture);
        if (slides == null || slides.isEmpty()){
            throw new ResourceNotFoundException("指定讲座没有课件");
        }

        return slides;
    }

    //删除课件
    public void deleteSlide(Long userId,Long lectureSlideId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定的教师不存在"));
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以删除课件");
        }
        LectureSlide slide = lectureSlideRepository.findById(lectureSlideId)
                .orElseThrow(() -> new ResourceNotFoundException("要删除的课件不存在"));

        lectureSlideRepository.delete(slide);
    }
}
