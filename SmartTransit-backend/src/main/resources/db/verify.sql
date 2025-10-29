-- ===================================
-- Script de Verificación Rápida
-- SmartTransit Database
-- ===================================

\echo '=== VERIFICACIÓN DE TABLAS ==='
\echo ''

-- Verificar existencia de tablas
DO $$
DECLARE
    tables TEXT[] := ARRAY['admins', 'admin_sessions', 'drivers', 'driver_sessions', 
                           'buses', 'current_bus_state', 'bus_positions', 'routes', 'stops'];
    tbl TEXT;
    exists_count INTEGER;
    missing_count INTEGER := 0;
BEGIN
    FOREACH tbl IN ARRAY tables
    LOOP
        SELECT COUNT(*) INTO exists_count
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = tbl;
        
        IF exists_count > 0 THEN
            RAISE NOTICE '✓ %', tbl;
        ELSE
            RAISE WARNING '✗ % NO EXISTE', tbl;
            missing_count := missing_count + 1;
        END IF;
    END LOOP;
    
    IF missing_count = 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '✅ Todas las tablas existen correctamente';
    ELSE
        RAISE WARNING '';
        RAISE WARNING '⚠️  Faltan % tablas. Ejecuta init.sql', missing_count;
    END IF;
END $$;

\echo ''
\echo '=== CONTEO DE REGISTROS ==='
\echo ''

-- Verificar datos
SELECT 
    'Admins' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) > 0 THEN '✓' ELSE '✗' END as status
FROM admins
UNION ALL
SELECT 
    'Admin Sessions' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM admin_sessions
UNION ALL
SELECT 
    'Drivers' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) > 0 THEN '✓' ELSE '✗' END as status
FROM drivers
UNION ALL
SELECT 
    'Driver Sessions' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM driver_sessions
UNION ALL
SELECT 
    'Buses' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) > 0 THEN '✓' ELSE '✗' END as status
FROM buses
UNION ALL
SELECT 
    'Routes' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM routes
UNION ALL
SELECT 
    'Stops' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM stops
UNION ALL
SELECT 
    'Bus Positions' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM bus_positions
UNION ALL
SELECT 
    'Current Bus State' as tabla, 
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 0 THEN '✓' ELSE '✗' END as status
FROM current_bus_state;

\echo ''
\echo '=== DETALLES DE DATOS DE PRUEBA ==='
\echo ''

-- Admin users
\echo 'ADMINISTRADORES:'
SELECT 
    username, 
    full_name as nombre,
    role,
    active as activo
FROM admins
ORDER BY created_at;

\echo ''
\echo 'CONDUCTORES:'
SELECT 
    username,
    display_name as nombre,
    bus_id,
    route_id,
    active as activo
FROM drivers
ORDER BY created_at;

\echo ''
\echo 'BUSES:'
SELECT 
    id,
    route_id,
    plate as placa
FROM buses
ORDER BY id;

\echo ''
\echo 'RUTAS IMPORTADAS:'
SELECT 
    id,
    name as nombre,
    color,
    active as activo,
    created_at as fecha_creacion
FROM routes
WHERE active = true
ORDER BY id;

\echo ''
\echo 'PARADAS POR RUTA:'
SELECT 
    r.id as ruta_id,
    r.name as ruta_nombre,
    COUNT(s.id) as num_paradas
FROM routes r
LEFT JOIN stops s ON r.id = s.route_id
WHERE r.active = true
GROUP BY r.id, r.name
ORDER BY r.id;

\echo ''
\echo '=== VERIFICACIÓN COMPLETADA ==='

