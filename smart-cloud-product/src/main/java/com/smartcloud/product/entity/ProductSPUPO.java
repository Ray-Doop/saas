package com.smartcloud.product.entity;

import com.smartcloud.common.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_product_spu")
public class ProductSPUPO extends BasePO {

    @Column(name = "spu_code", nullable = false, unique = true, length = 64)
    private String spuCode;

    @Column(name = "spu_name", nullable = false, length = 256)
    private String spuName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 128)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "main_image", length = 512)
    private String mainImage;

    @Column(name = "image_list", columnDefinition = "JSON")
    private String imageList;

    @Column(nullable = false)
    private Integer status = 1;
}
