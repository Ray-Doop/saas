package com.skynet.product.workshop.qo;

import lombok.Data;

@Data
public class ProductSPUQO {
    private String spuName;
    private String spuCode;
    private Long categoryId;
    private String brand;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
