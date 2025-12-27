package com.proj.webprojrct.chatbot.repository;

import com.proj.webprojrct.chatbot.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    
    Optional<ChatConversation> findBySessionId(String sessionId);
    
    List<ChatConversation> findByUserId(Long userId);
}
