package com.proj.webprojrct.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateStatusRequest {

    @JsonProperty("is_active")
    private Boolean isActive;
}
