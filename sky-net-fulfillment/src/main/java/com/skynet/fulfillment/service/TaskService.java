package com.skynet.fulfillment.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skynet.common.core.exception.BizException;
import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.ResultCode;
import com.skynet.fulfillment.dto.TaskCreateDTO;
import com.skynet.fulfillment.entity.GenResultPO;
import com.skynet.fulfillment.entity.GenTaskPO;
import com.skynet.fulfillment.entity.QGenTaskPO;
import com.skynet.fulfillment.entity.QGenResultPO;
import com.skynet.fulfillment.producer.TaskProducer;
import com.skynet.fulfillment.qo.TaskQO;
import com.skynet.fulfillment.repository.GenResultRepository;
import com.skynet.fulfillment.repository.GenTaskRepository;
import com.skynet.fulfillment.vo.GenResultVO;
import com.skynet.fulfillment.vo.GenTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final GenTaskRepository taskRepository;
    private final GenResultRepository resultRepository;
    private final JPAQueryFactory queryFactory;
    private final TaskProducer taskProducer;
    private final ObjectMapper objectMapper;

    /**
     * 创建AI生成任务
     * 1. 加载工作流模板(需通过WorkflowService API获取)
     * 2. 替换变量占位符生成最终JSON
     * 3. 持久化任务记录
     * 4. 通过RocketMQ发送到Python推理服务
     */
    @Transactional
    public GenTaskVO create(TaskCreateDTO dto, String workflowJson) {
        String taskCode = "TASK_" + IdUtil.fastSimpleUUID().toUpperCase();

        // 替换占位符, 生成工作流快照
        String workflowSnapshot = workflowJson;
        Map<String, Object> params = new HashMap<>();
        if (dto.getVariableMap() != null) {
            for (Map.Entry<String, String> entry : dto.getVariableMap().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                workflowSnapshot = workflowSnapshot.replace(placeholder, entry.getValue());
            }
            params.putAll(dto.getVariableMap());
        }

        // 保存任务
        GenTaskPO task = new GenTaskPO();
        task.setTaskCode(taskCode);
        task.setWorkflowId(dto.getWorkflowId());
        task.setSpuId(dto.getSpuId());
        task.setSkuId(dto.getSkuId());
        task.setProductImage(dto.getProductImage());
        task.setWorkflowSnapshot(workflowSnapshot);
        try {
            task.setInputParams(objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            task.setInputParams("{}");
        }
        task.setStatus("PENDING");
        task.setProgress(0);
        taskRepository.save(task);

        // 发送MQ消息到Python推理服务
        try {
            taskProducer.sendGenTask(taskCode, workflowSnapshot, dto.getProductImage(), params);

            task.setStatus("PROCESSING");
            task.setStartTime(LocalDateTime.now());
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("发送任务到MQ失败: taskCode={}", taskCode, e);
            task.setStatus("FAILED");
            task.setErrorMessage("发送任务失败: " + e.getMessage());
            taskRepository.save(task);
        }

        return getByTaskCode(taskCode);
    }

    public GenTaskVO getById(Long id) {
        GenTaskPO task = taskRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.TASK_NOT_EXIST));
        return toVO(task);
    }

    public GenTaskVO getByTaskCode(String taskCode) {
        GenTaskPO task = taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new BizException(ResultCode.TASK_NOT_EXIST));
        return toVO(task);
    }

    public PageResult<GenTaskVO> page(TaskQO qo) {
        QGenTaskPO q = QGenTaskPO.genTaskPO;
        var query = queryFactory.selectFrom(q).where(q.isDeleted.eq(0));

        if (qo.getWorkflowId() != null) {
            query.where(q.workflowId.eq(qo.getWorkflowId()));
        }
        if (qo.getSpuId() != null) {
            query.where(q.spuId.eq(qo.getSpuId()));
        }
        if (qo.getStatus() != null) {
            query.where(q.status.eq(qo.getStatus()));
        }

        query.orderBy(q.createTime.desc());

        long total = query.fetchCount();
        long offset = (long) (qo.getPage() - 1) * qo.getSize();
        List<GenTaskPO> pos = query.offset(offset).limit(qo.getSize()).fetch();
        List<GenTaskVO> vos = pos.stream().map(this::toVO).collect(Collectors.toList());

        return PageResult.of(total, qo.getPage(), qo.getSize(), vos);
    }

    @Transactional
    public void retry(Long taskId) {
        GenTaskPO task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ResultCode.TASK_NOT_EXIST));

        if (!"FAILED".equals(task.getStatus())) {
            throw new BizException(ResultCode.TASK_STATUS_ERROR);
        }

        task.setStatus("PENDING");
        task.setProgress(0);
        task.setErrorMessage(null);
        taskRepository.save(task);

        taskProducer.sendGenTask(
                task.getTaskCode(),
                task.getWorkflowSnapshot(),
                task.getProductImage(),
                null
        );

        task.setStatus("PROCESSING");
        task.setStartTime(LocalDateTime.now());
        taskRepository.save(task);
    }

    private GenTaskVO toVO(GenTaskPO po) {
        GenTaskVO vo = new GenTaskVO();
        BeanUtils.copyProperties(po, vo);

        QGenResultPO qResult = QGenResultPO.genResultPO;
        List<GenResultPO> results = queryFactory.selectFrom(qResult)
                .where(qResult.taskId.eq(po.getId()))
                .orderBy(qResult.imageIndex.asc())
                .fetch();

        List<GenResultVO> resultVOs = results.stream().map(r -> {
            GenResultVO rvo = new GenResultVO();
            BeanUtils.copyProperties(r, rvo);
            return rvo;
        }).collect(Collectors.toList());
        vo.setResults(resultVOs);

        return vo;
    }
}
