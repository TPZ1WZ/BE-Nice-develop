package com.proj.webprojrct.chat.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatRoom extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column
    private Long adminId;

    @Column(name = "admin_name")
    private String adminName;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "unread_count")
    private int unreadCount = 0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (lastMessageTime == null) {
            lastMessageTime = LocalDateTime.now();
        }
    }
}
