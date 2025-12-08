package com.proj.webprojrct.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AdminReviewMapper {
    AdminReviewMapper INSTANCE = Mappers.getMapper(AdminReviewMapper.class);

    // Review admin mapper methods will be added later
    
}