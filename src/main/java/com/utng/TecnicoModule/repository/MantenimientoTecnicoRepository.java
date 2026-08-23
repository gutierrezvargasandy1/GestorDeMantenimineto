package com.utng.TecnicoModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.utng.TecnicoModule.model.RegistroMantenimiento;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio real para registros_mantenimiento, usado por la pantalla
 * del rol Tecnico. Trae ya resueltos el nombre del equipo (JOIN equipos)
 * y el nombre del responsable (JOIN usuarios).
 */
public class MantenimientoTecnicoRepository {

    private static final String SELECT_BASE = """
            SELECT
                m.id, m.id_equipo, e.modelo AS equipo_nombre,
                m.tipo::TEXT AS tipo, m.motivo, m.fecha, m.fecha_proxima,
                m.mantenimiento_realizado, m.notas_realizado,
                e.id_usuario_responsable,
                COALESCE(u.nombre_completo || ' ' || u.apellido_paterno, 'Sin asignar') AS responsable_nombre
            FROM registros_mantenimiento m
            JOIN equipos e ON e.id = m.id_equipo
            LEFT JOIN usuarios u ON u.id = e.id_usuario_responsable
            """;

    public List<RegistroMantenimiento> obtenerTodos() {
        String sql = SELECT_BASE + " ORDER BY m.fecha DESC";
        List<RegistroMantenimiento> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener mantenimientos", e);
        }
    }

    public RegistroMantenimiento guardar(RegistroMantenimiento r) {
        String sql = """
                INSERT INTO registros_mantenimiento
                    (id_equipo, fecha, motivo, tipo, fecha_proxima, mantenimiento_realizado, notas_realizado)
                VALUES (?, ?, ?, ?::tipo_mantenimiento, ?, ?, ?)
                RETURNING id
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, r.getIdEquipo());
            ps.setTimestamp(2, Timestamp.valueOf(r.getFecha().atStartOfDay()));
            ps.setString(3, r.getMotivo());
            ps.setString(4, r.getTipo().toLowerCase());
            setTimestampONull(ps, 5, r.getFechaProxima());
            ps.setBoolean(6, r.isMantenimientoRealizado());
            ps.setString(7, r.getNotasRealizado());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setId(rs.getLong("id"));
                }
            }
            return r;

        } catch (SQLException e) {
            throw new AppException("Error al guardar mantenimiento", e);
        }
    }

    public void actualizar(RegistroMantenimiento r) {
        String sql = """
                UPDATE registros_mantenimiento SET
                    id_equipo = ?, fecha = ?, motivo = ?, tipo = ?::tipo_mantenimiento,
                    fecha_proxima = ?, mantenimiento_realizado = ?, notas_realizado = ?
                WHERE id = ?
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, r.getIdEquipo());
            ps.setTimestamp(2, Timestamp.valueOf(r.getFecha().atStartOfDay()));
            ps.setString(3, r.getMotivo());
            ps.setString(4, r.getTipo().toLowerCase());
            setTimestampONull(ps, 5, r.getFechaProxima());
            ps.setBoolean(6, r.isMantenimientoRealizado());
            ps.setString(7, r.getNotasRealizado());
            ps.setLong(8, r.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al actualizar mantenimiento", e);
        }
    }

    private RegistroMantenimiento mapear(ResultSet rs) throws SQLException {
        RegistroMantenimiento r = new RegistroMantenimiento();
        r.setId(rs.getLong("id"));
        r.setIdEquipo(rs.getLong("id_equipo"));
        r.setEquipoNombre(rs.getString("equipo_nombre"));
        r.setTipo(capitalizar(rs.getString("tipo")));
        r.setMotivo(rs.getString("motivo"));
        r.setFecha(rs.getTimestamp("fecha").toLocalDateTime().toLocalDate());

        Timestamp proxima = rs.getTimestamp("fecha_proxima");
        r.setFechaProxima(proxima == null ? null : proxima.toLocalDateTime().toLocalDate());

        r.setMantenimientoRealizado(rs.getBoolean("mantenimiento_realizado"));
        r.setNotasRealizado(rs.getString("notas_realizado"));

        long idResp = rs.getLong("id_usuario_responsable");
        r.setIdUsuarioResponsable(rs.wasNull() ? null : idResp);
        r.setUsuarioResponsable(rs.getString("responsable_nombre"));

        return r;
    }

    private String capitalizar(String v) {
        if (v == null || v.isBlank())
            return v;
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    private void setTimestampONull(PreparedStatement ps, int indice, LocalDate fecha) throws SQLException {
        if (fecha == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, Timestamp.valueOf(fecha.atStartOfDay()));
        }
    }
}