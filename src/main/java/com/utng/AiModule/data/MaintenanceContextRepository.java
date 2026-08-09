package com.utng.AiModule.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.utng.config.ConectionDB;
import com.utng.util.AppException;

public class MaintenanceContextRepository {

    // =========================
    // RESUMEN GENERAL
    // =========================

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

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                return String.format(
                        "Resumen actual de equipos: %d totales, %d activos, %d en mantenimiento, %d de baja, %d inactivos.",
                        rs.getInt("total"),
                        rs.getInt("activos"),
                        rs.getInt("en_mantenimiento"),
                        rs.getInt("de_baja"),
                        rs.getInt("inactivos"));

            }

            return "Sin datos.";

        } catch (SQLException e) {

            throw new AppException(
                    "Error al obtener el resumen general de equipos",
                    e);

        }

    }

    // =========================
    // HISTORIAL DE EQUIPO
    // =========================

    public String historialEquipo(String busqueda, int maxRows) {

        String sqlEquipos = """
                SELECT id, modelo, lugar, estado, procesador, memoria_ram, almacenamiento
                FROM equipos
                WHERE modelo ILIKE ? OR lugar ILIKE ?
                LIMIT 5
                """;

        String sqlMantenimientos = """
                SELECT fecha, tipo, motivo, fecha_proxima, mantenimiento_realizado
                FROM registros_mantenimiento
                WHERE id_equipo = ?
                ORDER BY fecha DESC
                LIMIT ?
                """;

        String sqlActualizaciones = """
                SELECT fecha, tipo, nombre_actualizado, version_actual, version_actualizada
                FROM registros_actualizaciones
                WHERE id_equipo = ?
                ORDER BY fecha DESC
                LIMIT ?
                """;

        StringBuilder sb = new StringBuilder();

        String like = "%" + busqueda + "%";

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement psEquipos = con.prepareStatement(sqlEquipos)) {

            psEquipos.setString(1, like);

            psEquipos.setString(2, like);

            try (ResultSet rsEq = psEquipos.executeQuery()) {

                boolean hayEquipos = false;

                while (rsEq.next()) {

                    hayEquipos = true;

                    long idEquipo = rsEq.getLong("id");

                    sb.append("Equipo #").append(idEquipo)
                      .append(" — modelo: ").append(rsEq.getString("modelo"))
                      .append(", lugar: ").append(rsEq.getString("lugar"))
                      .append(", estado: ").append(rsEq.getString("estado"))
                      .append(", specs: ").append(rsEq.getString("procesador"))
                      .append(" / ").append(rsEq.getString("memoria_ram"))
                      .append(" / ").append(rsEq.getString("almacenamiento"))
                      .append("\n");

                    try (PreparedStatement psM = con.prepareStatement(sqlMantenimientos)) {

                        psM.setLong(1, idEquipo);

                        psM.setInt(2, maxRows);

                        try (ResultSet rsM = psM.executeQuery()) {

                            sb.append("  Mantenimientos:\n");

                            boolean hayM = false;

                            while (rsM.next()) {

                                hayM = true;

                                sb.append("  - ").append(rsM.getTimestamp("fecha"))
                                  .append(" | tipo: ").append(rsM.getString("tipo"))
                                  .append(" | motivo: ").append(rsM.getString("motivo"))
                                  .append(" | próxima fecha: ").append(rsM.getTimestamp("fecha_proxima"))
                                  .append(" | realizado: ").append(rsM.getBoolean("mantenimiento_realizado"))
                                  .append("\n");

                            }

                            if (!hayM) {

                                sb.append("  (sin mantenimientos registrados)\n");

                            }

                        }

                    }

                    try (PreparedStatement psA = con.prepareStatement(sqlActualizaciones)) {

                        psA.setLong(1, idEquipo);

                        psA.setInt(2, maxRows);

                        try (ResultSet rsA = psA.executeQuery()) {

                            sb.append("  Actualizaciones:\n");

                            boolean hayA = false;

                            while (rsA.next()) {

                                hayA = true;

                                sb.append("  - ").append(rsA.getTimestamp("fecha"))
                                  .append(" | tipo: ").append(rsA.getString("tipo"))
                                  .append(" | ").append(rsA.getString("nombre_actualizado"))
                                  .append(": ").append(rsA.getString("version_actual"))
                                  .append(" → ").append(rsA.getString("version_actualizada"))
                                  .append("\n");

                            }

                            if (!hayA) {

                                sb.append("  (sin actualizaciones registradas)\n");

                            }

                        }

                    }

                }

                if (!hayEquipos) {

                    sb.append("No se encontraron equipos que coincidan con \"").append(busqueda).append("\".\n");

                }

            }

            return sb.toString();

        } catch (SQLException e) {

            throw new AppException(
                    "Error al consultar el historial del equipo",
                    e);

        }

    }

    // =========================
    // REPORTE DEL MES ACTUAL
    // =========================

    public String reporteMesActual() {

        String sql = """
                SELECT tipo, COUNT(*) AS total
                FROM registros_mantenimiento
                WHERE date_trunc('month', fecha) = date_trunc('month', CURRENT_DATE)
                GROUP BY tipo
                """;

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder("Mantenimientos de este mes por tipo:\n");

            boolean hayDatos = false;

            while (rs.next()) {

                hayDatos = true;

                sb.append("- ").append(rs.getString("tipo"))
                  .append(": ").append(rs.getInt("total"))
                  .append("\n");

            }

            if (!hayDatos) {

                sb.append("(Sin mantenimientos registrados este mes)\n");

            }

            return sb.toString();

        } catch (SQLException e) {

            throw new AppException(
                    "Error al generar el reporte del mes actual",
                    e);

        }

    }

    // =========================
    // SERVICIOS POR TÉCNICO
    // =========================

    /**
     * APROXIMACIÓN: cuenta mantenimientos de equipos que cada usuario con rol
     * 'tecnico' tiene asignados como responsable. El esquema actual no registra
     * quién EJECUTÓ cada mantenimiento.
     */
    public String serviciosPorTecnico(int maxRows) {

        String sql = """
                SELECT u.nombre_completo, u.apellido_paterno, COUNT(m.id) AS total
                FROM usuarios u
                JOIN equipos e ON e.id_usuario_responsable = u.id
                LEFT JOIN registros_mantenimiento m ON m.id_equipo = e.id
                WHERE u.rol = 'tecnico'
                GROUP BY u.id, u.nombre_completo, u.apellido_paterno
                ORDER BY total DESC
                LIMIT ?
                """;

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {

                StringBuilder sb = new StringBuilder(
                        "Mantenimientos asociados a equipos de cada técnico responsable "
                                + "(nota: no es el ejecutor real del servicio, es el responsable del equipo):\n");

                while (rs.next()) {

                    sb.append("- ").append(rs.getString("nombre_completo"))
                      .append(" ").append(rs.getString("apellido_paterno"))
                      .append(": ").append(rs.getInt("total")).append(" mantenimientos\n");

                }

                return sb.toString();

            }

        } catch (SQLException e) {

            throw new AppException(
                    "Error al obtener servicios por técnico",
                    e);

        }

    }

    // =========================
    // EQUIPOS CON MÁS INCIDENCIAS
    // =========================

    public String equiposConMasIncidencias(int maxRows) {

        String sql = """
                SELECT e.modelo, e.lugar, COUNT(*) AS total, MAX(m.fecha) AS ultima_fecha
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                WHERE m.tipo = 'correctivo'
                GROUP BY e.id, e.modelo, e.lugar
                ORDER BY total DESC
                LIMIT ?
                """;

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {

                StringBuilder sb = new StringBuilder("Equipos con más fallas correctivas (posible riesgo):\n");

                boolean hayDatos = false;

                while (rs.next()) {

                    hayDatos = true;

                    sb.append("- ").append(rs.getString("modelo"))
                      .append(" (").append(rs.getString("lugar")).append(")")
                      .append(": ").append(rs.getInt("total")).append(" fallas")
                      .append(", última el ").append(rs.getTimestamp("ultima_fecha"))
                      .append("\n");

                }

                if (!hayDatos) {

                    sb.append("(Sin mantenimientos correctivos registrados)\n");

                }

                return sb.toString();

            }

        } catch (SQLException e) {

            throw new AppException(
                    "Error al calcular equipos con más incidencias",
                    e);

        }

    }

    // =========================
    // PRÓXIMOS MANTENIMIENTOS
    // =========================

    public String proximosMantenimientos(int maxRows) {

        String sql = """
                SELECT e.modelo, e.lugar, m.fecha_proxima, m.tipo
                FROM registros_mantenimiento m
                JOIN equipos e ON e.id = m.id_equipo
                WHERE m.fecha_proxima IS NOT NULL
                  AND m.mantenimiento_realizado = FALSE
                  AND m.fecha_proxima >= CURRENT_DATE
                ORDER BY m.fecha_proxima ASC
                LIMIT ?
                """;

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maxRows);

            try (ResultSet rs = ps.executeQuery()) {

                StringBuilder sb = new StringBuilder("Próximos mantenimientos programados:\n");

                boolean hayDatos = false;

                while (rs.next()) {

                    hayDatos = true;

                    sb.append("- ").append(rs.getString("modelo"))
                      .append(" (").append(rs.getString("lugar")).append(")")
                      .append(": ").append(rs.getString("tipo"))
                      .append(" programado para ").append(rs.getTimestamp("fecha_proxima"))
                      .append("\n");

                }

                if (!hayDatos) {

                    sb.append("(Sin mantenimientos programados próximos)\n");

                }

                return sb.toString();

            }

        } catch (SQLException e) {

            throw new AppException(
                    "Error al consultar próximos mantenimientos",
                    e);

        }

    }

    // =========================
    // PROGRAMAS INSTALADOS
    // =========================

    public String programasInstalados(String busquedaEquipo, int maxRows) {

        String sql = """
                SELECT e.modelo, e.lugar, p.nombre, p.version_actual, ep.fecha_instalacion
                FROM equipos_programas ep
                JOIN equipos e ON e.id = ep.id_equipo
                JOIN programas p ON p.id = ep.id_programa
                WHERE e.modelo ILIKE ? OR e.lugar ILIKE ?
                ORDER BY ep.fecha_instalacion DESC
                LIMIT ?
                """;

        String like = "%" + busquedaEquipo + "%";

        try (
                Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, like);

            ps.setString(2, like);

            ps.setInt(3, maxRows);

            try (ResultSet rs = ps.executeQuery()) {

                StringBuilder sb = new StringBuilder("Programas instalados:\n");

                boolean hayDatos = false;

                while (rs.next()) {

                    hayDatos = true;

                    sb.append("- ").append(rs.getString("nombre"))
                      .append(" v").append(rs.getString("version_actual"))
                      .append(" en ").append(rs.getString("modelo"))
                      .append(" (").append(rs.getString("lugar")).append(")")
                      .append(", instalado el ").append(rs.getTimestamp("fecha_instalacion"))
                      .append("\n");

                }

                if (!hayDatos) {

                    sb.append("(Sin coincidencias)\n");

                }

                return sb.toString();

            }

        } catch (SQLException e) {

            throw new AppException(
                    "Error al consultar programas instalados",
                    e);

        }

    }

}