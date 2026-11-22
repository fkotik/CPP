-- =========================================================
-- Роли
-- =========================================================
CREATE TABLE role (
    role_id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- code: стабильный технический код роли для проверок прав в программе.
    -- (Отличие от документа: role_name на русском языке может поменяться, а код нет.)
    code        VARCHAR(30)  NOT NULL UNIQUE,
    role_name   VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- =========================================================
-- Пользователи
-- Таблица названа app_user, потому что слово "user" в PostgreSQL зарезервировано
-- и требует кавычек везде, что вызывает постоянные ошибки.
-- =========================================================
CREATE TABLE app_user (
    user_id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    surname       VARCHAR(100) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    patronymic    VARCHAR(100),
    login         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role_id       INTEGER      NOT NULL REFERENCES role (role_id),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- =========================================================
-- Птичники / цеха
-- =========================================================
CREATE TABLE poultry_house (
    house_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_name      VARCHAR(100) NOT NULL UNIQUE,
    capacity        INTEGER      NOT NULL CHECK (capacity > 0),
    characteristics TEXT
);

-- =========================================================
-- Начальные данные (справочники)
-- =========================================================
INSERT INTO role (code, role_name, description) VALUES
    ('TECHNOLOGIST', 'Технолог',      'Ведёт учёт партий, фиксирует взвешивания'),
    ('VET',          'Ветврач',       'Вносит ветеринарные обработки, осмотры, вакцинации'),
    ('QC',           'ОТК',           'Вносит результаты убоя и контроля качества'),
    ('MANAGER',      'Руководитель',  'Просматривает паспорта и формирует сводные отчёты'),
    ('ADMIN',        'Администратор', 'Управляет учётными записями и ролями');

INSERT INTO poultry_house (house_name, capacity, characteristics) VALUES
    ('Птичник № 1', 30000, 'Напольное содержание, туннельная вентиляция'),
    ('Птичник № 2', 30000, 'Напольное содержание, туннельная вентиляция'),
    ('Птичник № 3', 30000, 'Клеточное содержание'),
    ('Цех № 4',     25000, 'Напольное содержание, поперечная вентиляция');