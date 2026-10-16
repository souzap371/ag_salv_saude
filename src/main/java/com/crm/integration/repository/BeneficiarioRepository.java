package com.crm.integration.repository;

import com.crm.integration.model.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, String> {
}
