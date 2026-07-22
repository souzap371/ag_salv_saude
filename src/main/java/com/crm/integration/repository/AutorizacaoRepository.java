// package com.crm.integration.repository;

// import com.crm.integration.model.AutorizacaoSolicitada;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.Optional;

// public interface AutorizacaoRepository extends JpaRepository<AutorizacaoSolicitada, Long> {
//     // Optional<AutorizacaoSolicitada> findByProtocolo(String protocolo);
//     // novos métodos:
//     List<AutorizacaoSolicitada> findAllByProtocoloOrderByCreatedAtAsc(String protocolo);

//     // para retornar o mais recente (se necessário)
//     Optional<AutorizacaoSolicitada> findTopByProtocoloOrderByCreatedAtDesc(String protocolo);

//     // Retorna todas as autorizações com o mesmo protocolo
//     List<AutorizacaoSolicitada> findByProtocolo(String protocolo);

//     Optional<AutorizacaoSolicitada> findTopByChaveUnicaOrderByCreatedAtDesc(String chaveUnica);

//     @Modifying
//     @Query("""
//                 UPDATE AutorizacaoSolicitada a
//                    SET a.sincronizadoMosia = 'S',
//                        a.ultimaSincronizacao = :dataSincronizacao,
//                        a.updatedAt = :dataSincronizacao
//                  WHERE a.protocolo = :protocolo
//                    AND a.sincronizadoMosia = 'N'
//             """)
//     int marcarPendenciasComoSincronizadas(
//             @Param("protocolo") String protocolo,
//             @Param("dataSincronizacao") OffsetDateTime dataSincronizacao);

// }



package com.crm.integration.repository;

import com.crm.integration.model.AutorizacaoSolicitada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutorizacaoRepository
        extends JpaRepository<AutorizacaoSolicitada, Long> {

    /**
     * Retorna todas as versões de um protocolo em ordem cronológica.
     */
    List<AutorizacaoSolicitada>
    findAllByProtocoloOrderByCreatedAtAsc(String protocolo);

    /**
     * Retorna a versão mais recente de um protocolo.
     */
    Optional<AutorizacaoSolicitada>
    findTopByProtocoloOrderByCreatedAtDesc(String protocolo);

    /**
     * Retorna somente o registro mais recente de cada protocolo,
     * desde que esse registro esteja pendente de sincronização.
     *
     * Isso impede que versões históricas antigas sejam reenviadas.
     */
    @Query("""
        SELECT a
          FROM AutorizacaoSolicitada a
         WHERE a.sincronizadoMosia = 'N'
           AND a.createdAt = (
               SELECT MAX(a2.createdAt)
                 FROM AutorizacaoSolicitada a2
                WHERE a2.protocolo = a.protocolo
           )
         ORDER BY a.createdAt ASC
    """)
    List<AutorizacaoSolicitada> buscarUltimasPendencias();

    /**
     * Marca como sincronizadas todas as versões pendentes de um protocolo.
     *
     * Deve ser executado quando chegar um callback da própria Mosia,
     * porque o callback confirma que a ocorrência já foi processada.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AutorizacaoSolicitada a
           SET a.sincronizadoMosia = 'S',
               a.ultimaSincronizacao = :dataSincronizacao,
               a.updatedAt = :dataSincronizacao
         WHERE a.protocolo = :protocolo
           AND (
               a.sincronizadoMosia IS NULL
               OR a.sincronizadoMosia <> 'S'
           )
    """)
    int marcarProtocoloComoSincronizado(
            @Param("protocolo") String protocolo,
            @Param("dataSincronizacao")
            OffsetDateTime dataSincronizacao
    );
}