package com.crm.integration.repository;

import com.crm.integration.model.Ocorrencia;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
    Optional<Ocorrencia> findByProtocolo(String protocolo);

    // Busca os 10 últimos registros ordenados pela data de registro de forma
    // decrescente.
    List<Ocorrencia> findTop10ByOrderByDataRegistroDesc();

    // SALVA DADOS DE OCORRENCIA EM AG_TB_OCORRENCIA
    @Modifying
    @Transactional
    @Query(value = """
              INSERT INTO AG_TB_OCORRENCIAS
                (IDOCORRENCIA, CONTEUDO, DTCARGA, DTPROCESSA)
              VALUES
                (:id, :conteudo, :dtCarga, :dtProcessa)
            """, nativeQuery = true)
    void saveBlob(
            @Param("id") Long id,
            @Param("conteudo") byte[] conteudo,
            @Param("dtCarga") java.sql.Timestamp dtCarga,
            @Param("dtProcessa") java.sql.Timestamp dtProcessa);

}
