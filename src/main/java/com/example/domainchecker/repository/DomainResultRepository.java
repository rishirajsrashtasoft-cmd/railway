package com.example.domainchecker.repository;

import com.example.domainchecker.entity.DomainResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainResultRepository extends JpaRepository<DomainResult, Long> {

    @Query("SELECT dr FROM DomainResult dr ORDER BY dr.createdAt DESC")
    List<DomainResult> findAllOrderByCreatedAtDesc();
}

