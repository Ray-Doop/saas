package com.smartcloud.workflow.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowVO {
    private Long id;
    private String workflowName;
    private String description;
    private String category;
    private String workflowJson;
    private String previewImage;
    private Integer nodeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
