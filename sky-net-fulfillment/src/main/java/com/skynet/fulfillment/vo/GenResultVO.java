package com.skynet.fulfillment.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GenResultVO {
    private Long id;
    private Long taskId;
    private String imageUrl;
    private Integer imageIndex;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private LocalDateTime createTime;
}
