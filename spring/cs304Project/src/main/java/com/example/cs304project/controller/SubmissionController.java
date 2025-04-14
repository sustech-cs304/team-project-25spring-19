package com.example.cs304project.controller;

import com.example.cs304project.dto.CodeResultDTO;
import com.example.cs304project.dto.SubmissionDTO;
import com.example.cs304project.entity.Submission;
import com.example.cs304project.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    //post /api/submissions/create  创建提交
    @PostMapping("/{studentId}/{exerciseId}/create")
    public ResponseEntity<SubmissionDTO> createSubmission(@PathVariable Long studentId,
                                                          @PathVariable Long exerciseId,
                                                          @RequestBody SubmissionDTO submissionDTO){
        Submission created = new Submission();
        String language = submissionDTO.getLanguage();
        created.setContent(submissionDTO.getContent());
        if (language != null && !language.isEmpty()){
            created.setLanguage(language);
            created.setResult(submissionService.runSubmission(submissionDTO.getContent(),submissionDTO.getLanguage()));
        }
        Submission submission = submissionService.createSubmission(studentId, exerciseId,created);
        SubmissionDTO dto = new SubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setStudentId(submission.getStudent().getUserId());
        dto.setExerciseId(submission.getExercise().getExerciseId());
        dto.setContent(submission.getContent());
        dto.setResult(submission.getResult());
        dto.setLanguage(submission.getLanguage());
        dto.setScore(submission.getScore());

        return ResponseEntity.ok(dto);
    }

    //put /api/submissions/{submissionId}/update 修改提交
    @PutMapping("/{userId}/{submissionId}/update")
    public ResponseEntity<SubmissionDTO> updateSubmission(@PathVariable Long userId,
                                                          @PathVariable Long submissionId,
                                                          @RequestBody SubmissionDTO submissionDTO){
        Submission updated = new Submission();
        updated.setScore(submissionDTO.getScore());
        Submission submission = submissionService.updateSubmission(userId,submissionId,updated);
        SubmissionDTO dto = new SubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setStudentId(submission.getStudent().getUserId());
        dto.setExerciseId(submission.getExercise().getExerciseId());
        dto.setContent(submission.getContent());
        dto.setLanguage(submission.getLanguage());
        dto.setResult(submission.getResult());
        dto.setScore(submission.getScore());

        return ResponseEntity.ok(dto);


    }

    //post /api/submissions/runSubmission 实时返回运行的提交结果
    @PostMapping("/runSubmission")
    public ResponseEntity<CodeResultDTO> runSubmission(@RequestBody CodeResultDTO resultDTO){
        CodeResultDTO dto = new CodeResultDTO();
        dto.setCode(resultDTO.getCode());
        dto.setLanguage(resultDTO.getLanguage());
        dto.setResult(submissionService.runSubmission(dto.getCode(),dto.getLanguage()));
        return ResponseEntity.ok(dto);
    }

    //get /api/submissions/{submissionId}/getById 根据id获取提交
    @GetMapping("/{submissionId}/getById")
    public ResponseEntity<SubmissionDTO> getById(@PathVariable Long submissionId){
        Submission submission = submissionService.getSubmissionById(submissionId);
        SubmissionDTO dto = new SubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setStudentId(submission.getStudent().getUserId());
        dto.setExerciseId(submission.getExercise().getExerciseId());
        dto.setContent(submission.getContent());
        dto.setLanguage(submission.getLanguage());
        dto.setResult(submission.getResult());
        dto.setScore(submission.getScore());
        return ResponseEntity.ok(dto);
    }

    //get /api/submissions/{exerciseId}/getByExercise 获取某练习的所有提交
    @GetMapping("/{userId}/{exerciseId}/getByExercise")
    public ResponseEntity<List<SubmissionDTO>> getByExercise(@PathVariable Long userId,
                                                             @PathVariable Long exerciseId){
        List<Submission> submissions = submissionService.getSubmissionByExercise(userId,exerciseId);
        List<SubmissionDTO> dtos = submissions.stream().map(submission -> {
            SubmissionDTO dto = new SubmissionDTO();
            dto.setSubmissionId(submission.getSubmissionId());
            dto.setStudentId(submission.getStudent().getUserId());
            dto.setExerciseId(submission.getExercise().getExerciseId());
            dto.setContent(submission.getContent());
            dto.setLanguage(submission.getLanguage());
            dto.setResult(submission.getResult());
            dto.setScore(submission.getScore());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/submissions/{userId}/getByUser 获取某学生的所有提交
    @GetMapping("/{userId}/getByUser")
    public ResponseEntity<List<SubmissionDTO>> getByUser(@PathVariable Long userId){
        List<Submission> submissions = submissionService.getSubmissionByUser(userId);
        List<SubmissionDTO> dtos = submissions.stream().map(submission -> {
            SubmissionDTO dto = new SubmissionDTO();
            dto.setSubmissionId(submission.getSubmissionId());
            dto.setStudentId(submission.getStudent().getUserId());
            dto.setExerciseId(submission.getExercise().getExerciseId());
            dto.setContent(submission.getContent());
            dto.setResult(submission.getResult());
            dto.setLanguage(submission.getLanguage());
            dto.setScore(submission.getScore());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/submissions/{userId}/{exerciseId}/getByUserExercise 获取某学生某个题的提交
    @GetMapping("/{userId}/{exerciseId}/getByUserExercise")
    public ResponseEntity<List<SubmissionDTO>> getByUserExercise(@PathVariable Long userId,
                                                                 @PathVariable Long exerciseId ){
        List<Submission> submissions = submissionService.getSubmissionByUserExercise(userId,exerciseId);
        List<SubmissionDTO> dtos = submissions.stream().map(submission -> {
            SubmissionDTO dto = new SubmissionDTO();
            dto.setSubmissionId(submission.getSubmissionId());
            dto.setStudentId(submission.getStudent().getUserId());
            dto.setExerciseId(submission.getExercise().getExerciseId());
            dto.setContent(submission.getContent());
            dto.setResult(submission.getResult());
            dto.setLanguage(submission.getLanguage());
            dto.setScore(submission.getScore());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //delete /api/submissions/{submissionId}/delete 删除提交
    @DeleteMapping("/{submissionId}/delete")
    public ResponseEntity<String> delete(@PathVariable Long submissionId){
        submissionService.deleteSubmission(submissionId);
        return ResponseEntity.ok("成功删除提交");
    }


}
