package com.crm.integration.repository;

import com.crm.integration.model.AutorizacaoSolicitada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AutorizacaoRepository extends JpaRepository<AutorizacaoSolicitada, Long> {
    Optional<AutorizacaoSolicitada> findByProtocolo(String protocolo);
}
