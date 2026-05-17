package com.skynet.fulfillment.entity;

import com.skynet.common.core.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_gen_task")
public class GenTaskPO extends BasePO {

    @Column(name = "task_code", nullable = false, unique = true, length = 64)
    private String taskCode;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "spu_id")
    private Long spuId;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "product_image", length = 512)
    private String productImage;

    @Column(name = "workflow_snapshot", nullable = false, columnDefinition = "LONGTEXT")
    private String workflowSnapshot;

    @Column(name = "input_params", columnDefinition = "JSON")
    private String inputParams;

    @Column(nullable = false, length = 32)
    private String status = "PENDING";

    @Column(name = "result_images", columnDefinition = "JSON")
    private String resultImages;

    private Integer progress = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "cost_points")
    private Integer costPoints = 0;

    @Column(name = "comfyui_prompt_id", length = 128)
    private String comfyuiPromptId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;
}
