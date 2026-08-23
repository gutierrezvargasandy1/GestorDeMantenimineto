package com.utng.TecnicoModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.utng.TecnicoModule.model.HistorialRegistro;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio para historial_registros. Ojo: esta tabla NO tiene columna de
 * descripcion; solo enlaza el equipo con el mantenimiento o la actualizacion
 * que lo origino. El detalle se obtiene despues haciendo JOIN con esas tablas.
 */
public class HistorialTecnicoRepository {

    public void registrarPorMantenimiento(Long idEquipo, Long idRegistroMantenimiento) {
        insertar(idEquipo, null, idRegistroMantenimiento);
    }

    public void registrarPorActualizacion(Long idEquipo, Long idRegistroActualizacion) {
        insertar(idEquipo, idRegistroActualizacion, null);
    }

    private void insertar(Long idEquipo, Long idRegistroActualizacion, Long idRegistroMantenimiento) {
        String sql = """
                INSERT INTO historial_registros
                    (id_equipo, id_registro_actualizacion, id_registro_mantenimiento)
                VALUES (?, ?, ?)
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEquipo);

            if (idRegistroActualizacion == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, idRegistroActualizacion);
            }

            if (idRegistroMantenimiento == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(3, idRegistroMantenimiento);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al registrar en el historial", e);
        }
    }

    /**
     * Trae el historial completo, resolviendo tipo/equipo/descripcion via JOIN
     * porque historial_registros solo guarda las llaves foraneas.
     */
    public List<HistorialRegistro> obtenerTodos() {
        // ⚠️ VERIFICAR nombres reales de tabla y columnas de registros_mantenimiento
        // y registros_actualizaciones (los tomo de MantenimientoTecnicoRepository.java
        // y ActualizacionTecnicoRepository.java que aun no tengo).
        String sql = """
                SELECT
                    h.id                          AS h_id,
                    h.id_equipo                   AS h_id_equipo,
                    e.modelo                      AS equipo_nombre,
                    h.id_registro_mantenimiento   AS h_id_mant,
                    h.id_registro_actualizacion   AS h_id_act,
                    rm.tipo                       AS mant_tipo,
                    rm.motivo                     AS mant_motivo,
                    rm.fecha                      AS mant_fecha,
                    ra.tipo                       AS act_tipo,
                    ra.nombre_actualizado         AS act_nombre,
                    ra.version_actual             AS act_version_actual,
                    ra.version_actualizada        AS act_version_nueva,
                    ra.fecha                      AS act_fecha
                FROM historial_registros h
                LEFT JOIN equipos e ON e.id = h.id_equipo
                LEFT JOIN registros_mantenimiento rm ON rm.id = h.id_registro_mantenimiento
                LEFT JOIN registros_actualizaciones ra ON ra.id = h.id_registro_actualizacion
                ORDER BY h.id DESC
                """;

        List<HistorialRegistro> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                boolean esMant = rs.getObject("h_id_mant") != null;

                String tipo = esMant ? "Mantenimiento" : "Actualizacion";
                String descripcion = esMant
                        ? (rs.getString("mant_tipo") + ": " + rs.getString("mant_motivo"))
                        : (rs.getString("act_tipo") + ": " + rs.getString("act_nombre")
                                + " (" + rs.getString("act_version_actual")
                                + " -> " + rs.getString("act_version_nueva") + ")");
                LocalDate fecha = esMant
                        ? rs.getDate("mant_fecha").toLocalDate()
                        : rs.getDate("act_fecha").toLocalDate();

                // ⚠️ VERIFICAR: orden y tipos exactos del constructor de HistorialRegistro
                lista.add(new HistorialRegistro(
                        rs.getInt("h_id"),
                        rs.getInt("h_id_equipo"), // FIX: era rs.getLong(...), el modelo pide int
                        rs.getString("equipo_nombre"),
                        tipo,
                        rs.getInt("h_id_mant"),
                        rs.getInt("h_id_act"),
                        descripcion,
                        fecha));
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener historial", e);
        }
    }
}