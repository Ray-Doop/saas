package com.skynet.auth.repository;

import com.skynet.auth.entity.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserPO, Long>, QuerydslPredicateExecutor<UserPO> {
    Optional<UserPO> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
