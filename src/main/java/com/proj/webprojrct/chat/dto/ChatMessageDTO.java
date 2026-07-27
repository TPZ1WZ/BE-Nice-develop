package com.proj.webprojrct.chat.dto;

import com.proj.webprojrct.chat.entity.UserChatMessage;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private boolean isRead;
    private UserChatMessage.MessageType type;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
