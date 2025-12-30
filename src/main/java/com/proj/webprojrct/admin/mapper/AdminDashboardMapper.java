package com.proj.webprojrct.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

// Temporarily disabled until mapper methods are added
// @Mapper(componentModel = "spring")
public interface AdminDashboardMapper {
    AdminDashboardMapper INSTANCE = Mappers.getMapper(AdminDashboardMapper.class);

    // Dashboard statistics mapper methods will be added later
    
}