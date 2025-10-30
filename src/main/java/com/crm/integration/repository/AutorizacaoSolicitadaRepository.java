package com.crm.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.integration.model.AutorizacaoSolicitada;

import java.util.List;

@Repository
public interface AutorizacaoSolicitadaRepository extends JpaRepository<AutorizacaoSolicitada, Long> {
    List<AutorizacaoSolicitada> findBySincronizadoMosia(String sincronizadoMosia);
}
