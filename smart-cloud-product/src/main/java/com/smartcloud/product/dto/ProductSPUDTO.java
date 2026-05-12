package com.smartcloud.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSPUDTO {

    @NotBlank(message = "SPU编码不能为空")
    private String spuCode;

    @NotBlank(message = "商品名称不能为空")
    private String spuName;

    private Long categoryId;
    private String brand;
    private String description;
    private String mainImage;
    private List<String> imageList;
    private List<SKUItem> skuList;

    @Data
    public static class SKUItem {
        private String skuCode;
        private String skuName;
        private String specs;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stock;
        private String image;
    }
}
