package com.skynet.product.workshop.repository;

import com.skynet.product.workshop.entity.ProductSPUPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSPURepository extends JpaRepository<ProductSPUPO, Long>, QuerydslPredicateExecutor<ProductSPUPO> {
    Optional<ProductSPUPO> findBySpuCode(String spuCode);
    boolean existsBySpuCode(String spuCode);
}
