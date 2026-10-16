-- Expansão compatível para IDs de funcionalidade do Mosia que não são inteiros,
-- por exemplo: 9999.00069. As colunas numéricas anteriores são preservadas.

DECLARE
    coluna_existente NUMBER;
BEGIN
    SELECT COUNT(*) INTO coluna_existente
      FROM user_tab_columns
     WHERE table_name = 'OCORRENCIAS'
       AND column_name = 'CODIGO_FUNCIONALIDADE';

    IF coluna_existente = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ocorrencias ADD (codigo_funcionalidade VARCHAR2(100))';
    END IF;
END;
/

DECLARE
    coluna_existente NUMBER;
BEGIN
    SELECT COUNT(*) INTO coluna_existente
      FROM user_tab_columns
     WHERE table_name = 'OCORRENCIA_TIPO_CONFIG'
       AND column_name = 'CODIGO_FUNCIONALIDADE';

    IF coluna_existente = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ocorrencia_tipo_config ADD (codigo_funcionalidade VARCHAR2(100))';
    END IF;
END;
/

-- Migra configurações numéricas existentes sem remover a coluna anterior.
UPDATE ocorrencia_tipo_config
   SET codigo_funcionalidade = TRIM(TO_CHAR(id_funcionalidade))
 WHERE codigo_funcionalidade IS NULL
   AND id_funcionalidade IS NOT NULL;

COMMIT;

CREATE UNIQUE INDEX uk_ocorr_tipo_cfg_codigo ON ocorrencia_tipo_config (
    tipo_negocio,
    NVL(codigo_funcionalidade, '#'),
    NVL(id_tipo_ocorrencia, -1),
    NVL(id_assunto, -1)
);

-- Rollback somente antes de o código novo gravar identificadores não numéricos:
-- DROP INDEX uk_ocorr_tipo_cfg_codigo;
-- ALTER TABLE ocorrencia_tipo_config DROP COLUMN codigo_funcionalidade;
-- ALTER TABLE ocorrencias DROP COLUMN codigo_funcionalidade;
