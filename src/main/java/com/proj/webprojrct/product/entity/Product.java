
package com.proj.webprojrct.product.entity;

import com.proj.webprojrct.category.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.proj.webprojrct.common.entity.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {
    private String name;
    private String slug;
    private String subTitle;
    @Column(columnDefinition = "text")
    private String description;
    private Double price;
    private boolean isDelete;
    private Integer stock;

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> images;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
