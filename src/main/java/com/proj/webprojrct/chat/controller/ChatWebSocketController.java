package com.proj.webprojrct.chat.controller;

import com.proj.webprojrct.chat.dto.ChatMessageDTO;
import com.proj.webprojrct.chat.service.ChatService;
import com.proj.webprojrct.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * User sends message to admin
     * Endpoint: /app/chat.sendToAdmin
     */
    @MessageMapping("/chat.sendToAdmin")
    public void sendToAdmin(
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            Long userId = Long.parseLong(message.get("senderId").toString());
            String content = message.get("content").toString();

            log.info("Received message from user {}: {}", userId, content);

            chatService.sendMessageToAdmin(userId, content);
        } catch (Exception e) {
            log.error("Error sending message to admin: {}", e.getMessage());
            throw new RuntimeException("Failed to send message");
        }
    }

    /**
     * Admin sends message to user
     * Endpoint: /app/chat.sendToUser
     */
    @MessageMapping("/chat.sendToUser")
    public void sendToUser(@Payload Map<String, Object> message) {
        try {
            Long adminId = Long.parseLong(message.get("senderId").toString());
            Long userId = Long.parseLong(message.get("receiverId").toString());
            String content = message.get("content").toString();

            log.info("Admin {} sending message to user {}: {}", adminId, userId, content);

            chatService.sendMessageToUser(adminId, userId, content);
        } catch (Exception e) {
            log.error("Error sending message to user: {}", e.getMessage());
            throw new RuntimeException("Failed to send message");
        }
    }

    /**
     * User connects to chat
     * Endpoint: /app/chat.connect
     */
    @MessageMapping("/chat.connect")
    @SendTo("/topic/admin/userConnected")
    public Map<String, Object> userConnected(@Payload Map<String, Object> payload) {
        log.info("User connected: {}", payload.get("userId"));
        return payload;
    }

    /**
     * User disconnects from chat
     * Endpoint: /app/chat.disconnect
     */
    @MessageMapping("/chat.disconnect")
    @SendTo("/topic/admin/userDisconnected")
    public Map<String, Object> userDisconnected(@Payload Map<String, Object> payload) {
        log.info("User disconnected: {}", payload.get("userId"));
        return payload;
    }
}
