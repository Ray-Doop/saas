package com.skynet.fulfillment.controller;

import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.Result;
import com.skynet.fulfillment.dto.TaskCreateDTO;
import com.skynet.fulfillment.qo.TaskQO;
import com.skynet.fulfillment.service.TaskService;
import com.skynet.fulfillment.vo.GenTaskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 创建AI生成任务
     * workflowJson 需由调用方从WorkflowService获取后传入(通过Gateway内部调用时由Feign组装)
     * 这里简化处理: 前端传入workflowId, controller层调用workflowService获取JSON
     */
    @PostMapping
    public Result<GenTaskVO> create(@Valid @RequestBody TaskCreateDTO dto) {
        // MVP阶段简化: workflowJson从请求头或内部调用获取
        // 实际生产环境应通过Feign调用WorkflowService
        return Result.fail("请通过 /api/task/create-with-workflow 接口创建任务，需传入完整workflowJson");
    }

    @PostMapping("/create")
    public Result<GenTaskVO> createWithWorkflow(
            @Valid @RequestBody TaskCreateDTO dto,
            @RequestHeader(value = "X-Workflow-Json", required = false) String workflowJson) {
        if (workflowJson == null || workflowJson.isEmpty()) {
            return Result.fail("缺少工作流JSON");
        }
        return Result.ok(taskService.create(dto, workflowJson));
    }

    @GetMapping("/{id}")
    public Result<GenTaskVO> getById(@PathVariable Long id) {
        return Result.ok(taskService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<GenTaskVO>> page(TaskQO qo) {
        return Result.ok(taskService.page(qo));
    }

    @PostMapping("/{id}/retry")
    public Result<?> retry(@PathVariable Long id) {
        taskService.retry(id);
        return Result.ok();
    }
}
