package com.crm.integration.repository;


import com.crm.integration.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
    Optional<Ocorrencia> findByProtocolo(String protocolo);

    // Busca os 10 últimos registros ordenados pela data de registro de forma decrescente.
    List<Ocorrencia> findTop10ByOrderByDataRegistroDesc();
}
