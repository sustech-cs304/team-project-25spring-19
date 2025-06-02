package com.example.cs304project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "Resource_Sharing")
public class ResourceSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @ManyToOne
    @JoinColumn(name = "course_id")

    private Course course;

    @ManyToOne
    @JoinColumn(name = "lecture_id")

    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "user_id")

    private User user;

    @Column(name = "resource_type")
    private String type;

    @Column(name = "resource_link")
    private String link;

    @Column(name = "description")
    private String description;

    public ResourceSharing(Long resourceId, Course course, Lecture lecture, User user, String type, String link, String description) {
        this.resourceId = resourceId;
        this.course = course;
        this.lecture = lecture;
        this.user = user;
        this.type = type;
        this.link = link;
        this.description = description;
    }

    public ResourceSharing() {

    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
