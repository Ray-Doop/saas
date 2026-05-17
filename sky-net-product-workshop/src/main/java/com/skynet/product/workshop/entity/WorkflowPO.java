package com.skynet.product.workshop.entity;

import com.skynet.common.core.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_workflow")
public class WorkflowPO extends BasePO {

    @Column(name = "workflow_name", nullable = false, length = 256)
    private String workflowName;

    @Column(length = 1024)
    private String description;

    @Column(length = 64)
    private String category;

    @Column(name = "workflow_json", nullable = false, columnDefinition = "LONGTEXT")
    private String workflowJson;

    @Column(name = "preview_image", length = 512)
    private String previewImage;

    @Column(name = "node_count")
    private Integer nodeCount = 0;

    @Column(nullable = false)
    private Integer status = 1;
}
