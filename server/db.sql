-- Criar banco de dados
CREATE DATABASE petsafe
    WITH OWNER = postgres
        ENCODING = 'UTF8'
        LOCALE_PROVIDER = 'libc'
        CONNECTION LIMIT = -1
        IS_TEMPLATE = FALSE;

\c petsafe;

-- ===============================
-- Tabela de Usuários
-- ===============================
CREATE TABLE public.users (
    id BIGSERIAL PRIMARY KEY,
    email  VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    phone VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===============================
-- Tabela de Pets
-- ===============================
CREATE TABLE public.pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    species VARCHAR(40),
    breed VARCHAR(80),
    dob DATE,
    microchip_id VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id)
        REFERENCES public.users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ===============================
-- Tabela de Dispositivos
-- ===============================
CREATE TABLE public.devices (
    id BIGSERIAL PRIMARY KEY,
    serial_number VARCHAR(64) NOT NULL UNIQUE,
    imei VARCHAR(64),
    model VARCHAR(80),
    firmware VARCHAR(50),
    owner_id BIGINT NOT NULL,
    pet_id BIGINT,
    connectivity VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_comm TIMESTAMPTZ,
    last_latitude NUMERIC(9, 6),
    last_longitude NUMERIC(9, 6),
    battery_percent REAL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_devices_owner FOREIGN KEY (owner_id)
        REFERENCES public.users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_devices_pet FOREIGN KEY (pet_id)
        REFERENCES public.pets (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);


-- ===============================
-- Tabela de Histórico da Bateria de Dispositivos
-- ===============================
CREATE TABLE public.battery_history (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    ts TIMESTAMPTZ,
    battery_percent REAL,
    CONSTRAINT fk_battery_history_device FOREIGN KEY (device_id)
        REFERENCES public.devices (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =================================
-- Tabela de Eventos de Dispositivos
-- =================================
CREATE TABLE public.device_events (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    event_timestamp TIMESTAMPTZ NOT NULL,
    metadata JSON,
    CONSTRAINT fk_device_events_device FOREIGN KEY (device_id)
        REFERENCES public.devices (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =================================
-- Tabela de Geofences
-- =================================
CREATE TABLE public.geofences (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100),
    -- type ENUM(...) NOT NULL,
    geometry circle NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_geofence_user FOREIGN KEY (owner_id)
        REFERENCES public.users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =================================
-- Tabela Geofence Device
-- =================================
CREATE TABLE public.geofence_device (
    geofence_id BIGINT NOT NULL,
    device_id   BIGINT NOT NULL,
    CONSTRAINT fk_geofences_geofence FOREIGN KEY (geofence_id)
        REFERENCES public.geofences (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_geofences_device FOREIGN KEY (device_id)
        REFERENCES public.devices (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT pk_geofence_device
        PRIMARY KEY (geofence_id, device_id)
);

-- =================================
-- Tabela de Alertas
-- =================================
CREATE TABLE public.alerts (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    geofence_id BIGINT NULL,
    event_id BIGINT NULL,
    alert_type VARCHAR(80) NOT NULL,
    alert_timestamp TIMESTAMPTZ NOT NULL,
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_alerts_device FOREIGN KEY (device_id)
        REFERENCES public.devices (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_alerts_geofence FOREIGN KEY (geofence_id)
        REFERENCES public.geofences (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- =================================
-- Tabela de Notificações
-- =================================
CREATE TABLE public.notifications (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    -- method ENUM(...) NOT NULL,
    -- status ENUM(...),
    sent_at TIMESTAMPTZ,
    attempts INTEGER,
    CONSTRAINT fk_notification_alert FOREIGN KEY (alert_id)
        REFERENCES public.alerts (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ===============================
-- Tabela de Localizações
-- ===============================
CREATE TABLE public.locations (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    accuracy REAL,
    speed REAL,
    heading REAL,
    updated_at BIGINT NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_locations_device FOREIGN KEY (device_id)
        REFERENCES public.devices (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- =================================
-- Tabela de Refresh Tokens
-- =================================
CREATE TABLE public.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255),
    revoked BOOLEAN,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_geofence_user FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
