package com.skynet.fulfillment.repository;

import com.skynet.fulfillment.entity.GenResultPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenResultRepository extends JpaRepository<GenResultPO, Long>, QuerydslPredicateExecutor<GenResultPO> {
    List<GenResultPO> findByTaskIdOrderByImageIndexAsc(Long taskId);
}
