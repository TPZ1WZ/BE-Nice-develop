package com.proj.webprojrct.chat.repository;

import com.proj.webprojrct.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    Optional<UserChatRoom> findByUserId(Long userId);

    @Query("SELECT cr FROM UserChatRoom cr WHERE cr.isActive = true ORDER BY cr.lastMessageTime DESC")
    List<UserChatRoom> findAllActiveRooms();

    @Query("SELECT cr FROM UserChatRoom cr WHERE cr.unreadCount > 0 ORDER BY cr.lastMessageTime DESC")
    List<UserChatRoom> findRoomsWithUnreadMessages();
}
