package com.proj.webprojrct.chat.service;

import com.proj.webprojrct.chat.dto.ChatMessageDTO;
import com.proj.webprojrct.chat.dto.ChatRoomDTO;
import com.proj.webprojrct.chat.entity.UserChatMessage;
import com.proj.webprojrct.chat.entity.UserChatRoom;
import com.proj.webprojrct.chat.repository.UserChatMessageRepository;
import com.proj.webprojrct.chat.repository.UserChatRoomRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final UserChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * User sends message to Admin
     */
    @Transactional
    public ChatMessageDTO sendMessageToAdmin(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create or update chat room
        UserChatRoom chatRoom = chatRoomRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserChatRoom newRoom = UserChatRoom.builder()
                            .userId(userId)
                            .userName(user.getFullName())
                            .userEmail(user.getEmail())
                            .lastMessage(content)
                            .lastMessageTime(LocalDateTime.now())
                            .unreadCount(1)
                            .isActive(true)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        // Update chat room
        chatRoom.setLastMessage(content);
        chatRoom.setLastMessageTime(LocalDateTime.now());
        chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
        chatRoomRepository.save(chatRoom);

        // Save message
        UserChatMessage message = UserChatMessage.builder()
                .senderId(userId)
                .senderName(user.getFullName())
                .receiverId(0L) // 0 means admin
                .receiverName("Admin")
                .content(content)
                .isRead(false)
                .type(UserChatMessage.MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .build();

        message = chatMessageRepository.save(message);

        // Send to all admins via WebSocket
        ChatMessageDTO dto = convertToDTO(message);
        messagingTemplate.convertAndSend("/topic/admin/messages", dto);
        log.info("📤 Sent to admin topic: /topic/admin/messages");

        log.info("Message sent from user {} to admin: {}", userId, content);
        return dto;
    }

    /**
     * Admin sends message to User
     */
    @Transactional
    public ChatMessageDTO sendMessageToUser(Long adminId, Long userId, String content) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update chat room
        UserChatRoom chatRoom = chatRoomRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        chatRoom.setLastMessage(content);
        chatRoom.setLastMessageTime(LocalDateTime.now());
        chatRoom.setAdminId(adminId);
        chatRoom.setAdminName(admin.getFullName());
        chatRoomRepository.save(chatRoom);

        // Save message
        UserChatMessage message = UserChatMessage.builder()
                .senderId(adminId)
                .senderName(admin.getFullName())
                .receiverId(userId)
                .receiverName(user.getFullName())
                .content(content)
                .isRead(false)
                .type(UserChatMessage.MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .build();

        message = chatMessageRepository.save(message);

        // Send to specific user via WebSocket
        ChatMessageDTO dto = convertToDTO(message);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                dto
        );

        log.info("Message sent from admin {} to user {}: {}", adminId, userId, content);
        return dto;
    }

    /**
     * Get chat history for user
     */
    public List<ChatMessageDTO> getChatHistory(Long userId) {
        return chatMessageRepository.findChatHistory(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active chat rooms (for admin)
     */
    public List<ChatRoomDTO> getActiveChatRooms() {
        return chatRoomRepository.findAllActiveRooms()
                .stream()
                .map(this::convertRoomToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread message count for user
     */
    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markAsRead(Long messageId) {
        UserChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        chatMessageRepository.save(message);
    }

    /**
     * Mark all messages from user as read (for admin)
     */
    @Transactional
    public void markUserMessagesAsRead(Long userId) {
        List<UserChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(0L);
        unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(userId))
                .forEach(msg -> {
                    msg.setRead(true);
                    chatMessageRepository.save(msg);
                });

        // Update chat room unread count
        UserChatRoom chatRoom = chatRoomRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        chatRoom.setUnreadCount(0);
        chatRoomRepository.save(chatRoom);
    }

    // Helper methods
    private ChatMessageDTO convertToDTO(UserChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .receiverId(message.getReceiverId())
                .receiverName(message.getReceiverName())
                .content(message.getContent())
                .isRead(message.isRead())
                .type(message.getType())
                .sentAt(message.getSentAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ChatRoomDTO convertRoomToDTO(UserChatRoom room) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .userId(room.getUserId())
                .userName(room.getUserName())
                .userEmail(room.getUserEmail())
                .lastMessage(room.getLastMessage())
                .lastMessageTime(room.getLastMessageTime())
                .unreadCount(room.getUnreadCount())
                .isActive(room.isActive())
                .build();
    }
}
