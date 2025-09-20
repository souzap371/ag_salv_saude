package com.crm.integration.service;

import com.crm.integration.dto.CallbackSolicitacaoDTO;
import com.crm.integration.dto.FormCampoDTO;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.repository.AutorizacaoRepository;
import com.crm.integration.repository.ArquivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AutorizacaoService {

    private final AutorizacaoRepository autorizacaoRepo;
    private final ArquivoRepository arquivoRepo;
    private final StorageService storageService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AutorizacaoService(AutorizacaoRepository autorizacaoRepo,
            ArquivoRepository arquivoRepo,
            StorageService storageService) {
        this.autorizacaoRepo = autorizacaoRepo;
        this.arquivoRepo = arquivoRepo;
        this.storageService = storageService;
    }

    /**
     * Processa o callback recebido do Mobile Saúde.
     * - Idempotência baseada no protocolo
     * - Persiste dados básicos
     * - Baixa arquivos (campos cujo value é URL)
     */
    // @Transactional
    // public void processCallback(CallbackSolicitacaoDTO dto) throws Exception {
    // Optional<AutorizacaoSolicitada> exist =
    // autorizacaoRepo.findByProtocolo(dto.getProtocolo());
    // AutorizacaoSolicitada ent = exist.orElseGet(AutorizacaoSolicitada::new);

    // if (ent.getId() == null) {
    // ent.setProtocolo(dto.getProtocolo());
    // ent.setCreatedAt(OffsetDateTime.now());
    // }

    // ent.setIdOperadora(dto.getIdOperadora());
    // ent.setMshash(dto.getMshash());
    // ent.setTipo(dto.getTipo());
    // ent.setNome(dto.getNome());
    // ent.setMatricula(dto.getMatricula());
    // ent.setMatriculaTitular(dto.getMatriculaTitular());
    // ent.setEquipeAtendimentoCms(dto.getEquipeAtendimentoCms());
    // ent.setLogin(dto.getLogin());
    // ent.setIdMsUsuario(dto.getIdMsUsuario());
    // ent.setTipoGuia(dto.getTipoGuia());
    // ent.setIdTipoGuia(dto.getIdTipoGuia());
    // ent.setLabel(dto.getLabel());
    // ent.setUpdatedAt(OffsetDateTime.now());

    // // salva raw JSON para auditoria
    // try {
    // String raw = mapper.writeValueAsString(dto);
    // ent.setRawCallbackJson(raw);
    // } catch (Exception e) {
    // // logging
    // }

    // autorizacaoRepo.save(ent);

    // // procurar campos com URLs (ex.: imagens)
    // if (dto.getCampos() != null) {
    // for (FormCampoDTO campo : dto.getCampos()) {
    // String val = campo.getValue();
    // if (val != null && (val.startsWith("http://") || val.startsWith("https://")))
    // {
    // // baixar o arquivo imediatamente (URL assinada expira)
    // try {
    // String localPath = storageService.downloadAndSave(val,
    // "uploads/autorizacoes");
    // AutorizacaoArquivo ar = new AutorizacaoArquivo();
    // ar.setNomeOriginal(campo.getLabel());
    // ar.setUrlOriginal(val);
    // ar.setCaminhoLocal(localPath);
    // ar.setBaixadoEm(OffsetDateTime.now());
    // ar.setAutorizacao(ent);
    // arquivoRepo.save(ar);
    // } catch (Exception ex) {
    // // registre erro mas não pare processamento
    // ex.printStackTrace();
    // }
    // }
    // }
    // }
    // }
    @Transactional
    public void processCallback(CallbackSolicitacaoDTO dto) throws Exception {
        // 0) Log inicial do callback recebido
        try {
            System.out.println("Callback recebido: " + mapper.writeValueAsString(dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1) Validação do protocolo
        if (dto.getProtocolo() == null || dto.getProtocolo().isBlank()) {
            System.err.println("Callback recebido sem protocolo: " + mapper.writeValueAsString(dto));
            // apenas ignora o callback inválido, sem lançar exceção
            return;
        }

        // 2) Busca por registro existente
        Optional<AutorizacaoSolicitada> exist = autorizacaoRepo.findByProtocolo(dto.getProtocolo());
        AutorizacaoSolicitada ent = exist.orElseGet(AutorizacaoSolicitada::new);

        // 3) Se novo registro, setar protocolo e createdAt
        if (ent.getId() == null) {
            ent.setProtocolo(dto.getProtocolo());
            ent.setCreatedAt(OffsetDateTime.now());
        }

        // 4) Atualiza campos
        ent.setIdOperadora(dto.getIdOperadora());
        ent.setMshash(dto.getMshash());
        ent.setTipo(dto.getTipo());
        ent.setNome(dto.getNome());
        ent.setMatricula(dto.getMatricula());
        ent.setMatriculaTitular(dto.getMatriculaTitular());
        ent.setEquipeAtendimentoCms(dto.getEquipeAtendimentoCms());
        ent.setLogin(dto.getLogin());
        ent.setIdMsUsuario(dto.getIdMsUsuario());
        ent.setTipoGuia(dto.getTipoGuia());
        ent.setIdTipoGuia(dto.getIdTipoGuia());
        ent.setLabel(dto.getLabel());
        ent.setUpdatedAt(OffsetDateTime.now());

        // 5) Salva raw JSON para auditoria
        try {
            String raw = mapper.writeValueAsString(dto);
            ent.setRawCallbackJson(raw);
        } catch (Exception e) {
            e.printStackTrace(); // apenas log
        }

        // 6) Persiste entidade principal
        autorizacaoRepo.save(ent);

        // 7) Processa campos com URLs (imagens/arquivos)
        if (dto.getCampos() != null) {
            for (FormCampoDTO campo : dto.getCampos()) {
                String val = campo.getValue();
                if (val != null && (val.startsWith("http://") || val.startsWith("https://"))) {
                    try {
                        String localPath = storageService.downloadAndSave(val, "uploads/autorizacoes");
                        AutorizacaoArquivo ar = new AutorizacaoArquivo();
                        ar.setNomeOriginal(campo.getLabel());
                        ar.setUrlOriginal(val);
                        ar.setCaminhoLocal(localPath);
                        ar.setBaixadoEm(OffsetDateTime.now());
                        ar.setAutorizacao(ent);
                        arquivoRepo.save(ar);
                    } catch (Exception ex) {
                        ex.printStackTrace(); // registra erro mas continua
                    }
                }
            }
        }
    }

    public Optional<AutorizacaoSolicitada> findByProtocolo(String protocolo) {
        return autorizacaoRepo.findByProtocolo(protocolo);
    }

    // método para atualizar status local (ex.: ao aprovar/rejeitar no seu sistema)
    @Transactional
    public void updateStatusLocal(String protocolo, String novoStatus) {
        autorizacaoRepo.findByProtocolo(protocolo).ifPresent(ent -> {
            ent.setStatus(novoStatus);
            ent.setUpdatedAt(OffsetDateTime.now());
            autorizacaoRepo.save(ent);
            // opcional: notificar Mobile Saúde via API se houver endpoint
        });
    }
}
