package com.example.cs304project.service;

import com.example.cs304project.entity.Course;
import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.ResourceSharing;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.CourseRepository;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.ResourceSharingRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceSharingService {

    @Autowired
    private ResourceSharingRepository sharingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LectureRepository lectureRepository;
    //创建资源共享
    public ResourceSharing createSharing(Long userId,Long courseId,Long lectureId,ResourceSharing sharing){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("要创建分享的用户不存在"));
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要创建分享的课程不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要创建分享的讲座不存在"));
        sharing.setUser(user);
        sharing.setCourse(course);
        sharing.setLecture(lecture);
        return sharingRepository.save(sharing);

    }

    //更新共享内容
    public ResourceSharing updateSharing(Long userId,ResourceSharing newSharing){


        ResourceSharing oldSharing = sharingRepository.findById(newSharing.getResourceId())
                .orElseThrow(() -> new InvalidRequestException("要更新的分享不存在"));
        if (!oldSharing.getUser().getUserId().equals(userId)){
            throw new InvalidRequestException("只能修改自己的分享");
        }
        if (newSharing.getLink() != null && !newSharing.getLink().isEmpty())
            oldSharing.setLink(newSharing.getLink());
        if (newSharing.getType() != null && !newSharing.getType().isEmpty())
            oldSharing.setType(newSharing.getType());
        if (newSharing.getDescription() != null && !newSharing.getDescription().isEmpty())
            oldSharing.setDescription(newSharing.getDescription());
        return sharingRepository.save(oldSharing);
    }

    //根据id获取共享内容
    public ResourceSharing getSharingById(Long sharingId){
        ResourceSharing sharing = sharingRepository.findById(sharingId)
                .orElseThrow(() -> new InvalidRequestException("要查找的分享不存在"));
        return sharing;
    }

    //获取某课程的全部共享
    public List<ResourceSharing> getSharingByCourse(Long courseId){
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要查找分享的课程不存在"));
        List<ResourceSharing> sharings = sharingRepository.findByCourse(course);
        if (sharings == null || sharings.isEmpty()){
            throw new ResourceNotFoundException("查找的课程没有分享");
        }
        return sharings;
    }

    //获取某讲座的全部共享
    public List<ResourceSharing> getSharingByCourseLecture(Long courseId,Long lectureId){
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查找分享的讲座不存在"));
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("要查找分享的课程不存在"));

        List<ResourceSharing> sharings = sharingRepository.findByCourseAndLecture(course,lecture);
        if (sharings == null || sharings.isEmpty()){
            throw new ResourceNotFoundException("查找的讲座没有分享");
        }
        return sharings;
    }

    //获取某用户的全部共享
    public List<ResourceSharing> getSharingByUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("要查找分享的用户不存在"));
        List<ResourceSharing> sharings = sharingRepository.findByUser(user);
        if (sharings == null || sharings.isEmpty()){
            throw new ResourceNotFoundException("查找的用户没有分享");
        }
        return sharings;
    }

    //删除资源共享
    public void deleteSharing(Long sharingId){
        ResourceSharing sharing = sharingRepository.findById(sharingId)
                .orElseThrow(() -> new InvalidRequestException("要删除的分享不存在"));
        sharingRepository.deleteById(sharingId);
    }
}
