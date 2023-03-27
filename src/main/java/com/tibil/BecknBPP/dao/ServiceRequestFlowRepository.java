package com.tibil.BecknBPP.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tibil.BecknBPP.model.ServiceRequestFlow;


@Repository
@Transactional
public interface ServiceRequestFlowRepository extends JpaRepository<ServiceRequestFlow, Long> {

	List<ServiceRequestFlow> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
