# CRM Mosia — SALV Saúde

Integração Spring Boot responsável pelo recebimento e acompanhamento de
ocorrências do Mosia e pelo envio de mensagens ao aplicativo.

## Recursos ativos

- API pública de ocorrências em `/clientes/ocorrencias/**`.
- API pública de mensageria em `/clientes/mensageria/**`.
- Classificação dinâmica de tipos de ocorrência, preservando a nomenclatura
  recebida do Mosia.
- Catálogo de status separado por tipo de ocorrência.
- Histórico de alterações de status.
- Mensageria genérica por ocorrência através da tabela `AG_MENSAPP`.
- Registro opcional do número da guia em `AG_MENSAPP.NNUMEGUIA`.
- Armazenamento dos formulários recebidos em `FORMULARIOS.DADOS_JSON`.
- Painel de monitoramento e administração em `/status.html`.
- Documentação funcional e técnica em `/documentacao.html`, com geração de PDF.
- Swagger interativo em `/swagger-ui/index.html`.

Os endpoints legados permanecem no código para eventual compatibilidade, mas
são desativados por padrão por `FEATURES_LEGACY_ENDPOINTS_ENABLED=false`. A
sincronização antiga baseada em `AUTORIZACAO_SOLICITADA` também fica desativada
por padrão através de `FEATURES_LEGACY_AUTORIZACAO_SYNC_ENABLED=false`.

## Fluxo de atualização

Ao alterar o status de uma ocorrência, o sistema localiza o registro pelo
protocolo, atualiza `OCORRENCIAS`, grava o histórico, sincroniza o novo estado
com o Mosia e inclui uma mensagem idempotente na `AG_MENSAPP`. O número da guia
pode ser enviado no request ou identificado no JSON do formulário; ocorrências
sem guia continuam funcionando normalmente.

## Configuração segura

Credenciais devem ser fornecidas exclusivamente por variáveis de ambiente. Não
registre tokens, senhas ou dados pessoais no README, na documentação HTML ou em
arquivos versionados.


