-- ============================================================
-- 1. USUARIOS
-- ============================================================
-- NO se inserta el usuario con id = 3 porque ya existe.

INSERT INTO usuarios
(id, nombre_completo, apellido_paterno, apellido_materno, correo,
 contrasena_hash, rol, codigo_recuperacion, intentos_recuperacion,
 requiere_recuperacion, fecha_codigo, activo)
VALUES
(1, 'Juan Carlos', 'Hernández', 'López',
 'juan.hernandez@empresa.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'administrador', NULL, 0, FALSE, NULL, TRUE),

(2, 'María Fernanda', 'Gómez', 'Ramírez',
 'maria.gomez@empresa.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'tecnico', NULL, 0, FALSE, NULL, TRUE),

(4, 'Carlos Alberto', 'Martínez', 'Sánchez',
 'carlos.martinez@empresa.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'tecnico', NULL, 0, FALSE, NULL, TRUE),

(5, 'Ana Sofía', 'Torres', 'Mendoza',
 'ana.torres@empresa.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'consulta', NULL, 0, FALSE, NULL, TRUE),

(6, 'Luis Eduardo', 'Ramírez', 'Vargas',
 'luis.ramirez@empresa.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'consulta', NULL, 0, FALSE, NULL, TRUE);


-- ============================================================
-- 2. SISTEMAS OPERATIVOS
-- ============================================================

INSERT INTO sistemas_operativos
(id, tipo, nombre, version_actual)
VALUES
(1, 'Desktop', 'Windows', '11 Pro'),
(2, 'Desktop', 'Windows', '10 Pro'),
(3, 'Linux', 'Ubuntu', '24.04 LTS'),
(4, 'Linux', 'Ubuntu', '22.04 LTS'),
(5, 'Desktop', 'Windows Server', '2022');


-- ============================================================
-- 3. PROGRAMAS
-- ============================================================

INSERT INTO programas
(id, tipo, nombre, version_actual)
VALUES
(1, 'ofimatica', 'Microsoft Office', '2024'),
(2, 'ofimatica', 'Google Chrome', '138.0'),
(3, 'ofimatica', 'Mozilla Firefox', '141.0'),
(4, 'seguridad', 'Windows Defender', '4.18'),
(5, 'seguridad', 'Malwarebytes', '5.3'),
(6, 'utilidad', '7-Zip', '24.09'),
(7, 'utilidad', 'VLC Media Player', '3.0.21'),
(8, 'sistema', 'Microsoft Edge', '138.0'),
(9, 'otro', 'Visual Studio Code', '1.103'),
(10, 'ofimatica', 'Adobe Acrobat Reader', '25.001');


-- ============================================================
-- 4. EQUIPOS
-- ============================================================

INSERT INTO equipos
(id, modelo, lugar, almacenamiento, memoria_ram, procesador,
 anio_creacion, estado, id_sistema_operativo,
 id_usuario_responsable)
VALUES
(1, 'Dell OptiPlex 7090', 'Oficina Administrativa',
 '512GB SSD', '16GB', 'Intel Core i7-11700',
 2021, 'activo', 1, 1),

(2, 'HP ProDesk 600 G6', 'Recursos Humanos',
 '512GB SSD', '16GB', 'Intel Core i5-10500',
 2021, 'activo', 1, 2),

(3, 'Lenovo ThinkCentre M720', 'Contabilidad',
 '1TB SSD', '16GB', 'Intel Core i5-8500',
 2020, 'activo', 2, 4),

(4, 'Dell Latitude 5520', 'Dirección',
 '512GB SSD', '16GB', 'Intel Core i5-1145G7',
 2022, 'activo', 1, 1),

(5, 'HP EliteBook 840 G7', 'Soporte Técnico',
 '512GB SSD', '32GB', 'Intel Core i7-10610U',
 2021, 'en_mantenimiento', 1, 4),

(6, 'Lenovo ThinkPad E14', 'Ventas',
 '512GB SSD', '16GB', 'Intel Core i5-1135G7',
 2022, 'activo', 1, 5),

(7, 'Dell Precision 3650', 'Desarrollo',
 '1TB SSD', '32GB', 'Intel Xeon W-1350',
 2022, 'activo', 3, 4),

(8, 'HP ProLiant ML350', 'Servidor',
 '2TB SSD', '64GB', 'Intel Xeon Silver 4210',
 2020, 'activo', 5, 1),

(9, 'Acer Aspire 5', 'Recepción',
 '512GB SSD', '8GB', 'Intel Core i5-1135G7',
 2021, 'activo', 1, 2),

(10, 'ASUS ExpertBook B1', 'Compras',
 '512GB SSD', '16GB', 'Intel Core i5-1235U',
 2023, 'activo', 1, 5);


-- ============================================================
-- 5. EQUIPOS_PROGRAMAS
-- ============================================================

INSERT INTO equipos_programas
(id, id_equipo, id_programa)
VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 4),
(4, 1, 6),
(5, 1, 8),

(6, 2, 1),
(7, 2, 2),
(8, 2, 4),
(9, 2, 10),

(10, 3, 1),
(11, 3, 2),
(12, 3, 4),
(13, 3, 6),

(14, 4, 1),
(15, 4, 2),
(16, 4, 4),
(17, 4, 8),
(18, 4, 10),

(19, 5, 1),
(20, 5, 2),
(21, 5, 4),
(22, 5, 5),
(23, 5, 9),

(24, 6, 1),
(25, 6, 2),
(26, 6, 4),
(27, 6, 7),

(28, 7, 2),
(29, 7, 3),
(30, 7, 4),
(31, 7, 9),
(32, 7, 6),

(33, 8, 2),
(34, 8, 4),
(35, 8, 6),

(36, 9, 1),
(37, 9, 2),
(38, 9, 4),
(39, 9, 8),

(40, 10, 1),
(41, 10, 2),
(42, 10, 4),
(43, 10, 10);


-- ============================================================
-- 6. REGISTROS DE MANTENIMIENTO
-- ============================================================

INSERT INTO registros_mantenimiento
(id, id_equipo, fecha, motivo, tipo, fecha_proxima, mantenimiento_realizado)
VALUES
(1, 1, '2026-01-15 09:00:00',
 'Limpieza interna y revisión general',
 'preventivo',
 '2026-07-15 09:00:00',
 TRUE),

(2, 2, '2026-02-10 10:30:00',
 'Actualización y limpieza del sistema',
 'preventivo',
 '2026-08-10 10:30:00',
 TRUE),

(3, 3, '2026-02-20 11:00:00',
 'Revisión de almacenamiento',
 'preventivo',
 '2026-08-20 11:00:00',
 TRUE),

(4, 4, '2026-03-05 09:30:00',
 'Limpieza física y revisión de ventilación',
 'preventivo',
 '2026-09-05 09:30:00',
 TRUE),

(5, 5, '2026-03-18 14:00:00',
 'Equipo presenta sobrecalentamiento',
 'correctivo',
 NULL,
 TRUE),

(6, 5, '2026-07-20 10:00:00',
 'Cambio de pasta térmica y revisión de ventilador',
 'correctivo',
 NULL,
 FALSE),

(7, 6, '2026-04-12 09:00:00',
 'Mantenimiento preventivo general',
 'preventivo',
 '2026-10-12 09:00:00',
 TRUE),

(8, 7, '2026-04-25 15:00:00',
 'Limpieza y optimización del sistema',
 'preventivo',
 '2026-10-25 15:00:00',
 TRUE),

(9, 8, '2026-05-03 08:00:00',
 'Revisión general del servidor',
 'preventivo',
 '2026-11-03 08:00:00',
 TRUE),

(10, 9, '2026-05-15 11:30:00',
 'Revisión de hardware',
 'correctivo',
 NULL,
 TRUE),

(11, 10, '2026-06-01 10:00:00',
 'Mantenimiento preventivo general',
 'preventivo',
 '2026-12-01 10:00:00',
 TRUE);


-- ============================================================
-- 7. REGISTROS DE ACTUALIZACIONES
-- ============================================================

INSERT INTO registros_actualizaciones
(id, id_equipo, tipo, fecha, nombre_actualizado,
 version_actual, version_actualizada)
VALUES
(1, 1, 'sistema_operativo', '2026-01-20 10:00:00',
 'Windows 11 Pro', '22H2', '23H2'),

(2, 1, 'programa', '2026-02-05 11:00:00',
 'Microsoft Office', '2021', '2024'),

(3, 1, 'programa', '2026-02-05 11:30:00',
 'Google Chrome', '136.0', '138.0'),

(4, 2, 'sistema_operativo', '2026-02-15 09:00:00',
 'Windows 11 Pro', '22H2', '23H2'),

(5, 2, 'programa', '2026-03-01 10:00:00',
 'Mozilla Firefox', '139.0', '141.0'),

(6, 3, 'programa', '2026-03-10 12:00:00',
 '7-Zip', '23.01', '24.09'),

(7, 4, 'sistema_operativo', '2026-03-20 09:00:00',
 'Windows 11 Pro', '23H2', '24H2'),

(8, 4, 'programa', '2026-04-02 10:30:00',
 'Adobe Acrobat Reader', '24.005', '25.001'),

(9, 5, 'driver', '2026-04-15 14:00:00',
 'Controlador Intel Graphics', '31.0', '32.0'),

(10, 6, 'programa', '2026-05-05 11:00:00',
 'VLC Media Player', '3.0.20', '3.0.21'),

(11, 7, 'programa', '2026-05-20 15:00:00',
 'Visual Studio Code', '1.100', '1.103'),

(12, 7, 'sistema_operativo', '2026-06-01 09:00:00',
 'Ubuntu', '22.04 LTS', '24.04 LTS'),

(13, 8, 'firmware', '2026-06-10 08:00:00',
 'Firmware del servidor', '2.1', '2.4'),

(14, 9, 'programa', '2026-06-15 10:00:00',
 'Google Chrome', '136.0', '138.0'),

(15, 10, 'sistema_operativo', '2026-07-01 09:30:00',
 'Windows 11 Pro', '23H2', '24H2');


-- ============================================================
-- 8. HISTORIAL DE REGISTROS
-- ============================================================

INSERT INTO historial_registros
(id, id_equipo, id_registro_actualizacion,
 id_registro_mantenimiento, fecha_registro)
VALUES

(1, 1, NULL, 1, '2026-01-15 09:00:00'),
(2, 1, 1, NULL, '2026-01-20 10:00:00'),
(3, 1, 2, NULL, '2026-02-05 11:00:00'),
(4, 1, 3, NULL, '2026-02-05 11:30:00'),

(5, 2, NULL, 2, '2026-02-10 10:30:00'),
(6, 2, 4, NULL, '2026-02-15 09:00:00'),
(7, 2, 5, NULL, '2026-03-01 10:00:00'),

(8, 3, NULL, 3, '2026-02-20 11:00:00'),
(9, 3, 6, NULL, '2026-03-10 12:00:00'),

(10, 4, NULL, 4, '2026-03-05 09:30:00'),
(11, 4, 7, NULL, '2026-03-20 09:00:00'),
(12, 4, 8, NULL, '2026-04-02 10:30:00'),

(13, 5, NULL, 5, '2026-03-18 14:00:00'),
(14, 5, 9, NULL, '2026-04-15 14:00:00'),
(15, 5, NULL, 6, '2026-07-20 10:00:00'),

(16, 6, NULL, 7, '2026-04-12 09:00:00'),
(17, 6, 10, NULL, '2026-05-05 11:00:00'),

(18, 7, NULL, 8, '2026-04-25 15:00:00'),
(19, 7, 11, NULL, '2026-05-20 15:00:00'),
(20, 7, 12, NULL, '2026-06-01 09:00:00'),

(21, 8, NULL, 9, '2026-05-03 08:00:00'),
(22, 8, 13, NULL, '2026-06-10 08:00:00'),

(23, 9, NULL, 10, '2026-05-15 11:30:00'),
(24, 9, 14, NULL, '2026-06-15 10:00:00'),

(25, 10, NULL, 11, '2026-06-01 10:00:00'),
(26, 10, 15, NULL, '2026-07-01 09:30:00');


-- ============================================================
-- 9. ACTUALIZAR SECUENCIAS
-- ============================================================
-- Esto evita que el BIGSERIAL intente volver a utilizar
-- alguno de los IDs que acabamos de insertar.

SELECT setval(
    pg_get_serial_sequence('usuarios', 'id'),
    GREATEST((SELECT MAX(id) FROM usuarios), 1)
);

SELECT setval(
    pg_get_serial_sequence('sistemas_operativos', 'id'),
    GREATEST((SELECT MAX(id) FROM sistemas_operativos), 1)
);

SELECT setval(
    pg_get_serial_sequence('programas', 'id'),
    GREATEST((SELECT MAX(id) FROM programas), 1)
);

SELECT setval(
    pg_get_serial_sequence('equipos', 'id'),
    GREATEST((SELECT MAX(id) FROM equipos), 1)
);

SELECT setval(
    pg_get_serial_sequence('equipos_programas', 'id'),
    GREATEST((SELECT MAX(id) FROM equipos_programas), 1)
);

SELECT setval(
    pg_get_serial_sequence('registros_mantenimiento', 'id'),
    GREATEST((SELECT MAX(id) FROM registros_mantenimiento), 1)
);

SELECT setval(
    pg_get_serial_sequence('registros_actualizaciones', 'id'),
    GREATEST((SELECT MAX(id) FROM registros_actualizaciones), 1)
);

SELECT setval(
    pg_get_serial_sequence('historial_registros', 'id'),
    GREATEST((SELECT MAX(id) FROM historial_registros), 1)
);

COMMIT;
