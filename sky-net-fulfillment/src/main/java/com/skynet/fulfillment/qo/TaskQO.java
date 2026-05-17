package com.skynet.fulfillment.qo;

import lombok.Data;

@Data
public class TaskQO {
    private Long workflowId;
    private Long spuId;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
}
