package com.crm.integration.repository;

import com.crm.integration.model.AgMensApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgMensAppRepository extends JpaRepository<AgMensApp, Long> {
}
