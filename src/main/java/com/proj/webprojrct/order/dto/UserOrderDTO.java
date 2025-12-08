package com.proj.webprojrct.order.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
}
