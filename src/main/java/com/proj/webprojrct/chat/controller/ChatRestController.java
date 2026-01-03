package com.proj.webprojrct.chat.controller;

import com.proj.webprojrct.chat.dto.ChatMessageDTO;
import com.proj.webprojrct.chat.dto.ChatRoomDTO;
import com.proj.webprojrct.chat.service.ChatService;
import com.proj.webprojrct.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    /**
     * Get chat history for current user
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<ChatMessageDTO> history = chatService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get unread message count for current user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        long count = chatService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark message as read
     */
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long messageId) {
        chatService.markAsRead(messageId);
        return ResponseEntity.ok(Map.of("message", "Message marked as read"));
    }

    /**
     * Get all active chat rooms (Admin only)
     */
    @GetMapping("/admin/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<List<ChatRoomDTO>> getActiveChatRooms() {
        List<ChatRoomDTO> rooms = chatService.getActiveChatRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get chat with specific user (Admin only)
     */
    @GetMapping("/admin/chat/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<List<ChatMessageDTO>> getChatWithUser(@PathVariable Long userId) {
        List<ChatMessageDTO> messages = chatService.getChatHistory(userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages from user as read (Admin only)
     */
    @PostMapping("/admin/mark-read/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<Map<String, String>> markUserMessagesAsRead(@PathVariable Long userId) {
        chatService.markUserMessagesAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    /**
     * Send message (REST alternative to WebSocket)
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody Map<String, String> request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        String content = request.get("content");
        ChatMessageDTO message = chatService.sendMessageToAdmin(userId, content);
        return ResponseEntity.ok(message);
    }
}
