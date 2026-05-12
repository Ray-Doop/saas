package com.smartcloud.task.entity;

import com.smartcloud.common.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_gen_result")
public class GenResultPO extends BasePO {

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "image_index")
    private Integer imageIndex = 0;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer width;

    private Integer height;
}
