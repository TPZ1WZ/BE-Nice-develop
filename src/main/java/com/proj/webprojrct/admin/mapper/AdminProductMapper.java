package com.proj.webprojrct.admin.mapper;

import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.admin.dto.request.ProductManagementRequest;
import com.proj.webprojrct.admin.dto.response.ProductManagementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminProductMapper {
    AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductManagementRequest request);

    ProductManagementResponse toResponse(Product product);

    List<ProductManagementResponse> toResponseList(List<Product> products);
    List<Product> toEntityList(List<ProductManagementRequest> requests);
}