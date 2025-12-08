package com.proj.webprojrct.admin.mapper;

import com.proj.webprojrct.order.entity.Order;
import com.proj.webprojrct.admin.dto.request.OrderManagementRequest;
import com.proj.webprojrct.admin.dto.response.OrderManagementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminOrderMapper {
    AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderManagementRequest request);

    OrderManagementResponse toResponse(Order order);

    List<OrderManagementResponse> toResponseList(List<Order> orders);
    List<Order> toEntityList(List<OrderManagementRequest> requests);
}