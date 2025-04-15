package com.example.cs304project.service;

import com.example.cs304project.entity.*;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CourseProgressService {

    @Autowired
    private CourseProgressRepository courseProgressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private LectureSlideRepository lectureSlideRepository;
    //创建学习进度
    public CourseProgress createProcess(Long userId, Long courseId, Long lectureId, Long slideId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定用户不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("指定讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new ResourceNotFoundException("指定课件不存在"));

        // 新记录的初始状态设为 "未开始"
        CourseProgress progress = new CourseProgress();
        progress.setUser(user);
        progress.setCourse(course);
        progress.setLecture(lecture);
        progress.setSlide(slide);
        progress.setState("未开始");
        // 如果一门课程中只有这一条记录，比例可以初步定义为：未开始为1，其它为0。
        progress.setFinished(BigDecimal.ZERO);
        progress.setStarted(BigDecimal.ZERO);
        progress.setNotStart(BigDecimal.ONE);

        return courseProgressRepository.save(progress);
    }

    //更新学习进度
    public CourseProgress updateProcess(Long progressId, String newState){
        CourseProgress progress = courseProgressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("进度记录不存在"));
        progress.setState(newState);
        // 更新当前用户和课程下所有记录的比例
        ProgressRatio(progress.getUser(), progress.getCourse());
        return courseProgressRepository.save(progress);
    }

    private void ProgressRatio(User user, Course course) {
        List<CourseProgress> progressList = courseProgressRepository.findByUserAndCourse(user, course);
        int total = progressList.size();
        if (total == 0) {
            return;
        }
        long finishedCount = progressList.stream().filter(p -> "已完成".equals(p.getState())).count();
        long startedCount = progressList.stream().filter(p -> "进行中".equals(p.getState())).count();
        long notStartCount = progressList.stream().filter(p -> "未开始".equals(p.getState())).count();

        // 计算各个状态的比例，保留两位小数
        BigDecimal finishedRatio = BigDecimal.valueOf(finishedCount)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        BigDecimal startedRatio = BigDecimal.valueOf(startedCount)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        BigDecimal notStartRatio = BigDecimal.valueOf(notStartCount)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        // 更新该用户在该课程所有进度记录的比例字段
        for (CourseProgress cp : progressList) {
            cp.setFinished(finishedRatio);
            cp.setStarted(startedRatio);
            cp.setNotStart(notStartRatio);
        }
        // 事务结束时自动flush更新
    }

    //根据id获取学习进度
    public CourseProgress getProcessById(Long progressId){
        return courseProgressRepository.findById(progressId)
            .orElseThrow(() -> new ResourceNotFoundException("进度记录不存在"));

    }

    //获取某学生的学习各个课程进度
    public List<CourseProgress> getProcessByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定用户不存在"));
        List<CourseProgress> progresses = courseProgressRepository.findByUser(user);
        if (progresses == null || progresses.isEmpty()){
            throw new ResourceNotFoundException("要查找的进度记录不存在");
        }
        return progresses;
    }

    //获取某学生在某课程某讲座的某个课件学习状态
    public List<CourseProgress> getProcessByUserCourseLecture(Long userId, Long courseId, Long lectureId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("指定用户不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("指定课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("指定讲座不存在"));
        List<CourseProgress> progresses = courseProgressRepository.findByUserAndCourseAndLecture(user, course, lecture);
        if (progresses == null || progresses.isEmpty()){
            throw new ResourceNotFoundException("要查找的进度记录不存在");
        }
        return progresses;
    }

    //删除学习进度
    public void deleteProcess(Long progressId){
        CourseProgress progress = courseProgressRepository.findById(progressId)
                .orElseThrow(() -> new InvalidRequestException("要删除的进度不存在"));

        courseProgressRepository.deleteById(progressId);
    }
}
