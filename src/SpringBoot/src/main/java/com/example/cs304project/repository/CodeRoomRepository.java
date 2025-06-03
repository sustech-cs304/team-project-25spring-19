package com.example.cs304project.repository;

import com.example.cs304project.entity.CodeRoom;
import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CodeRoomRepository extends JpaRepository<CodeRoom, Long> {
    List<CodeRoom> findByOwner(User owner);
    
    @Query(value = "SELECT cr.* FROM code_rooms cr JOIN room_members rm ON cr.id = rm.room_id WHERE rm.user_id = :userId", 
           nativeQuery = true)
    List<CodeRoom> findByMemberId(@Param("userId") Long userId);
    
    @Query(value = "SELECT DISTINCT cr.* FROM code_rooms cr " +
           "LEFT JOIN room_members rm ON cr.id = rm.room_id " +
           "WHERE cr.owner_id = :userId OR rm.user_id = :userId", 
           nativeQuery = true)
    List<CodeRoom> findAllByUserId(@Param("userId") Long userId);
    
    Optional<CodeRoom> findByNameAndOwner(String name, User owner);
} 