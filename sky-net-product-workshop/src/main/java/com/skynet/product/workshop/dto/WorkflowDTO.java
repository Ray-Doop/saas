package com.skynet.product.workshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowDTO {

    @NotBlank(message = "工作流名称不能为空")
    private String workflowName;

    private String description;
    private String category;

    @NotBlank(message = "工作流JSON不能为空")
    private String workflowJson;

    private String previewImage;
    private Integer nodeCount;
}
