package com.smartcloud.product.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductSPUVO {
    private Long id;
    private String spuCode;
    private String spuName;
    private Long categoryId;
    private String brand;
    private String description;
    private String mainImage;
    private String imageList;
    private Integer status;
    private List<ProductSKUVO> skuList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
