package com.skynet.product.workshop.controller;

import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.Result;
import com.skynet.product.workshop.dto.WorkflowDTO;
import com.skynet.product.workshop.qo.WorkflowQO;
import com.skynet.product.workshop.service.WorkflowService;
import com.skynet.product.workshop.vo.WorkflowVO;
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
