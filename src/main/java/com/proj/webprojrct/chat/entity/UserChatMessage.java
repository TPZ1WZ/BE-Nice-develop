package com.proj.webprojrct.chat.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatMessage extends BaseEntity {

    @Column(nullable = false)
    private Long senderId;

    @Column(name = "sender_name")
    private String senderName;

    @Column(nullable = false)
    private Long receiverId;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        SYSTEM,
        NOTIFICATION
    }
}
