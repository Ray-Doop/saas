package com.skynet.product.workshop.repository;

import com.skynet.product.workshop.entity.ProductSKUPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSKURepository extends JpaRepository<ProductSKUPO, Long>, QuerydslPredicateExecutor<ProductSKUPO> {
    List<ProductSKUPO> findBySpuId(Long spuId);
    void deleteBySpuId(Long spuId);
}
