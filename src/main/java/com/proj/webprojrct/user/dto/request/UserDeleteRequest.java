package com.proj.webprojrct.user.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDeleteRequest {

    private String email;
}
