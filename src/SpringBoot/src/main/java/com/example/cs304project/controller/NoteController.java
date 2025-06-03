package com.example.cs304project.controller;

import com.example.cs304project.dto.NoteDTO;
import com.example.cs304project.entity.Note;
import com.example.cs304project.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    @Autowired
    private NoteService noteService;

    //post /api/notes/create 创建笔记
    @PostMapping("/{userId}/{lectureId}/{lectureSlideId}/create")
    public ResponseEntity<NoteDTO> createNote(@PathVariable Long userId,
                                              @PathVariable Long lectureId,
                                              @PathVariable Long lectureSlideId,
                                              @RequestBody NoteDTO noteDTO){
        Note created = new Note();
        created.setContent(noteDTO.getContent());
        Note note = noteService.createNote(userId,lectureId, lectureSlideId,created);
        NoteDTO dto = new NoteDTO();
        dto.setNoteId(note.getNoteId());
        dto.setUserId(note.getUser().getUserId());
        dto.setLectureId(note.getLecture().getLectureId());
        dto.setSlideId(note.getSlide().getSlideId());
        dto.setContent(note.getContent());
        return ResponseEntity.ok(dto);
    }


    //put /api/notes/{noteId}/update 更新笔记信息
    @PutMapping("/{userId}/{noteId}/update")
    public ResponseEntity<NoteDTO> updateNote(@PathVariable Long userId,
                                              @PathVariable Long noteId,

                                              @RequestBody  NoteDTO noteDTO){
        Note updated = new Note();
        updated.setNoteId(noteId);
        updated.setContent(noteDTO.getContent());
        Note note = noteService.updateNote(userId,updated);
        NoteDTO dto = new NoteDTO();
        dto.setNoteId(note.getNoteId());
        dto.setUserId(note.getUser().getUserId());
        dto.setLectureId(note.getLecture().getLectureId());
        dto.setSlideId(note.getSlide().getSlideId());
        dto.setContent(note.getContent());
        return ResponseEntity.ok(dto);
    }

    //get /api/notes/{noteId}/getNoteById 获取笔记详情
    @GetMapping("/{noteId}/getNoteById")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable Long noteId){
        Note note = noteService.getNoteById(noteId);
        NoteDTO dto = new NoteDTO();
        dto.setNoteId(note.getNoteId());
        dto.setUserId(note.getUser().getUserId());
        dto.setLectureId(note.getLecture().getLectureId());
        dto.setSlideId(note.getSlide().getSlideId());
        dto.setContent(note.getContent());
        return ResponseEntity.ok(dto);
    }

    //get /api/notes/{userId}/getNotesByUser 获取学生所有笔记
    @GetMapping("/{userId}/getNotesByUser")
    public ResponseEntity<List<NoteDTO>> getNoteByUser(@PathVariable Long userId){
        List<Note> notes = noteService.getNotesByUser(userId);
        List<NoteDTO> dtos = notes.stream().map(note -> {
            NoteDTO dto = new NoteDTO();
            dto.setNoteId(note.getNoteId());
            dto.setUserId(note.getUser().getUserId());
            dto.setLectureId(note.getLecture().getLectureId());
            dto.setSlideId(note.getSlide().getSlideId());
            dto.setContent(note.getContent());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/notes/{userId}/{lectureId}/getNotesByUserLecture 获取某学生在某讲座所有笔记
    @GetMapping("/{userId}/{lectureId}/getNotesByUserLecture")
    public ResponseEntity<List<NoteDTO>> getNoteByUserLecture(@PathVariable Long userId,
                                                              @PathVariable Long lectureId){
        List<Note> notes = noteService.getNotesByUserLecture(userId,lectureId);
        List<NoteDTO> dtos = notes.stream().map(note -> {
            NoteDTO dto = new NoteDTO();
            dto.setNoteId(note.getNoteId());
            dto.setUserId(note.getUser().getUserId());
            dto.setLectureId(note.getLecture().getLectureId());
            dto.setSlideId(note.getSlide().getSlideId());
            dto.setContent(note.getContent());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //get /api/notes/{userId}/{lectureId}/{slideId}/getNotesByUserLectureSlide  获取某学生在某讲座的某课件的所有笔记
    @GetMapping("/{userId}/{lectureId}/{slideId}/getNotesByUserLectureSlide")
    public ResponseEntity<List<NoteDTO>> getNoteByUserLectureSlide(@PathVariable Long userId,
                                                                   @PathVariable Long lectureId,
                                                                   @PathVariable Long slideId){
        List<Note> notes = noteService.getNotesByUserLectureSlide(userId,lectureId,slideId);
        List<NoteDTO> dtos = notes.stream().map(note -> {
            NoteDTO dto = new NoteDTO();
            dto.setNoteId(note.getNoteId());
            dto.setUserId(note.getUser().getUserId());
            dto.setLectureId(note.getLecture().getLectureId());
            dto.setSlideId(note.getSlide().getSlideId());
            dto.setContent(note.getContent());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    //delete /api/notes/{noteId}/delete 删除笔记
    @DeleteMapping("/{noteId}/delete")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteId){
        noteService.deleteNote(noteId);
        return ResponseEntity.ok("已成功删除笔记");
    }
}