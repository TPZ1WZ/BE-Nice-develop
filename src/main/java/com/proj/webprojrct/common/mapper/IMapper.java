package com.proj.webprojrct.common.mapper;

import java.util.List;

/*
    Đây là interface chung của Mapper
    TODO: Định nghĩa các class mapper thừa kế như theo module, ví dụ BlogMapper
    Lưu ý có thể dùng mapstruct để generate code (Khuyến khích) hoặc viết thủ công
 */
public interface IMapper<E, D> {

    // DTO (request) → Entity
    E toEntity(D dto);

    // Entity → DTO (response)
    D toDto(E entity);

    // List<Entity> → List<DTO>
    List<D> toDtoList(List<E> entities);

    // List<DTO> → List<Entity>
    List<E> toEntityList(List<D> dtos);
}
