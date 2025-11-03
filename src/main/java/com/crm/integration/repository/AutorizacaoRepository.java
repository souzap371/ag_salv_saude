package com.crm.integration.repository;

import com.crm.integration.model.AutorizacaoSolicitada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorizacaoRepository extends JpaRepository<AutorizacaoSolicitada, Long> {
    // Optional<AutorizacaoSolicitada> findByProtocolo(String protocolo);
     // novos métodos:
    List<AutorizacaoSolicitada> findAllByProtocoloOrderByCreatedAtAsc(String protocolo);

    // para retornar o mais recente (se necessário)
    Optional<AutorizacaoSolicitada> findTopByProtocoloOrderByCreatedAtDesc(String protocolo);

    // Retorna todas as autorizações com o mesmo protocolo
    List<AutorizacaoSolicitada> findByProtocolo(String protocolo);

    Optional<AutorizacaoSolicitada> findTopByChaveUnicaOrderByCreatedAtDesc(String chaveUnica);

}
