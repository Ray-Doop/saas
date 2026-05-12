package com.smartcloud.workflow.qo;

import lombok.Data;

@Data
public class WorkflowQO {
    private String workflowName;
    private String category;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
