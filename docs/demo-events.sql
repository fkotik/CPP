-- Демо-партия DEMO-EVENTS со всеми типами событий. Запускать один раз.
DO $$
DECLARE
    v_batch INTEGER;
    v_house INTEGER := (SELECT min(house_id) FROM poultry_house);
    v_tech  INTEGER := (SELECT user_id FROM app_user WHERE login = 'tech');
    v_vet   INTEGER := (SELECT user_id FROM app_user WHERE login = 'vet');
    v_otk   INTEGER := (SELECT user_id FROM app_user WHERE login = 'otk');
    v_plant DATE   := current_date - 42;
BEGIN
    INSERT INTO batch (batch_number, house_id, planting_date, initial_count, source_supplier,
                       cross_breed, planned_weight, planned_slaughter_date, status, created_by)
    VALUES ('DEMO-EVENTS', v_house, v_plant, 20000, 'Supplier',
            'Ross 308', 2900, current_date, 'SLAUGHTER', v_tech)
    RETURNING batch_id INTO v_batch;

    -- 241 показатель температуры (раз в час за 10 суток): чтобы проверить пагинацию
    INSERT INTO indicator (batch_id, indicator_type, value, unit, measured_at, source_system)
    SELECT v_batch, 'TEMPERATURE', round((22 + random() * 6)::numeric, 1), 'C', t, 'SITUATION_CENTER'
    FROM generate_series(now() - interval '10 days', now(), interval '1 hour') AS t;

    -- расход корма: по одному показателю в сутки, 42 штуки (пригодится для FCR)
    INSERT INTO indicator (batch_id, indicator_type, value, unit, measured_at, source_system)
    SELECT v_batch, 'FEED_CONSUMPTION', 650 + d * 70, 'kg', (v_plant + d)::timestamptz, 'SITUATION_CENTER'
    FROM generate_series(1, 42) AS d;

    INSERT INTO incident (batch_id, incident_type, description, occurred_at, resolved_at, status, source_system)
    VALUES
     (v_batch, 'Temperature deviation', 'Temperature above norm',
      now() - interval '5 days', now() - interval '5 days' + interval '3 hours', 'RESOLVED', 'SITUATION_CENTER'),
     (v_batch, 'Ventilation failure', 'Fan 2 stopped',
      now() - interval '1 day', NULL, 'OPEN', 'SITUATION_CENTER');

    INSERT INTO vet_treatment (batch_id, treatment_date, treatment_type, drug_name, dosage, method,
                               responsible_user_id, notes)
    VALUES
     (v_batch, v_plant + 7,  'VACCINATION', 'Vaccine X', '1 dose', 'drinking water', v_vet, NULL),
     (v_batch, v_plant + 20, 'EXAMINATION', NULL, NULL, NULL, v_vet, 'Routine check');

    INSERT INTO control_weighing (batch_id, weighing_date, age_days, sample_size, actual_avg_weight,
                                  planned_avg_weight, deviation_absolute, deviation_percent, responsible_user_id)
    VALUES
     (v_batch, v_plant + 14, 14, 100,  470,  480,  -10, -2.08, v_tech),
     (v_batch, v_plant + 28, 28, 100, 1450, 1590, -140, -8.81, v_tech),
     (v_batch, v_plant + 35, 35, 100, 2100, 2290, -190, -8.30, v_tech);

    INSERT INTO slaughter_result (batch_id, slaughter_date, actual_count, final_live_weight,
                                  product_output_weight, product_yield_percent, survival_rate_percent,
                                  quality_result, defect_count, responsible_user_id)
    VALUES (v_batch, current_date, 19200, 55680, 41000, 73.64, 96.00, 'WITH_REMARKS', 12, v_otk);
END $$;