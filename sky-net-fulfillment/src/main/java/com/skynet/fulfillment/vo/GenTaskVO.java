package com.skynet.fulfillment.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GenTaskVO {
    private Long id;
    private String taskCode;
    private Long workflowId;
    private Long spuId;
    private Long skuId;
    private String productImage;
    private String inputParams;
    private String status;
    private String resultImages;
    private Integer progress;
    private String errorMessage;
    private Integer costPoints;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private List<GenResultVO> results;
}
