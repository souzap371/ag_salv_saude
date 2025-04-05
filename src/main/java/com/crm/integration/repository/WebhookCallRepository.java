package com.crm.integration.repository;


import com.crm.integration.model.WebhookCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookCallRepository extends JpaRepository<WebhookCall, Long> {
    // Métodos adicionais conforme necessário.
}

