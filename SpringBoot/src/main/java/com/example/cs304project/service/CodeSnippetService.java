package com.example.cs304project.service;


import com.example.cs304project.entity.CodeSnippet;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CodeSnippetRepository;
import com.example.cs304project.repository.LectureSlideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CodeSnippetService {


    @Autowired
    private CodeSnippetRepository codeSnippetRepository;

    @Autowired
    private LectureSlideRepository lectureSlideRepository;

    @Autowired
    private CodeRun codeRun;
    //创建，运行代码并存储返回结果
    public CodeSnippet createCode(Long slideId, CodeSnippet code){
        LectureSlide lectureSlide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("课件不存在"));
        code.setSlide(lectureSlide);
        return codeSnippetRepository.save(code);
    }

    //直接运行并获得代码结果，不存储
    public String runCode(String code, String language) {
       return codeRun.runCode(code,language);
    }

    //根据id获取代码内容
    public CodeSnippet getCodeById(Long codeId){
        return codeSnippetRepository.findById(codeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定代码"));
    }

    //根据课件获取代码内容
    public List<CodeSnippet> getCodesBySlide(Long slideId){
        LectureSlide lectureSlide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("课件不存在"));
        List<CodeSnippet> codes = codeSnippetRepository.findBySlide(lectureSlide);

        if (codes == null || codes.isEmpty()){
            throw  new ResourceNotFoundException("指定课件没有存储代码");
        }
        return codes;
    }

    //删除代码存储
    public void deleteCode(Long codeId){
        CodeSnippet codeSnippet = codeSnippetRepository.findById(codeId)
                .orElseThrow(() -> new ResourceNotFoundException("要删除的代码不存在"));
        codeSnippetRepository.deleteById(codeId);
    }
}
