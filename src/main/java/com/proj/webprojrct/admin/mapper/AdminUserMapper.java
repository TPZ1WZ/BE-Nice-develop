package com.proj.webprojrct.admin.mapper;

import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.admin.dto.request.UserManagementRequest;
import com.proj.webprojrct.admin.dto.response.UserManagementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserManagementRequest request);

    UserManagementResponse toResponse(User user);

    List<UserManagementResponse> toResponseList(List<User> users);
    List<User> toEntityList(List<UserManagementRequest> requests);
}