package com.smartcloud.product.entity;

import com.smartcloud.common.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_product_sku")
public class ProductSKUPO extends BasePO {

    @Column(name = "spu_id", nullable = false)
    private Long spuId;

    @Column(name = "sku_code", nullable = false, unique = true, length = 64)
    private String skuCode;

    @Column(name = "sku_name", nullable = false, length = 256)
    private String skuName;

    @Column(columnDefinition = "JSON")
    private String specs;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 512)
    private String image;

    @Column(nullable = false)
    private Integer status = 1;
}
