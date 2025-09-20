package com.crm.integration.repository;

import com.crm.integration.model.AutorizacaoArquivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArquivoRepository extends JpaRepository<AutorizacaoArquivo, Long> {
}
