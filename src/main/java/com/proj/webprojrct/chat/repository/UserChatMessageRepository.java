package com.proj.webprojrct.chat.repository;

import com.proj.webprojrct.chat.entity.UserChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatMessageRepository extends JpaRepository<UserChatMessage, Long> {

    // Get all messages between user and admin
    @Query("SELECT m FROM UserChatMessage m WHERE " +
           "(m.senderId = :userId OR m.receiverId = :userId) " +
           "ORDER BY m.sentAt ASC")
    List<UserChatMessage> findChatHistory(@Param("userId") Long userId);

    // Get unread messages for user
    @Query("SELECT m FROM UserChatMessage m WHERE " +
           "m.receiverId = :userId AND m.isRead = false " +
           "ORDER BY m.sentAt DESC")
    List<UserChatMessage> findUnreadMessages(@Param("userId") Long userId);

    // Count unread messages for user
    @Query("SELECT COUNT(m) FROM UserChatMessage m WHERE " +
           "m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);

    // Get messages between specific users
    @Query("SELECT m FROM UserChatMessage m WHERE " +
           "(m.senderId = :user1Id AND m.receiverId = :user2Id) OR " +
           "(m.senderId = :user2Id AND m.receiverId = :user1Id) " +
           "ORDER BY m.sentAt ASC")
    List<UserChatMessage> findMessagesBetween(
        @Param("user1Id") Long user1Id, 
        @Param("user2Id") Long user2Id
    );
}
