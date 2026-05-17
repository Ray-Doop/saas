package com.skynet.product.workshop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSKUVO {
    private Long id;
    private Long spuId;
    private String skuCode;
    private String skuName;
    private String specs;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private String image;
    private Integer status;
    private LocalDateTime createTime;
}
