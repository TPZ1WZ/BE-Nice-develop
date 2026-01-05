package com.proj.webprojrct.loyalty.dto;

import com.proj.webprojrct.loyalty.entity.LoyaltyTransaction;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho lịch sử giao dịch coin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {
    
    private Long id;
    private String transactionType; // EARN, SPEND, REFUND
    private Integer amount;
    private String source; // DAILY_CHECKIN, ORDER, REVIEW, etc.
    private String description;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
    
    /**
     * Convert từ Entity sang DTO
     */
    public static TransactionHistoryResponse fromEntity(LoyaltyTransaction transaction) {
        return TransactionHistoryResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .source(transaction.getSource())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
