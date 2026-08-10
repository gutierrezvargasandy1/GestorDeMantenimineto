package com.utng.AiModule.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio de contexto para el asistente IA.
 * Proporciona datos COMPLETOS de la BD para que el modelo
 * pueda responder con informacion real y detallada.
 */
public class MaintenanceContextRepository {

    // ================================================================
    // TODOS LOS EQUIPOS (lista completa con estado)
    // ================================================================

    public String todosLosEquipos() {
        String sql = """
                SELECT
                    e.id,
                    e.modelo,
                    e.lugar,
                    e.estado::TEXT,
                    e.procesador,
                    e.memoria_ram,
                    e.almacenamiento,
                    e.anio_creacion,
                    so.nombre   AS so_nombre,
                    so.version_actual AS so_version,
                    u.nombre_completo || ' ' || u.apellido_paterno AS responsable
                FROM equipos e
                LEFT JOIN sistemas_operativos so ON so.id = e.id_sistema_operativo
                LEFT JOIN usuarios u ON u.id = e.id_usuario_responsable
                ORDER BY e.estado, e.modelo
                """;

        StringBuilder sb = new StringBuilder("=== LISTA COMPLETA DE EQUIPOS ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                sb.append("\n[Equipo #").append(rs.getInt("id")).append("]\n");
                sb.append("  Modelo       : ").append(rs.getString("modelo")).append("\n");
                sb.append("  Ubicacion    : ").append(rs.getString("lugar")).append("\n");
                sb.append("  Estado       : ").append(mapearEstado(rs.getString("estado"))).append("\n");
                sb.append("  Procesador   : ").append(rs.getString("procesador")).append("\n");
                sb.append("  RAM          : ").append(rs.getString("memoria_ram")).append("\n");
                sb.append("  Almacenamiento: ").append(rs.getString("almacenamiento")).append("\n");
                sb.append("  Anio         : ").append(rs.getInt("anio_creacion")).append("\n");
                sb.append("  Sistema OS   : ").append(rs.getString("so_nombre"))
                  .append(" ").append(rs.getString("so_version")).append("\n");
                sb.append("  Responsable  : ").append(rs.getString("responsable")).append("\n");
            }

            sb.append("\nTotal de equipos: ").append(count).append("\n");

        } catch (SQLException e) {
            throw new AppException("Error al obtener todos los equipos", e);
        }

        return sb.toString();
    }

    // ================================================================
    // RESUMEN GENERAL (estadisticas)
    // ================================================================

    public String resumenGeneral() {
        String sql = """
                SELECT
                    COUNT(*) AS total,
                    COUNT(*) FILTER (WHERE estado = 'activo')           AS activos,
                    COUNT(*) FILTER (WHERE estado = 'en_mantenimiento') AS en_mantenimiento,
                    COUNT(*) FILTER (WHERE estado = 'de_baja')          AS de_baja,
                    COUNT(*) FILTER (WHERE estado = 'inactivo')         AS inactivos
                FROM equipos
                """;

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format(
                    "=== RESUMEN GENERAL ===\n" +
                    "Total equipos      : %d\n" +
                    "Activos            : %d\n" +
                    "En mantenimiento   : %d\n" +
                    "De baja            : %d\n" +
                    "Inactivos          : %d\n",
                    rs.getInt("total"),
                    rs.getInt("activos"),
                    rs.getInt("en_mantenimiento"),
                    rs.getInt("de_baja"),
                    rs.getInt("inactivos"));
            }

            return "Sin datos de equipos.";

        } catch (SQLException e) {
            throw new AppException("Error al obtener resumen general", e);
        }
    }

    // ================================================================
    // TODOS LOS MANTENIMIENTOS REGISTRADOS
    // ================================================================

    public String todosLosMantenimientos(int limit) {
        String sql = """
                SELECT
                    m.id,
                    e.modelo,
                    e.lugar,
                    m.tipo::TEXT,
                    m.motivo,
                    m.fecha,
                    m.fecha_proxima,
                    m.mantenimiento_realizado,
                    e.estado::TEXT AS estado_equipo
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                ORDER BY m.fecha DESC
                LIMIT ?
                """;

        StringBuilder sb = new StringBuilder("=== REGISTROS DE MANTENIMIENTO ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append("\n[Mantenimiento #").append(rs.getInt("id")).append("]\n");
                    sb.append("  Equipo        : ").append(rs.getString("modelo"))
                      .append(" — ").append(rs.getString("lugar")).append("\n");
                    sb.append("  Tipo          : ").append(mapearTipo(rs.getString("tipo"))).append("\n");
                    sb.append("  Motivo        : ").append(rs.getString("motivo")).append("\n");
                    sb.append("  Fecha         : ").append(rs.getTimestamp("fecha")).append("\n");

                    if (rs.getTimestamp("fecha_proxima") != null) {
                        sb.append("  Proxima fecha : ").append(rs.getTimestamp("fecha_proxima")).append("\n");
                    }

                    sb.append("  Realizado     : ").append(rs.getBoolean("mantenimiento_realizado") ? "Si" : "No").append("\n");
                    sb.append("  Estado equipo : ").append(mapearEstado(rs.getString("estado_equipo"))).append("\n");
                }

                sb.append("\nTotal registros mostrados: ").append(count).append("\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al obtener todos los mantenimientos", e);
        }

        return sb.toString();
    }

    // ================================================================
    // HISTORIAL COMPLETO DE UN EQUIPO ESPECIFICO
    // ================================================================

    public String historialEquipo(String busqueda, int maxRows) {
        String sqlEquipos = """
                SELECT id, modelo, lugar, estado::TEXT, procesador, memoria_ram, almacenamiento
                FROM equipos
                WHERE modelo ILIKE ? OR lugar ILIKE ?
                LIMIT 5
                """;

        String sqlMantenimientos = """
                SELECT fecha, tipo::TEXT, motivo, fecha_proxima, mantenimiento_realizado
                FROM registros_mantenimiento
                WHERE id_equipo = ?
                ORDER BY fecha DESC
                LIMIT ?
                """;

        String sqlActualizaciones = """
                SELECT fecha, tipo::TEXT, nombre_actualizado, version_actual, version_actualizada
                FROM registros_actualizaciones
                WHERE id_equipo = ?
                ORDER BY fecha DESC
                LIMIT ?
                """;

        String sqlProgramas = """
                SELECT p.nombre, p.version_actual, ep.fecha_instalacion
                FROM equipos_programas ep
                JOIN programas p ON p.id = ep.id_programa
                WHERE ep.id_equipo = ?
                ORDER BY p.nombre
                """;

        StringBuilder sb = new StringBuilder();
        String like = "%" + busqueda + "%";

        try (Connection con = ConectionDB.conectar();
             PreparedStatement psEq = con.prepareStatement(sqlEquipos)) {

            psEq.setString(1, like);
            psEq.setString(2, like);

            try (ResultSet rsEq = psEq.executeQuery()) {
                boolean hayEquipos = false;

                while (rsEq.next()) {
                    hayEquipos = true;
                    long idEquipo = rsEq.getLong("id");

                    sb.append("=== HISTORIAL: ").append(rsEq.getString("modelo")).append(" ===\n");
                    sb.append("Ubicacion  : ").append(rsEq.getString("lugar")).append("\n");
                    sb.append("Estado     : ").append(mapearEstado(rsEq.getString("estado"))).append("\n");
                    sb.append("Procesador : ").append(rsEq.getString("procesador")).append("\n");
                    sb.append("RAM        : ").append(rsEq.getString("memoria_ram")).append("\n");
                    sb.append("Disco      : ").append(rsEq.getString("almacenamiento")).append("\n\n");

                    // Mantenimientos
                    sb.append("--- Mantenimientos ---\n");
                    try (PreparedStatement psM = con.prepareStatement(sqlMantenimientos)) {
                        psM.setLong(1, idEquipo);
                        psM.setInt(2, maxRows);
                        try (ResultSet rsM = psM.executeQuery()) {
                            boolean hayM = false;
                            while (rsM.next()) {
                                hayM = true;
                                sb.append("  ").append(rsM.getTimestamp("fecha"))
                                  .append(" | ").append(mapearTipo(rsM.getString("tipo")))
                                  .append(" | ").append(rsM.getString("motivo"));
                                if (rsM.getTimestamp("fecha_proxima") != null) {
                                    sb.append(" | Proxima: ").append(rsM.getTimestamp("fecha_proxima"));
                                }
                                sb.append(" | Realizado: ")
                                  .append(rsM.getBoolean("mantenimiento_realizado") ? "Si" : "Pendiente")
                                  .append("\n");
                            }
                            if (!hayM) sb.append("  (sin mantenimientos registrados)\n");
                        }
                    }

                    // Actualizaciones
                    sb.append("\n--- Actualizaciones ---\n");
                    try (PreparedStatement psA = con.prepareStatement(sqlActualizaciones)) {
                        psA.setLong(1, idEquipo);
                        psA.setInt(2, maxRows);
                        try (ResultSet rsA = psA.executeQuery()) {
                            boolean hayA = false;
                            while (rsA.next()) {
                                hayA = true;
                                sb.append("  ").append(rsA.getTimestamp("fecha"))
                                  .append(" | ").append(rsA.getString("tipo"))
                                  .append(" | ").append(rsA.getString("nombre_actualizado"))
                                  .append(": v").append(rsA.getString("version_actual"))
                                  .append(" -> v").append(rsA.getString("version_actualizada"))
                                  .append("\n");
                            }
                            if (!hayA) sb.append("  (sin actualizaciones registradas)\n");
                        }
                    }

                    // Programas instalados
                    sb.append("\n--- Programas instalados ---\n");
                    try (PreparedStatement psP = con.prepareStatement(sqlProgramas)) {
                        psP.setLong(1, idEquipo);
                        try (ResultSet rsP = psP.executeQuery()) {
                            boolean hayP = false;
                            while (rsP.next()) {
                                hayP = true;
                                sb.append("  - ").append(rsP.getString("nombre"))
                                  .append(" v").append(rsP.getString("version_actual"))
                                  .append("\n");
                            }
                            if (!hayP) sb.append("  (sin programas registrados)\n");
                        }
                    }

                    sb.append("\n");
                }

                if (!hayEquipos) {
                    sb.append("No se encontraron equipos con \"").append(busqueda).append("\".\n");
                }
            }

            return sb.toString();

        } catch (SQLException e) {
            throw new AppException("Error al consultar historial del equipo", e);
        }
    }

    // ================================================================
    // REPORTE DEL MES ACTUAL
    // ================================================================

    public String reporteMesActual() {
        String sqlTipos = """
                SELECT tipo::TEXT, COUNT(*) AS total
                FROM registros_mantenimiento
                WHERE date_trunc('month', fecha) = date_trunc('month', CURRENT_DATE)
                GROUP BY tipo
                """;

        String sqlDetalle = """
                SELECT m.tipo::TEXT, m.motivo, m.fecha, e.modelo, e.lugar,
                       m.mantenimiento_realizado
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                WHERE date_trunc('month', m.fecha) = date_trunc('month', CURRENT_DATE)
                ORDER BY m.fecha DESC
                """;

        StringBuilder sb = new StringBuilder("=== REPORTE DEL MES ACTUAL ===\n\n");

        try (Connection con = ConectionDB.conectar()) {

            // Resumen por tipo
            try (PreparedStatement ps = con.prepareStatement(sqlTipos);
                 ResultSet rs = ps.executeQuery()) {

                sb.append("Resumen por tipo:\n");
                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    sb.append("  - ").append(mapearTipo(rs.getString("tipo")))
                      .append(": ").append(rs.getInt("total")).append("\n");
                }
                if (!hayDatos) sb.append("  (Sin mantenimientos este mes)\n");
            }

            sb.append("\nDetalle de registros:\n");

            // Detalle completo
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle);
                 ResultSet rs = ps.executeQuery()) {

                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    sb.append("  [").append(rs.getTimestamp("fecha")).append("] ")
                      .append(mapearTipo(rs.getString("tipo")))
                      .append(" — ").append(rs.getString("modelo"))
                      .append(" (").append(rs.getString("lugar")).append(")")
                      .append(" | ").append(rs.getString("motivo"))
                      .append(" | ").append(rs.getBoolean("mantenimiento_realizado") ? "Realizado" : "Pendiente")
                      .append("\n");
                }
                if (!hayDatos) sb.append("  (Sin registros detallados este mes)\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al generar reporte del mes", e);
        }

        return sb.toString();
    }

    // ================================================================
    // TODOS LOS USUARIOS Y TECNICOS
    // ================================================================

    public String todosLosUsuarios() {
        String sql = """
                SELECT
                    u.id,
                    u.nombre_completo,
                    u.apellido_paterno,
                    u.apellido_materno,
                    u.correo,
                    u.rol::TEXT,
                    u.activo,
                    COUNT(e.id) AS equipos_asignados
                FROM usuarios u
                LEFT JOIN equipos e ON e.id_usuario_responsable = u.id
                GROUP BY u.id, u.nombre_completo, u.apellido_paterno,
                         u.apellido_materno, u.correo, u.rol, u.activo
                ORDER BY u.rol, u.nombre_completo
                """;

        StringBuilder sb = new StringBuilder("=== USUARIOS DEL SISTEMA ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append("\n[Usuario #").append(rs.getInt("id")).append("]\n");
                sb.append("  Nombre   : ").append(rs.getString("nombre_completo"))
                  .append(" ").append(rs.getString("apellido_paterno")).append("\n");
                sb.append("  Correo   : ").append(rs.getString("correo")).append("\n");
                sb.append("  Rol      : ").append(rs.getString("rol")).append("\n");
                sb.append("  Activo   : ").append(rs.getBoolean("activo") ? "Si" : "No").append("\n");
                sb.append("  Equipos asignados: ").append(rs.getInt("equipos_asignados")).append("\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al obtener usuarios", e);
        }

        return sb.toString();
    }

    // ================================================================
    // SERVICIOS POR TECNICO (detallado)
    // ================================================================

    public String serviciosPorTecnico(int maxRows) {
        String sql = """
                SELECT
                    u.nombre_completo || ' ' || u.apellido_paterno AS tecnico,
                    u.correo,
                    COUNT(m.id) AS total_mantenimientos,
                    COUNT(m.id) FILTER (WHERE m.tipo = 'preventivo') AS preventivos,
                    COUNT(m.id) FILTER (WHERE m.tipo = 'correctivo') AS correctivos,
                    COUNT(DISTINCT e.id) AS equipos_responsable
                FROM usuarios u
                JOIN equipos e ON e.id_usuario_responsable = u.id
                LEFT JOIN registros_mantenimiento m ON m.id_equipo = e.id
                WHERE u.rol = 'tecnico'
                GROUP BY u.id, u.nombre_completo, u.apellido_paterno, u.correo
                ORDER BY total_mantenimientos DESC
                LIMIT ?
                """;

        StringBuilder sb = new StringBuilder("=== SERVICIOS POR TECNICO ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append("\n  Tecnico   : ").append(rs.getString("tecnico")).append("\n");
                    sb.append("  Correo    : ").append(rs.getString("correo")).append("\n");
                    sb.append("  Total mtto: ").append(rs.getInt("total_mantenimientos")).append("\n");
                    sb.append("  Preventivos: ").append(rs.getInt("preventivos")).append("\n");
                    sb.append("  Correctivos: ").append(rs.getInt("correctivos")).append("\n");
                    sb.append("  Equipos a cargo: ").append(rs.getInt("equipos_responsable")).append("\n");
                }
            }

        } catch (SQLException e) {
            throw new AppException("Error al obtener servicios por tecnico", e);
        }

        return sb.toString();
    }

    // ================================================================
    // PROXIMOS MANTENIMIENTOS PENDIENTES
    // ================================================================

    public String proximosMantenimientos(int maxRows) {
        String sql = """
                SELECT
                    e.modelo,
                    e.lugar,
                    e.estado::TEXT,
                    m.tipo::TEXT,
                    m.motivo,
                    m.fecha_proxima
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                WHERE m.fecha_proxima IS NOT NULL
                  AND m.mantenimiento_realizado = FALSE
                  AND m.fecha_proxima >= CURRENT_DATE
                ORDER BY m.fecha_proxima ASC
                LIMIT ?
                """;

        StringBuilder sb = new StringBuilder("=== PROXIMOS MANTENIMIENTOS PENDIENTES ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    sb.append("\n  Fecha     : ").append(rs.getTimestamp("fecha_proxima")).append("\n");
                    sb.append("  Equipo    : ").append(rs.getString("modelo"))
                      .append(" — ").append(rs.getString("lugar")).append("\n");
                    sb.append("  Estado    : ").append(mapearEstado(rs.getString("estado"))).append("\n");
                    sb.append("  Tipo      : ").append(mapearTipo(rs.getString("tipo"))).append("\n");
                    sb.append("  Motivo    : ").append(rs.getString("motivo")).append("\n");
                }
                if (!hayDatos) sb.append("  (Sin mantenimientos proximos pendientes)\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al consultar proximos mantenimientos", e);
        }

        return sb.toString();
    }

    // ================================================================
    // EQUIPOS CON MAS INCIDENCIAS (correctivos)
    // ================================================================

    public String equiposConMasIncidencias(int maxRows) {
        String sql = """
                SELECT
                    e.modelo,
                    e.lugar,
                    e.estado::TEXT,
                    COUNT(*) AS total_fallas,
                    MAX(m.fecha) AS ultima_falla
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                WHERE m.tipo = 'correctivo'
                GROUP BY e.id, e.modelo, e.lugar, e.estado
                ORDER BY total_fallas DESC
                LIMIT ?
                """;

        StringBuilder sb = new StringBuilder("=== EQUIPOS CON MAS FALLAS CORRECTIVAS ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    sb.append("\n  Equipo    : ").append(rs.getString("modelo"))
                      .append(" — ").append(rs.getString("lugar")).append("\n");
                    sb.append("  Estado    : ").append(mapearEstado(rs.getString("estado"))).append("\n");
                    sb.append("  Fallas    : ").append(rs.getInt("total_fallas")).append("\n");
                    sb.append("  Ultima falla: ").append(rs.getTimestamp("ultima_falla")).append("\n");
                }
                if (!hayDatos) sb.append("  (Sin fallas correctivas registradas)\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al calcular equipos con mas incidencias", e);
        }

        return sb.toString();
    }

    // ================================================================
    // PROGRAMAS INSTALADOS (con busqueda o todos)
    // ================================================================

    public String programasInstalados(String busquedaEquipo, int maxRows) {
        String sql = """
                SELECT
                    e.modelo,
                    e.lugar,
                    p.nombre,
                    p.tipo::TEXT AS tipo_programa,
                    p.version_actual,
                    ep.fecha_instalacion
                FROM equipos_programas ep
                JOIN equipos e ON e.id = ep.id_equipo
                JOIN programas p ON p.id = ep.id_programa
                WHERE e.modelo ILIKE ? OR e.lugar ILIKE ? OR p.nombre ILIKE ?
                ORDER BY e.modelo, p.nombre
                LIMIT ?
                """;

        String like = "%" + busquedaEquipo + "%";
        StringBuilder sb = new StringBuilder("=== PROGRAMAS INSTALADOS ===\n");

        try (Connection con = ConectionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setInt(4, maxRows);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    sb.append("  - ").append(rs.getString("nombre"))
                      .append(" v").append(rs.getString("version_actual"))
                      .append(" [").append(rs.getString("tipo_programa")).append("]")
                      .append(" en ").append(rs.getString("modelo"))
                      .append(" (").append(rs.getString("lugar")).append(")")
                      .append("\n");
                }
                if (!hayDatos) sb.append("  (Sin coincidencias para: " + busquedaEquipo + ")\n");
            }

        } catch (SQLException e) {
            throw new AppException("Error al consultar programas instalados", e);
        }

        return sb.toString();
    }

    // ================================================================
    // CONTEXTO COMPLETO (para preguntas generales)
    // ================================================================

    public String contextoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(resumenGeneral()).append("\n");
        sb.append(todosLosEquipos()).append("\n");
        sb.append(todosLosMantenimientos(50)).append("\n");
        return sb.toString();
    }

    // ================================================================
    // HELPERS PRIVADOS
    // ================================================================

    private String mapearEstado(String dbValue) {
        if (dbValue == null) return "Desconocido";
        switch (dbValue.toLowerCase()) {
            case "activo":           return "Activo";
            case "en_mantenimiento": return "En mantenimiento";
            case "inactivo":         return "Inactivo";
            case "de_baja":          return "De baja";
            default:                 return dbValue;
        }
    }

    private String mapearTipo(String dbValue) {
        if (dbValue == null) return "Sin tipo";
        switch (dbValue.toLowerCase()) {
            case "preventivo": return "Preventivo";
            case "correctivo": return "Correctivo";
            default:           return dbValue;
        }
    }
}