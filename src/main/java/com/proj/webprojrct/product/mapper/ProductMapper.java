package com.proj.webprojrct.product.mapper;

import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.product.dto.ProductCreateDto;
import com.proj.webprojrct.product.dto.ProductUpdateDto;
import com.proj.webprojrct.product.dto.ProductDetailDto;
import com.proj.webprojrct.product.dto.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategoryIdToEntity")
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductUpdateDto dto);

    ProductCreateDto toCreateDto(Product product);
    ProductUpdateDto toUpdateDto(Product product);
    
    // Mapping helpers used by ProductService
    ProductDetailDto toDetailDto(Product product);
    ProductResponseDto toResponseDto(Product product);

    @Named("mapCategoryIdToEntity")
    default Category mapCategoryIdToEntity(Long categoryId) {
        if (categoryId == null) return null;
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }
}
