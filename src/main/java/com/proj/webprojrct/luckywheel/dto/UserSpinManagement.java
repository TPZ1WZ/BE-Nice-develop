package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho việc quản lý lượt quay của user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSpinManagement {
    private Long userId;
    private String username;
    private String email;
    private Integer spinsToday;
    private Integer spinsThisWeek;
    private Integer totalSpins;
    private Integer prizesWon;
}
