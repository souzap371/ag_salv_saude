-- Atualiza registros que ainda não têm número de guia
BEGIN 
  FOR x IN (SELECT id FROM autorizacao_solicitada WHERE nnumeguia IS NULL) LOOP
    UPDATE autorizacao_solicitada 
       SET nnumeguia = solus.seqguia.NEXTVAL,
           nnumeprot = solus.seq_crmprot.NEXTVAL,
           nnumeatend = solus.seq_crmatend.NEXTVAL
     WHERE id = x.id;
  END LOOP;
  COMMIT;
END;
/
