package com.proj.webprojrct.user.dto.request;

import org.springframework.web.multipart.MultipartFile;
import com.proj.webprojrct.user.entity.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateMultipartRequest {

    @Schema(description = "Tên người dùng")
    private String full_name;

    @Schema(description = "Vai trò người dùng")
    private UserRole role;

    @Schema(description = "Ảnh đại diện", type = "string", format = "binary")
    private MultipartFile avatar;
}
