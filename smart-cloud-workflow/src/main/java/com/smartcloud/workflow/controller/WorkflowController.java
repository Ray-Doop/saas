package com.smartcloud.workflow.controller;

import com.smartcloud.common.result.PageResult;
import com.smartcloud.common.result.Result;
import com.smartcloud.workflow.dto.WorkflowDTO;
import com.smartcloud.workflow.qo.WorkflowQO;
import com.smartcloud.workflow.service.WorkflowService;
import com.smartcloud.workflow.vo.WorkflowVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public Result<WorkflowVO> create(@Valid @RequestBody WorkflowDTO dto) {
        return Result.ok(workflowService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<WorkflowVO> update(@PathVariable Long id, @Valid @RequestBody WorkflowDTO dto) {
        return Result.ok(workflowService.update(id, dto));
    }

    @GetMapping("/{id}")
    public Result<WorkflowVO> getById(@PathVariable Long id) {
        return Result.ok(workflowService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<WorkflowVO>> page(WorkflowQO qo) {
        return Result.ok(workflowService.page(qo));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        workflowService.delete(id);
        return Result.ok();
    }
}
