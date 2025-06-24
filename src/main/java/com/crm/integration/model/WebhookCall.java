// package com.crm.integration.model;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

// import java.time.Instant;

// @Entity
// @Table(name = "webhook_calls")
// public class WebhookCall {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     // Armazena o payload recebido como String. Pode ser alterado para JSON ou outro tipo conforme necessidade.
//     @Column(columnDefinition = "TEXT")
//     private String payload;

//     private long receivedAt;

//     public WebhookCall() {}

//     public WebhookCall(String payload) {
//         this.payload = payload;
//         this.receivedAt = Instant.now().toEpochMilli();
//     }

//     public Long getId() {
//         return id;
//     }

//     public String getPayload() {
//         return payload;
//     }

//     public long getReceivedAt() {
//         return receivedAt;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public void setPayload(String payload) {
//         this.payload = payload;
//     }

//     public void setReceivedAt(long receivedAt) {
//         this.receivedAt = receivedAt;
//     }
// }
package com.crm.integration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "webhook_calls")
public class WebhookCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usa @Lob para armazenar grandes textos, funcionando em Oracle e H2
    @Lob
    @Column
    private String payload;

    private long receivedAt;

    public WebhookCall() {
    }

    public WebhookCall(String payload) {
        this.payload = payload;
        this.receivedAt = Instant.now().toEpochMilli();
    }

    public Long getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public long getReceivedAt() {
        return receivedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setReceivedAt(long receivedAt) {
        this.receivedAt = receivedAt;
    }
}
