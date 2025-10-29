-- ===================================
-- SmartTransit Database Schema
-- ===================================

-- Tabla de administradores
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(80) UNIQUE NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(200),
    role VARCHAR(50),
    email VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE,
    last_login TIMESTAMP WITH TIME ZONE
);

-- Tabla de sesiones de administradores
CREATE TABLE IF NOT EXISTS admin_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL,
    token VARCHAR(200) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Tabla de conductores
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(80) UNIQUE NOT NULL,
    display_name VARCHAR(120),
    password_hash VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT true,
    bus_id VARCHAR(60),
    route_id VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE
);

-- Tabla de sesiones de conductores
CREATE TABLE IF NOT EXISTS driver_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    token VARCHAR(200) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Tabla de buses
CREATE TABLE IF NOT EXISTS buses (
    id VARCHAR(60) PRIMARY KEY,
    route_id VARCHAR(20),
    plate VARCHAR(60)
);

-- Tabla de posiciones actuales de buses
CREATE TABLE IF NOT EXISTS current_bus_state (
    bus_id VARCHAR(60) PRIMARY KEY,
    route_id VARCHAR(20),
    route_name VARCHAR(120),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    bearing DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    status VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE,
    progress INTEGER,
    occupancy_level VARCHAR(50),
    next_stop_id VARCHAR(60),
    next_stop_name VARCHAR(120),
    estimated_arrival_time TIMESTAMP WITH TIME ZONE,
    distance_to_destination INTEGER,
    passenger_count INTEGER,
    delay INTEGER
);

-- Tabla de historial de posiciones de buses
CREATE TABLE IF NOT EXISTS bus_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bus_id VARCHAR(60) NOT NULL,
    route_id VARCHAR(20),
    route_name VARCHAR(120),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    bearing DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    status VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE,
    progress INTEGER,
    occupancy_level VARCHAR(50),
    next_stop_id VARCHAR(60),
    next_stop_name VARCHAR(120),
    estimated_arrival_time TIMESTAMP WITH TIME ZONE,
    distance_to_destination INTEGER,
    passenger_count INTEGER,
    delay INTEGER
);

-- Índice para mejorar consultas de historial
CREATE INDEX IF NOT EXISTS idx_bus_positions_bus_time ON bus_positions(bus_id, timestamp);

-- Tabla de rutas
CREATE TABLE IF NOT EXISTS routes (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    color VARCHAR(20),
    polyline_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN DEFAULT true
);

-- Tabla de paradas
CREATE TABLE IF NOT EXISTS stops (
    id VARCHAR(60) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    route_id VARCHAR(20),
    stop_order INTEGER
);

-- ===================================
-- Índices adicionales para mejorar rendimiento
-- ===================================
CREATE INDEX IF NOT EXISTS idx_stops_route ON stops(route_id, stop_order);
CREATE INDEX IF NOT EXISTS idx_admin_sessions_token ON admin_sessions(token) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_driver_sessions_token ON driver_sessions(token) WHERE active = true;

-- ===================================
-- Comentarios de tablas
-- ===================================
COMMENT ON TABLE admins IS 'Usuarios administradores del sistema';
COMMENT ON TABLE admin_sessions IS 'Sesiones activas de administradores';
COMMENT ON TABLE drivers IS 'Conductores de buses';
COMMENT ON TABLE driver_sessions IS 'Sesiones activas de conductores';
COMMENT ON TABLE buses IS 'Flota de buses del sistema';
COMMENT ON TABLE current_bus_state IS 'Estado actual en tiempo real de cada bus';
COMMENT ON TABLE bus_positions IS 'Historial de posiciones de buses';
COMMENT ON TABLE routes IS 'Rutas de buses con su geometría';
COMMENT ON TABLE stops IS 'Paradas asociadas a las rutas';

