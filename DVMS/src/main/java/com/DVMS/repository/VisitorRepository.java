package com.DVMS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DVMS.entity.Visitor;

import java.util.List;
@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
	long countByStatus(String status);
    List<Visitor> findByStatus(String status);
    List<Visitor> findByStatusAndCancellationReason(String status, String cancellationReason);
}
