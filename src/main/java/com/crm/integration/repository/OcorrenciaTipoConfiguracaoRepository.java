package com.crm.integration.repository;

import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OcorrenciaTipoConfiguracaoRepository extends JpaRepository<OcorrenciaTipoConfiguracao, Long> {
    List<OcorrenciaTipoConfiguracao> findByAtivoTrue();
    boolean existsByIdTipoOcorrencia(Integer idTipoOcorrencia);
}
