package com.smartcloud.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class TaskCreateDTO {

    @NotNull(message = "工作流ID不能为空")
    private Long workflowId;

    private Long spuId;

    private Long skuId;

    private String productImage;

    /** 变量映射: key=占位符名称, value=实际值 */
    private Map<String, String> variableMap;
}
