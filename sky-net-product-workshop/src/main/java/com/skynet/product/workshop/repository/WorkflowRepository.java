package com.skynet.product.workshop.repository;

import com.skynet.product.workshop.entity.WorkflowPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowPO, Long>, QuerydslPredicateExecutor<WorkflowPO> {
}
