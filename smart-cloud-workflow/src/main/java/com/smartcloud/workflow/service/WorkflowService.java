package com.smartcloud.workflow.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smartcloud.common.exception.BizException;
import com.smartcloud.common.result.PageResult;
import com.smartcloud.common.result.ResultCode;
import com.smartcloud.workflow.dto.WorkflowDTO;
import com.smartcloud.workflow.entity.QWorkflowPO;
import com.smartcloud.workflow.entity.WorkflowPO;
import com.smartcloud.workflow.qo.WorkflowQO;
import com.smartcloud.workflow.repository.WorkflowRepository;
import com.smartcloud.workflow.vo.WorkflowVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final JPAQueryFactory queryFactory;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkflowVO create(WorkflowDTO dto) {
        validateWorkflowJson(dto.getWorkflowJson());

        WorkflowPO po = new WorkflowPO();
        BeanUtils.copyProperties(dto, po);
        po.setStatus(1);
        po.setNodeCount(dto.getNodeCount() != null ? dto.getNodeCount() : countNodes(dto.getWorkflowJson()));
        workflowRepository.save(po);

        return toVO(po);
    }

    @Transactional
    public WorkflowVO update(Long id, WorkflowDTO dto) {
        WorkflowPO po = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.WORKFLOW_NOT_EXIST));

        validateWorkflowJson(dto.getWorkflowJson());
        BeanUtils.copyProperties(dto, po, "id", "createTime", "updateTime", "isDeleted");
        po.setNodeCount(dto.getNodeCount() != null ? dto.getNodeCount() : countNodes(dto.getWorkflowJson()));
        workflowRepository.save(po);

        return toVO(po);
    }

    public WorkflowVO getById(Long id) {
        WorkflowPO po = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.WORKFLOW_NOT_EXIST));
        return toVO(po);
    }

    public PageResult<WorkflowVO> page(WorkflowQO qo) {
        QWorkflowPO q = QWorkflowPO.workflowPO;
        var query = queryFactory.selectFrom(q).where(q.isDeleted.eq(0));

        if (qo.getWorkflowName() != null) {
            query.where(q.workflowName.like("%" + qo.getWorkflowName() + "%"));
        }
        if (qo.getCategory() != null) {
            query.where(q.category.eq(qo.getCategory()));
        }
        if (qo.getStatus() != null) {
            query.where(q.status.eq(qo.getStatus()));
        }

        long total = query.fetchCount();
        long offset = (long) (qo.getPage() - 1) * qo.getSize();
        List<WorkflowVO> vos = query.offset(offset).limit(qo.getSize())
                .fetch().stream().map(this::toVO).collect(Collectors.toList());

        return PageResult.of(total, qo.getPage(), qo.getSize(), vos);
    }

    @Transactional
    public void delete(Long id) {
        WorkflowPO po = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.WORKFLOW_NOT_EXIST));
        workflowRepository.delete(po);
    }

    private void validateWorkflowJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BizException("工作流JSON格式无效: " + e.getMessage());
        }
    }

    private int countNodes(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("nodes")) {
                return root.get("nodes").size();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private WorkflowVO toVO(WorkflowPO po) {
        WorkflowVO vo = new WorkflowVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }
}
