package com.example.cs304project.service;


import com.example.cs304project.entity.Lecture;
import com.example.cs304project.entity.LectureSlide;
import com.example.cs304project.entity.Note;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.LectureRepository;
import com.example.cs304project.repository.LectureSlideRepository;
import com.example.cs304project.repository.NoteRepository;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private LectureSlideRepository lectureSlideRepository;
    //创建笔记
    public Note createNote(Long userId, Long lectureId, Long slideId, Note note){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要创建笔记的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要创建笔记的讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要创建笔记的课件不存在"));
        note.setUser(user);
        note.setLecture(lecture);
        note.setSlide(slide);
        return noteRepository.save(note);
    }

    //更新笔记内容
    public Note updateNote(Long userId, Note newNote){

        Note oldNote = noteRepository.findById(newNote.getNoteId())
                .orElseThrow(() -> new InvalidRequestException("要更新的笔记不存在"));
        if (!oldNote.getUser().getUserId().equals(userId)){
            throw new InvalidRequestException("只能更新自己的笔记");
        }
        oldNote.setContent(newNote.getContent());

        return noteRepository.save(oldNote);
    }

    //根据id获取笔记详情
    public Note getNoteById(Long noteId){
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("查找的笔记不存在"));
    }
    //获取某学生的所有笔记
    public List<Note> getNotesByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的用户不存在"));
        List<Note> notes = noteRepository.findByUser(user);
        if (notes == null || notes.isEmpty()){
            throw new ResourceNotFoundException("指定的学生没有笔记");
        }
        return notes;
    }

    //获取某学生在某讲座的所有笔记
    public List<Note> getNotesByUserLecture(Long userId,Long lectureId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的讲座不存在"));
        List<Note> notes = noteRepository.findByUserAndLecture(user,lecture);
        if (notes == null || notes.isEmpty()){
            throw new ResourceNotFoundException("指定的学生没有笔记");
        }
        return notes;
    }

    //获取某学生在某讲座的某课件的所有笔记
    public List<Note> getNotesByUserLectureSlide(Long userId, Long lectureId, Long slideId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的用户不存在"));
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的讲座不存在"));
        LectureSlide slide = lectureSlideRepository.findById(slideId)
                .orElseThrow(() -> new InvalidRequestException("要查询笔记的课件不存在"));
        List<Note> notes = noteRepository.findByUserAndLectureAndSlide(user,lecture,slide);
        if (notes == null || notes.isEmpty()){
            throw new ResourceNotFoundException("指定的学生讲座课件没有笔记");
        }
        return notes;
    }

    //删除笔记
    public void deleteNote(Long noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new InvalidRequestException("要删除的笔记不存在"));
        noteRepository.deleteById(noteId);
    }

    //
}