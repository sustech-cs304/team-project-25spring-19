package com.example.cs304project.service;

import com.example.cs304project.dto.SubmissionDTO;
import com.example.cs304project.entity.Exercise;
import com.example.cs304project.entity.Submission;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.ExerciseRepository;
import com.example.cs304project.repository.SubmissionRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private CodeRun codeRun;
    //创建答案或代码
    public Submission createSubmission(Long userId, Long exerciseId, Submission submission){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要创建提交的用户不存在"));
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new InvalidRequestException("要创建提交的练习不存在"));
        submission.setStudent(user);
        submission.setExercise(exercise);
        submission.setScore(BigDecimal.valueOf(-1));
        return submissionRepository.save(submission);
    }

    //更新答案或代码
    public Submission updateSubmission(Long userId,Long submissionId,Submission newSubmission){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定教师不存在")
        );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师评分");
        }
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new InvalidRequestException("要更新的提交不存"));
        submission.setScore(newSubmission.getScore());
        return submissionRepository.save(submission);
    }

    //实时返回提交的代码运行结果
    public String runSubmission(String submission, String language){
        return codeRun.runCode(submission,language);
    }

    //获取某个题的所有提交
    public List<Submission> getSubmissionByExercise(Long userId, Long exerciseId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("指定教师不存在")
        );
        if (!user.getRole().equalsIgnoreCase("teacher")){
            throw new InvalidRequestException("只有教师可以查看所有提交");
        }
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new InvalidRequestException("要查找提交的练习不存在"));
        List<Submission> submissions = submissionRepository.findByExercise(exercise);
        if (submissions == null || submissions.isEmpty()){
            throw new ResourceNotFoundException("要查找的练习没有提交");
        }
        return submissions;
    }

    //获取某个学生的所有提交
    public List<Submission> getSubmissionByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查找提交的用户不存在"));
        List<Submission> submissions = submissionRepository.findByStudent(user);
        if (submissions == null || submissions.isEmpty()){
            throw new ResourceNotFoundException("要查找的学生没有提交");
        }
        return submissions;
    }

    //获取某个学生某个题的提交
    public List<Submission> getSubmissionByUserExercise(Long userId,Long exerciseId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查找提交的用户不存在"));
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new InvalidRequestException("要查找提交的练习不存在"));
        List<Submission> submissions = submissionRepository.findByStudentAndExercise(user,exercise);
        if (submissions == null || submissions.isEmpty()){
            throw new ResourceNotFoundException("要查找的学生在该练习没有提交");
        }
        return submissions;
    }

    //
    public Submission getSubmissionById(Long submissionId){
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("要查找的提交不存在"));
        return submission;
    }

    //删除答案
    public void deleteSubmission(Long submissionId){
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new InvalidRequestException("要删除的提交不存在"));
        submissionRepository.deleteById(submissionId);
    }

    //
}
