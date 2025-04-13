package com.example.cs304project.controller;

import com.example.cs304project.dto.BookmarkDTO;
import com.example.cs304project.dto.NoteDTO;
import com.example.cs304project.entity.Bookmark;
import com.example.cs304project.entity.Note;
import com.example.cs304project.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/api/bookmarks")
public class BookmarkController {
    @Autowired
    private BookmarkService bookmarkService;

    //post /api/bookmarks/create 创建书签标记
    @PostMapping("/{userId}/{lectureId}/{slideId}/create")
    public ResponseEntity<BookmarkDTO> createNote(@PathVariable Long userId,
                                                  @PathVariable Long lectureId,
                                                  @PathVariable Long slideId,
                                                  @RequestBody BookmarkDTO markDTO){
        Bookmark created = new Bookmark();
        created.setData(markDTO.getData());
        created.setPageNum(markDTO.getPageNum());
        Bookmark bookmark = bookmarkService.createBookmark(userId,lectureId, slideId,created);
        BookmarkDTO dto = new BookmarkDTO();
        dto.setBookmarkId(bookmark.getBookmarkId());
        dto.setUserId(bookmark.getUser().getUserId());
        dto.setLectureId(bookmark.getLecture().getLectureId());
        dto.setSlideId(bookmark.getSlide().getSlideId());
        dto.setData(bookmark.getData());
        dto.setPageNum(bookmark.getPageNum());
        return ResponseEntity.ok(dto);
    }
/*    //put /api/bookmarks/{bookmarkId}/update 更新标记位置
    @PutMapping("/{bookmarkId}/update")
    public ResponseEntity<BookmarkDTO> updateNote(@PathVariable Long bookmarkId,BookmarkDTO markDTO){
        Bookmark updated = new Bookmark();
        updated.setBookmarkId(bookmarkId);
        updated.setData(markDTO.getData());
        updated.setPageNum(markDTO.getPageNum());
        Bookmark bookmark = bookmarkService.updateBookmark(markDTO.getUserId(),markDTO.getLectureId(),
                markDTO.getSlideId(),updated);
        BookmarkDTO dto = new BookmarkDTO();
        dto.setBookmarkId(bookmark.getBookmarkId());
        dto.setUserId(bookmark.getUser().getUserId());
        dto.setLectureId(bookmark.getLecture().getLectureId());
        dto.setSlideId(bookmark.getSlide().getSlideId());
        dto.setData(bookmark.getData());
        dto.setPageNum(bookmark.getPageNum());
        return ResponseEntity.ok(dto);
    }*/
    //get /api/bookmarks/{bookmarkId}/getMarkById 根据id获取书签信息
    @GetMapping("/{bookmarkId}/getMarkById")
    public ResponseEntity<BookmarkDTO> getNoteById(@PathVariable Long bookmarkId){
        Bookmark bookmark = bookmarkService.getBookmarkById(bookmarkId);
        BookmarkDTO dto = new BookmarkDTO();
        dto.setBookmarkId(bookmark.getBookmarkId());
        dto.setUserId(bookmark.getUser().getUserId());
        dto.setLectureId(bookmark.getLecture().getLectureId());
        dto.setSlideId(bookmark.getSlide().getSlideId());
        dto.setData(bookmark.getData());
        dto.setPageNum(bookmark.getPageNum());
        return ResponseEntity.ok(dto);
    }
    //get /api/bookmarks/{userId}/{lectureId}/{slideId}/getMarksByUserLectureSlide 获取某学生在某讲座某课件的书签
    @GetMapping("/{userId}/{lectureId}/{slideId}/getMarksByUserLectureSlide")
    public ResponseEntity<List<BookmarkDTO>> getNoteByUserLectureSlide(@PathVariable Long userId,
                                                                   @PathVariable Long lectureId,
                                                                   @PathVariable Long slideId){
        List<Bookmark> bookmarks = bookmarkService.getBookmarkByUser(userId,lectureId,slideId);
        List<BookmarkDTO> dtos = bookmarks.stream().map(bookmark -> {
            BookmarkDTO dto = new BookmarkDTO();
            dto.setBookmarkId(bookmark.getBookmarkId());
            dto.setUserId(bookmark.getUser().getUserId());
            dto.setLectureId(bookmark.getLecture().getLectureId());
            dto.setSlideId(bookmark.getSlide().getSlideId());
            dto.setData(bookmark.getData());
            dto.setPageNum(bookmark.getPageNum());
            return dto;

        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    //delete /api/bookmarks/{bookmarkId}/delete 删除标记
    @DeleteMapping("/{bookmarkId}/delete")
    public ResponseEntity<String> deleteMark(@PathVariable Long bookmarkId){
        bookmarkService.deleteBookmark(bookmarkId);
        return ResponseEntity.ok("指定书签已删除");
    }
}
