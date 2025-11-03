-- Adiciona colunas
ALTER TABLE autorizacao_solicitada ADD nnumeguia NUMBER;
ALTER TABLE autorizacao_solicitada ADD nnumeprot NUMBER;
ALTER TABLE autorizacao_solicitada ADD nnumeatend NUMBER;
ALTER TABLE autorizacao_arquivo ADD nnumeanexo NUMBER;
ALTER TABLE autorizacao_arquivo ADD nnumeimagem NUMBER;
ALTER TABLE autorizacao_arquivo ADD nnumearqui NUMBER;

-- Trigger: autorizacao_solicitada
CREATE OR REPLACE TRIGGER trg_autorizacao_solicitada
BEFORE INSERT ON autorizacao_solicitada
FOR EACH ROW
BEGIN
   IF :new.nnumeguia IS NULL THEN
      SELECT solus.seqguia.NEXTVAL INTO :new.nnumeguia FROM dual;
   END IF;
   IF :new.numerocontrato IS NULL THEN
      :new.numerocontrato := TO_CHAR(REGEXP_SUBSTR(:new.raw_callback_json, '"numerocontrato":"([^"]+)"', 1, 1, NULL, 1));
   END IF;
   IF :new.chaveunica IS NULL THEN
      :new.chaveunica := TO_CHAR(REGEXP_SUBSTR(:new.raw_callback_json, '"chaveunica":"([^"]+)"', 1, 1, NULL, 1));
   END IF;
   IF :new.nnumeprot IS NULL THEN
      SELECT solus.seq_crmprot.NEXTVAL INTO :new.nnumeprot FROM dual;
   END IF;
   IF :new.nnumeatend IS NULL THEN
      SELECT solus.seq_crmatend.NEXTVAL INTO :new.nnumeatend FROM dual;
   END IF;
END;
/

-- Trigger: autorizacao_arquivo
CREATE OR REPLACE TRIGGER trg_autorizacao_arquivo
BEFORE INSERT ON autorizacao_arquivo
FOR EACH ROW
BEGIN
   IF :new.nnumeanexo IS NULL THEN
      SELECT solus.seqanexo.NEXTVAL INTO :new.nnumeanexo FROM dual;
   END IF;
   IF :new.nnumeimagem IS NULL THEN
      SELECT solus.seq_geral.NEXTVAL INTO :new.nnumeimagem FROM dual;
   END IF;
   IF :new.nnumearqui IS NULL THEN
      SELECT solus.seq_arqui.NEXTVAL INTO :new.nnumearqui FROM dual;
   END IF;
END;
/
