DO $$
DECLARE v INTEGER := (SELECT batch_id FROM batch WHERE batch_number = 'DEMO-EVENTS');
BEGIN
    DELETE FROM indicator          WHERE batch_id = v;
    DELETE FROM incident           WHERE batch_id = v;
    DELETE FROM vet_treatment      WHERE batch_id = v;
    DELETE FROM control_weighing   WHERE batch_id = v;
    DELETE FROM slaughter_result   WHERE batch_id = v;
    DELETE FROM batch              WHERE batch_id = v;
END $$;