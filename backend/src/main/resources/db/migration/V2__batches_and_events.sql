-- =====================================================================
-- V2: партии, события паспорта и справочник нормативов роста
-- Единицы: вес птицы - граммы; итоговая масса при убое - килограммы.
-- Статусы и типы хранятся английскими кодами, русские подписи - в коде.
-- =====================================================================

-- ---------- Партия ----------
CREATE TABLE batch (
    batch_id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_number           VARCHAR(20)  NOT NULL UNIQUE,
    house_id               INTEGER       NOT NULL REFERENCES poultry_house (house_id),
    planting_date          DATE         NOT NULL,
    initial_count          INTEGER      NOT NULL CHECK (initial_count > 0),
    source_supplier        VARCHAR(200) NOT NULL,
    cross_breed            VARCHAR(50)  NOT NULL
        CHECK (cross_breed IN ('Ross 308', 'Cobb 500', 'Hubbard F15')),
    planned_weight         NUMERIC(8,2) NOT NULL CHECK (planned_weight > 0),  -- граммы
    planned_slaughter_date DATE         NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'PLANTING'
        CHECK (status IN ('PLANTING','GROWING','WEIGHING','SLAUGHTER','CLOSED')),
    created_by             INTEGER       NOT NULL REFERENCES app_user (user_id),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at              TIMESTAMPTZ,
    CONSTRAINT chk_batch_dates CHECK (planned_slaughter_date >= planting_date)
);
CREATE INDEX idx_batch_house    ON batch (house_id);
CREATE INDEX idx_batch_status   ON batch (status);
CREATE INDEX idx_batch_planting ON batch (planting_date);

-- ---------- Показатели от Ситуационного центра (только чтение в паспорте) ----------
CREATE TABLE indicator (
    indicator_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id       INTEGER        NOT NULL REFERENCES batch (batch_id),
    indicator_type VARCHAR(50)   NOT NULL,   -- TEMPERATURE, HUMIDITY, FEED_CONSUMPTION ...
    value          NUMERIC(14,4) NOT NULL,
    unit           VARCHAR(20)   NOT NULL,
    measured_at    TIMESTAMPTZ   NOT NULL,   -- когда измерено
    source_system  VARCHAR(50)   NOT NULL,
    received_at    TIMESTAMPTZ   NOT NULL DEFAULT now()  -- когда получено нами
);
CREATE INDEX idx_indicator_batch_time ON indicator (batch_id, measured_at);
CREATE INDEX idx_indicator_batch_type ON indicator (batch_id, indicator_type);

-- ---------- Инциденты от Ситуационного центра ----------
CREATE TABLE incident (
    incident_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id      INTEGER       NOT NULL REFERENCES batch (batch_id),
    incident_type VARCHAR(100) NOT NULL,
    description   TEXT,
    occurred_at   TIMESTAMPTZ  NOT NULL,
    resolved_at   TIMESTAMPTZ,
    status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED')),
    source_system VARCHAR(50)  NOT NULL,
    received_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_incident_batch_time ON incident (batch_id, occurred_at);

-- ---------- Ветеринарные обработки (ручной ввод) ----------
CREATE TABLE vet_treatment (
    treatment_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id            INTEGER       NOT NULL REFERENCES batch (batch_id),
    treatment_date      DATE         NOT NULL,
    treatment_type      VARCHAR(20)  NOT NULL
        CHECK (treatment_type IN ('VACCINATION','TREATMENT','EXAMINATION','DIAGNOSTICS')),
    drug_name           VARCHAR(200),
    dosage              VARCHAR(100),
    method              VARCHAR(100),
    responsible_user_id INTEGER       NOT NULL REFERENCES app_user (user_id),
    notes               TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Для вакцинации и лечения препарат и дозировка обязательны (защита на уровне БД)
    CONSTRAINT chk_vet_drug_required CHECK (
        treatment_type NOT IN ('VACCINATION','TREATMENT')
        OR (drug_name IS NOT NULL AND btrim(drug_name) <> ''
            AND dosage IS NOT NULL AND btrim(dosage) <> '')
    )
);
CREATE INDEX idx_vet_batch_date ON vet_treatment (batch_id, treatment_date);

-- ---------- Контрольные взвешивания ----------
CREATE TABLE control_weighing (
    weighing_id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id            INTEGER       NOT NULL REFERENCES batch (batch_id),
    weighing_date       DATE         NOT NULL,
    age_days            INTEGER      NOT NULL CHECK (age_days >= 0),
    sample_size         INTEGER      NOT NULL CHECK (sample_size > 0),
    actual_avg_weight   NUMERIC(8,2) NOT NULL CHECK (actual_avg_weight > 0),  -- г
    planned_avg_weight  NUMERIC(8,2) NOT NULL,                                -- г
    deviation_absolute  NUMERIC(8,2) NOT NULL,                                -- г
    deviation_percent   NUMERIC(6,2) NOT NULL,                                -- %
    responsible_user_id INTEGER       NOT NULL REFERENCES app_user (user_id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_weighing_batch_date ON control_weighing (batch_id, weighing_date);

-- ---------- Результат убоя (одна запись на партию) ----------
CREATE TABLE slaughter_result (
    slaughter_result_id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id               INTEGER        NOT NULL UNIQUE REFERENCES batch (batch_id),
    slaughter_date         DATE          NOT NULL,
    actual_count           INTEGER       NOT NULL CHECK (actual_count >= 0),
    final_live_weight      NUMERIC(12,2) NOT NULL CHECK (final_live_weight > 0),      -- кг
    product_output_weight  NUMERIC(12,2) NOT NULL CHECK (product_output_weight >= 0), -- кг
    product_yield_percent  NUMERIC(5,2)  NOT NULL,
    survival_rate_percent  NUMERIC(5,2)  NOT NULL,
    quality_result         VARCHAR(20)   NOT NULL
        CHECK (quality_result IN ('COMPLIANT','WITH_REMARKS','REJECT')),
    defect_count           INTEGER CHECK (defect_count >= 0),   -- необязательное
    responsible_user_id    INTEGER        NOT NULL REFERENCES app_user (user_id),
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_slaughter_output CHECK (product_output_weight <= final_live_weight)
);

-- ---------- Справочник нормативов роста ----------
-- Хранятся ОРИЕНТИРОВОЧНЫЕ точки раз в неделю; между ними сервис интерполирует
-- линейно. Это учебные значения, не официальные таблицы производителей.
CREATE TABLE growth_norm (
    growth_norm_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cross_breed    VARCHAR(50)  NOT NULL
        CHECK (cross_breed IN ('Ross 308', 'Cobb 500', 'Hubbard F15')),
    age_days       INTEGER      NOT NULL CHECK (age_days >= 0),
    avg_weight_g   NUMERIC(8,2) NOT NULL CHECK (avg_weight_g > 0),
    CONSTRAINT uq_growth_norm UNIQUE (cross_breed, age_days)
);

INSERT INTO growth_norm (cross_breed, age_days, avg_weight_g) VALUES
 ('Ross 308',    0,   42), ('Ross 308',    7,  190), ('Ross 308',   14,  480),
 ('Ross 308',   21,  960), ('Ross 308',   28, 1590), ('Ross 308',   35, 2290),
 ('Ross 308',   42, 2950), ('Ross 308',   49, 3560), ('Ross 308',   56, 4100),

 ('Cobb 500',    0,   42), ('Cobb 500',    7,  185), ('Cobb 500',   14,  470),
 ('Cobb 500',   21,  930), ('Cobb 500',   28, 1550), ('Cobb 500',   35, 2230),
 ('Cobb 500',   42, 2880), ('Cobb 500',   49, 3450), ('Cobb 500',   56, 3950),

 ('Hubbard F15', 0,   42), ('Hubbard F15', 7,  175), ('Hubbard F15',14,  445),
 ('Hubbard F15',21,  880), ('Hubbard F15',28, 1480), ('Hubbard F15',35, 2150),
 ('Hubbard F15',42, 2800), ('Hubbard F15',49, 3380), ('Hubbard F15',56, 3900);