-- Migração automática Oracle.
-- Converte BENEFICIARIOS.ID para VARCHAR2 usando CHAVE_UNICA, ajusta a FK
-- de OCORRENCIAS e remove a coluna CHAVE_UNICA duplicada.
DECLARE
    v_tabela_beneficiarios NUMBER;
    v_tabela_ocorrencias NUMBER;
    v_coluna_chave NUMBER;
    v_coluna_fk NUMBER;
    v_coluna_fk_nova NUMBER;
    v_coluna_id_nova NUMBER;
    v_tipo_id VARCHAR2(30);
    v_invalidos NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_tabela_beneficiarios
      FROM USER_TABLES
     WHERE TABLE_NAME = 'BENEFICIARIOS';

    IF v_tabela_beneficiarios > 0 THEN
        SELECT DATA_TYPE
          INTO v_tipo_id
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = 'BENEFICIARIOS'
           AND COLUMN_NAME = 'ID';

        SELECT COUNT(*)
          INTO v_coluna_chave
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = 'BENEFICIARIOS'
           AND COLUMN_NAME = 'CHAVE_UNICA';

        IF v_tipo_id = 'NUMBER' THEN
            IF v_coluna_chave = 0 THEN
                RAISE_APPLICATION_ERROR(
                    -20001,
                    'BENEFICIARIOS.ID ainda e NUMBER, mas CHAVE_UNICA nao existe para migracao.');
            END IF;

            EXECUTE IMMEDIATE
                'SELECT COUNT(*) FROM BENEFICIARIOS '
                || 'WHERE CHAVE_UNICA IS NULL OR TRIM(CHAVE_UNICA) IS NULL'
                INTO v_invalidos;
            IF v_invalidos > 0 THEN
                RAISE_APPLICATION_ERROR(
                    -20002,
                    'Existem beneficiarios sem CHAVE_UNICA; migracao cancelada.');
            END IF;

            EXECUTE IMMEDIATE
                'SELECT COUNT(*) FROM ('
                || 'SELECT TRIM(CHAVE_UNICA) FROM BENEFICIARIOS '
                || 'GROUP BY TRIM(CHAVE_UNICA) HAVING COUNT(*) > 1)'
                INTO v_invalidos;
            IF v_invalidos > 0 THEN
                RAISE_APPLICATION_ERROR(
                    -20003,
                    'Existem CHAVES_UNICAS duplicadas; migracao cancelada.');
            END IF;

            SELECT COUNT(*)
              INTO v_coluna_id_nova
              FROM USER_TAB_COLUMNS
             WHERE TABLE_NAME = 'BENEFICIARIOS'
               AND COLUMN_NAME = 'ID_CHAVE_UNICA';
            IF v_coluna_id_nova = 0 THEN
                EXECUTE IMMEDIATE
                    'ALTER TABLE BENEFICIARIOS ADD (ID_CHAVE_UNICA VARCHAR2(255))';
            END IF;
            EXECUTE IMMEDIATE
                'UPDATE BENEFICIARIOS SET ID_CHAVE_UNICA = TRIM(CHAVE_UNICA)';

            SELECT COUNT(*)
              INTO v_tabela_ocorrencias
              FROM USER_TABLES
             WHERE TABLE_NAME = 'OCORRENCIAS';

            IF v_tabela_ocorrencias > 0 THEN
                SELECT COUNT(*)
                  INTO v_coluna_fk
                  FROM USER_TAB_COLUMNS
                 WHERE TABLE_NAME = 'OCORRENCIAS'
                   AND COLUMN_NAME = 'BENEFICIARIO_ID';
                SELECT COUNT(*)
                  INTO v_coluna_fk_nova
                  FROM USER_TAB_COLUMNS
                 WHERE TABLE_NAME = 'OCORRENCIAS'
                   AND COLUMN_NAME = 'BENEFICIARIO_ID_NOVO';

                IF v_coluna_fk_nova = 0 THEN
                    EXECUTE IMMEDIATE
                        'ALTER TABLE OCORRENCIAS ADD (BENEFICIARIO_ID_NOVO VARCHAR2(255))';
                END IF;
                IF v_coluna_fk > 0 THEN
                    EXECUTE IMMEDIATE
                        'UPDATE OCORRENCIAS O SET O.BENEFICIARIO_ID_NOVO = ('
                        || 'SELECT B.ID_CHAVE_UNICA FROM BENEFICIARIOS B '
                        || 'WHERE B.ID = O.BENEFICIARIO_ID)';
                END IF;
            END IF;

            EXECUTE IMMEDIATE 'ALTER TABLE BENEFICIARIOS DROP PRIMARY KEY CASCADE';

            IF v_tabela_ocorrencias > 0 AND v_coluna_fk > 0 THEN
                EXECUTE IMMEDIATE 'ALTER TABLE OCORRENCIAS DROP COLUMN BENEFICIARIO_ID';
            END IF;
            EXECUTE IMMEDIATE 'ALTER TABLE BENEFICIARIOS DROP COLUMN ID';
            EXECUTE IMMEDIATE
                'ALTER TABLE BENEFICIARIOS RENAME COLUMN ID_CHAVE_UNICA TO ID';
            EXECUTE IMMEDIATE
                'ALTER TABLE BENEFICIARIOS ADD CONSTRAINT PK_BENEFICIARIOS PRIMARY KEY (ID)';

            IF v_tabela_ocorrencias > 0 THEN
                EXECUTE IMMEDIATE
                    'ALTER TABLE OCORRENCIAS RENAME COLUMN BENEFICIARIO_ID_NOVO TO BENEFICIARIO_ID';
                EXECUTE IMMEDIATE
                    'ALTER TABLE OCORRENCIAS ADD CONSTRAINT FK_OCORRENCIAS_BENEFICIARIO '
                    || 'FOREIGN KEY (BENEFICIARIO_ID) REFERENCES BENEFICIARIOS(ID)';
            END IF;
        END IF;

        SELECT COUNT(*)
          INTO v_coluna_chave
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = 'BENEFICIARIOS'
           AND COLUMN_NAME = 'CHAVE_UNICA';
        IF v_coluna_chave > 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE BENEFICIARIOS DROP COLUMN CHAVE_UNICA';
        END IF;
    END IF;
END;
/
