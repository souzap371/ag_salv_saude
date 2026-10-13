package com.crm.integration.repository;

import com.crm.integration.model.MosiaStatusConfiguracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MosiaStatusConfiguracaoRepository extends JpaRepository<MosiaStatusConfiguracao, Long> {
    Optional<MosiaStatusConfiguracao> findByIdStatusMosiaAndIdTipoOcorrenciaMosia(
            Integer idStatusMosia, Integer idTipoOcorrenciaMosia);
    Optional<MosiaStatusConfiguracao> findFirstByIdStatusMosiaAndAtivoTrue(Integer idStatusMosia);
    List<MosiaStatusConfiguracao> findByIdTipoOcorrenciaMosiaOrderByIdStatusMosia(Integer idTipoOcorrenciaMosia);
}
