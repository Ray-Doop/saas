package com.smartcloud.workflow.repository;

import com.smartcloud.workflow.entity.WorkflowPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowPO, Long>, QuerydslPredicateExecutor<WorkflowPO> {
}
