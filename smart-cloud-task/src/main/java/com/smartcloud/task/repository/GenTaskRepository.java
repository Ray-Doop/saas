package com.smartcloud.task.repository;

import com.smartcloud.task.entity.GenTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenTaskRepository extends JpaRepository<GenTaskPO, Long>, QuerydslPredicateExecutor<GenTaskPO> {
    Optional<GenTaskPO> findByTaskCode(String taskCode);
}
