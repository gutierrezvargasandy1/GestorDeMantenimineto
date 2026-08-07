-- =====================================================================
-- ESQUEMA: Sistema de Gestión de Equipos, Mantenimientos y Actualizaciones
-- Motor: PostgreSQL 14+
-- Script completo, listo para ejecutar de un solo golpe (idempotente)
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- Limpieza previa (permite re-ejecutar el script sin errores)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS historial_registros            CASCADE;
DROP TABLE IF EXISTS registros_actualizaciones       CASCADE;
DROP TABLE IF EXISTS registros_mantenimiento         CASCADE;
DROP TABLE IF EXISTS equipos_programas               CASCADE;
DROP TABLE IF EXISTS programas                       CASCADE;
DROP TABLE IF EXISTS equipos                         CASCADE;
DROP TABLE IF EXISTS sistemas_operativos             CASCADE;
DROP TABLE IF EXISTS usuarios                        CASCADE;

DROP TYPE IF EXISTS rol_usuario;
DROP TYPE IF EXISTS estado_equipo;
DROP TYPE IF EXISTS tipo_programa;
DROP TYPE IF EXISTS tipo_actualizacion;
DROP TYPE IF EXISTS tipo_mantenimiento;

DROP FUNCTION IF EXISTS fn_actualizar_fecha() CASCADE;

-- ---------------------------------------------------------------------
-- Tipos enumerados (catálogos controlados en vez de VARCHAR libres)
-- ---------------------------------------------------------------------
CREATE TYPE rol_usuario        AS ENUM ('administrador', 'tecnico', 'consulta');
CREATE TYPE estado_equipo      AS ENUM ('activo', 'inactivo', 'en_mantenimiento', 'de_baja');
CREATE TYPE tipo_programa      AS ENUM ('sistema', 'ofimatica', 'seguridad', 'utilidad', 'otro');
CREATE TYPE tipo_actualizacion AS ENUM ('sistema_operativo', 'programa', 'firmware', 'driver');
CREATE TYPE tipo_mantenimiento AS ENUM ('preventivo', 'correctivo');

-- =====================================================================
-- 1. USUARIOS
-- =====================================================================
CREATE TABLE usuarios (
    id                      BIGSERIAL PRIMARY KEY,
    nombre_completo         VARCHAR(150)  NOT NULL,
    apellido_paterno        VARCHAR(100)  NOT NULL,
    apellido_materno        VARCHAR(100),
    correo                  VARCHAR(150)  NOT NULL UNIQUE,
    contrasena_hash         VARCHAR(255)  NOT NULL,          -- nunca texto plano
    rol                     rol_usuario   NOT NULL DEFAULT 'consulta',
    codigo_recuperacion     VARCHAR(10),
    intentos_recuperacion   INTEGER       NOT NULL DEFAULT 0,
    requiere_recuperacion   BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_codigo            TIMESTAMP,
    fecha_creacion          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo                  BOOLEAN       NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE usuarios IS 'Usuarios del sistema (antes "Usuario")';
COMMENT ON COLUMN usuarios.contrasena_hash IS 'Almacenar SIEMPRE con hash (bcrypt/argon2), nunca en claro';

-- =====================================================================
-- 2. SISTEMAS OPERATIVOS (catálogo)
-- =====================================================================
CREATE TABLE sistemas_operativos (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(50),
    nombre          VARCHAR(100)  NOT NULL,
    version_actual  VARCHAR(50)   NOT NULL,
    UNIQUE (nombre, version_actual)
);

-- =====================================================================
-- 3. PROGRAMAS (catálogo)
-- =====================================================================
CREATE TABLE programas (
    id              BIGSERIAL PRIMARY KEY,
    tipo            tipo_programa NOT NULL DEFAULT 'otro',
    nombre          VARCHAR(100)  NOT NULL,
    version_actual  VARCHAR(50)   NOT NULL,
    UNIQUE (nombre, version_actual)
);

-- =====================================================================
-- 4. EQUIPOS
-- =====================================================================
CREATE TABLE equipos (
    id                      BIGSERIAL PRIMARY KEY,
    modelo                  VARCHAR(100)  NOT NULL,
    lugar                   VARCHAR(150),
    almacenamiento          VARCHAR(50),      -- ej. "512GB SSD"
    memoria_ram             VARCHAR(50),      -- ej. "16GB"
    procesador              VARCHAR(100),
    anio_creacion           SMALLINT,
    estado                  estado_equipo NOT NULL DEFAULT 'activo',
    id_sistema_operativo    BIGINT REFERENCES sistemas_operativos(id) ON DELETE SET NULL,
    id_usuario_responsable  BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_creacion          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN equipos.id_usuario_responsable IS 'Usuario asignado/responsable del equipo (relación Usuario-Equipos del diagrama)';

-- =====================================================================
-- 5. EQUIPOS_PROGRAMAS (relación N:M entre Equipos y Programas)
-- =====================================================================
CREATE TABLE equipos_programas (
    id                  BIGSERIAL PRIMARY KEY,
    id_equipo           BIGINT NOT NULL REFERENCES equipos(id)   ON DELETE CASCADE,
    id_programa         BIGINT NOT NULL REFERENCES programas(id) ON DELETE CASCADE,
    fecha_instalacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_equipo, id_programa)
);

-- =====================================================================
-- 6. REGISTROS DE MANTENIMIENTO
-- =====================================================================
CREATE TABLE registros_mantenimiento (
    id                          BIGSERIAL PRIMARY KEY,
    id_equipo                   BIGINT NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    fecha                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo                      TEXT NOT NULL,
    tipo                        tipo_mantenimiento NOT NULL,
    fecha_proxima               TIMESTAMP,
    mantenimiento_realizado     BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================================
-- 7. REGISTROS DE ACTUALIZACIONES
-- =====================================================================
CREATE TABLE registros_actualizaciones (
    id                      BIGSERIAL PRIMARY KEY,
    id_equipo               BIGINT NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    tipo                    tipo_actualizacion NOT NULL,
    fecha                   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nombre_actualizado      VARCHAR(150) NOT NULL,
    version_actual          VARCHAR(50),
    version_actualizada     VARCHAR(50) NOT NULL
);

-- =====================================================================
-- 8. HISTORIAL DE REGISTROS
--    Une, por equipo, qué mantenimientos y qué actualizaciones ocurrieron
-- =====================================================================
CREATE TABLE historial_registros (
    id                              BIGSERIAL PRIMARY KEY,
    id_equipo                       BIGINT NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    id_registro_actualizacion       BIGINT REFERENCES registros_actualizaciones(id) ON DELETE CASCADE,
    id_registro_mantenimiento       BIGINT REFERENCES registros_mantenimiento(id) ON DELETE CASCADE,
    fecha_registro                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Un registro de historial debe referenciar al menos uno de los dos tipos de evento
    CONSTRAINT chk_historial_al_menos_un_evento
        CHECK (id_registro_actualizacion IS NOT NULL OR id_registro_mantenimiento IS NOT NULL)
);

-- =====================================================================
-- ÍNDICES para acelerar las consultas más comunes
-- =====================================================================
CREATE INDEX idx_equipos_usuario            ON equipos(id_usuario_responsable);
CREATE INDEX idx_equipos_so                 ON equipos(id_sistema_operativo);
CREATE INDEX idx_equipos_programas_equipo   ON equipos_programas(id_equipo);
CREATE INDEX idx_equipos_programas_programa ON equipos_programas(id_programa);
CREATE INDEX idx_mantenimiento_equipo       ON registros_mantenimiento(id_equipo);
CREATE INDEX idx_actualizaciones_equipo     ON registros_actualizaciones(id_equipo);
CREATE INDEX idx_historial_equipo           ON historial_registros(id_equipo);
CREATE INDEX idx_usuarios_correo            ON usuarios(correo);

-- =====================================================================
-- TRIGGER: mantiene fecha_actualizacion al día en cada UPDATE de equipos
-- =====================================================================
CREATE OR REPLACE FUNCTION fn_actualizar_fecha()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_equipos_actualizado
BEFORE UPDATE ON equipos
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha();

COMMIT;

-- =====================================================================
-- NOTAS DE CORRECCIÓN respecto al diagrama original:
-- 1. "Boleano" -> BOOLEAN correctamente escrito y tipado.
-- 2. "DataTime" / "Data time" -> TIMESTAMP (tipo real de PostgreSQL).
-- 3. "Prosesaodor" -> procesador.
-- 4. "A;o de Creacion" -> anio_creacion.
-- 5. "Contrase;a" -> contrasena_hash (siempre guardar con hash, nunca
--    en texto plano).
-- 6. "Vercion" -> version_actual / version_actualizada (consistente en
--    todas las tablas, para que las FK y UNIQUE cuadren).
-- 7. "Manteniminieto" -> mantenimiento.
-- 8. Campos de texto libre ("Rol", "Estado", "Tipo") ahora son ENUM
--    para evitar datos inconsistentes (ej. "Activo" vs "activo").
-- 9. Se agregaron llaves foráneas explícitas con REFERENCES.
-- 10. Tabla puente equipos_programas para la relación muchos a muchos
--     entre Equipos y Programas.
-- 11. Sistema Operativo y Programas quedan como catálogos normalizados.
-- 12. historial_registros tiene CHECK para asegurar que cada fila
--     enlace al menos un mantenimiento o una actualización.
-- =====================================================================