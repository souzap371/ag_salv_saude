package com.crm.integration.dto;

// import com.crm.integration.model.Funcionalidade;
// import com.crm.integration.model.Request;

// public class MosiaRequestDTO {
//     private String instancia_aplicacao;
//     private Request request;
//     private Funcionalidade funcionalidade;
//     private OcorrenciaMosia ocorrencia;

//     // Getters e Setters

//     public String getInstancia_aplicacao() {
//         return instancia_aplicacao;
//     }

//     public void setInstancia_aplicacao(String instancia_aplicacao) {
//         this.instancia_aplicacao = instancia_aplicacao;
//     }

//     public Request getRequest() {
//         return request;
//     }

//     public void setRequest(Request request) {
//         this.request = request;
//     }

//     public Funcionalidade getFuncionalidade() {
//         return funcionalidade;
//     }

//     public void setFuncionalidade(Funcionalidade funcionalidade) {
//         this.funcionalidade = funcionalidade;
//     }

//     public OcorrenciaMosia getOcorrencia() {
//         return ocorrencia;
//     }

//     public void setOcorrencia(OcorrenciaMosia ocorrencia) {
//         this.ocorrencia = ocorrencia;
//     }
// }


public class MosiaRequestDTO {

    private String instancia_aplicacao;
    private Request request;
    private Funcionalidade funcionalidade;
    private OcorrenciaMosia ocorrencia;

    // =========================
    // Getters e Setters PRINCIPAIS
    // =========================

    public String getInstancia_aplicacao() {
        return instancia_aplicacao;
    }

    public void setInstancia_aplicacao(String instancia_aplicacao) {
        this.instancia_aplicacao = instancia_aplicacao;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Funcionalidade getFuncionalidade() {
        return funcionalidade;
    }

    public void setFuncionalidade(Funcionalidade funcionalidade) {
        this.funcionalidade = funcionalidade;
    }

    public OcorrenciaMosia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(OcorrenciaMosia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }


    // ======================================
    // CLASSE INTERNA: OcorrenciaMosia
    // ======================================
    public static class OcorrenciaMosia {
        private String protocolo;
        private Atendimento atendimento;

        // Getters e Setters
        public String getProtocolo() {
            return protocolo;
        }

        public void setProtocolo(String protocolo) {
            this.protocolo = protocolo;
        }

        public Atendimento getAtendimento() {
            return atendimento;
        }

        public void setAtendimento(Atendimento atendimento) {
            this.atendimento = atendimento;
        }

        // ======================================
        // CLASSE INTERNA: Atendimento
        // ======================================
        public static class Atendimento {
            private Status status;

            // Getters e Setters
            public Status getStatus() {
                return status;
            }

            public void setStatus(Status status) {
                this.status = status;
            }

            // ======================================
            // CLASSE INTERNA: Status
            // ======================================
            public static class Status {
                private String descricao;

                public Status() {}

                public Status(String descricao) {
                    this.descricao = descricao;
                }

                public String getDescricao() {
                    return descricao;
                }

                public void setDescricao(String descricao) {
                    this.descricao = descricao;
                }
            }
        }
    }

    // ======================================
    // PLACEHOLDERS caso não existam em seu projeto
    // ======================================
    public static class Request {
        private String id;

        // Getter e Setter
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Funcionalidade {
        private String id;
        private String descricao;

        // Getters e Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
    }
}
