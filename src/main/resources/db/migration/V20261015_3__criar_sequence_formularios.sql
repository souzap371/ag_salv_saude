-- Cria a sequência interna de FORMULARIOS somente quando ainda não existir.
-- O primeiro valor considera os registros existentes para evitar colisões.
DECLARE
    v_tabela_existe NUMBER;
    v_sequence_existe NUMBER;
    v_proximo_id NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_tabela_existe
      FROM USER_TABLES
     WHERE TABLE_NAME = 'FORMULARIOS';

    SELECT COUNT(*)
      INTO v_sequence_existe
      FROM USER_SEQUENCES
     WHERE SEQUENCE_NAME = 'SEQ_FORMULARIOS';

    IF v_tabela_existe > 0 AND v_sequence_existe = 0 THEN
        EXECUTE IMMEDIATE
            'SELECT NVL(MAX(ID), 0) + 1 FROM FORMULARIOS'
            INTO v_proximo_id;

        EXECUTE IMMEDIATE
            'CREATE SEQUENCE SEQ_FORMULARIOS '
            || 'START WITH ' || TO_CHAR(v_proximo_id, 'FM99999999999999999990') || ' '
            || 'INCREMENT BY 1 NOCACHE NOCYCLE';
    END IF;
END;
/
