package com.proj.webprojrct.user;

import com.proj.webprojrct.common.mapper.IdMapper;
import com.proj.webprojrct.user.dto.request.UserCreateRequest;
import com.proj.webprojrct.user.dto.request.UserUpdateRequest;
import com.proj.webprojrct.user.dto.request.UserUpdateStatusRequest;
import com.proj.webprojrct.user.dto.response.UserCreateResponse;
import com.proj.webprojrct.user.dto.response.UserResponse;
import com.proj.webprojrct.user.dto.response.UserUpdateResponse;
import com.proj.webprojrct.user.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // request -> Entity
    // Add explicit @Mapping annotations if field names differ or are missing in DTOs

    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "phone", source = "phoneNumber") 
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserCreateRequest user);

    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserUpdateRequest user);

    User toEntity(UserUpdateStatusRequest user);

    // Entity -> response
    UserCreateResponse toCreateResponse(User user);

    UserUpdateResponse toUpdateResponse(User user);

    UserResponse toResponse(User user);
    
    // Entity List -> response list
    List<UserResponse> toResponse(List<User> users);

}
