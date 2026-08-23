package com.utng.MantenimientoModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.utng.MantenimientoModule.model.mantenimiento.RegistroMantenimiento;
import com.utng.MantenimientoModule.model.mantenimiento.TipoMantenimiento;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio real para la tabla registros_mantenimiento (DB.sql).
 * Mismo estilo que EquipoRepository.
 */
public class MantenimientoRepository {

    public List<RegistroMantenimiento> obtenerTodos() {
        String sql = "SELECT * FROM registros_mantenimiento ORDER BY fecha DESC";
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

    public RegistroMantenimiento buscarPorId(Long id) {
        String sql = "SELECT * FROM registros_mantenimiento WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw new AppException("Error al buscar mantenimiento por id", e);
        }
    }

    /** Inserta el registro y le asigna el id real generado por la BD. */
    public RegistroMantenimiento guardar(RegistroMantenimiento m) {
        String sql = """
                INSERT INTO registros_mantenimiento
                    (id_equipo, fecha, motivo, tipo, fecha_proxima, mantenimiento_realizado)
                VALUES (?, ?, ?, ?::tipo_mantenimiento, ?, ?)
                RETURNING id
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, m.getIdEquipo());
            ps.setTimestamp(2, m.getFecha());
            ps.setString(3, m.getMotivo());
            ps.setString(4, m.getTipo().getValor());
            setTimestampONull(ps, 5, m.getFechaProxima());
            ps.setBoolean(6, m.isMantenimientoRealizado());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.setIdMantenimiento(rs.getLong("id"));
                }
            }
            return m;

        } catch (SQLException e) {
            throw new AppException("Error al guardar mantenimiento", e);
        }
    }

    public void actualizar(RegistroMantenimiento m) {
        String sql = """
                UPDATE registros_mantenimiento SET
                    id_equipo = ?, fecha = ?, motivo = ?, tipo = ?::tipo_mantenimiento,
                    fecha_proxima = ?, mantenimiento_realizado = ?
                WHERE id = ?
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, m.getIdEquipo());
            ps.setTimestamp(2, m.getFecha());
            ps.setString(3, m.getMotivo());
            ps.setString(4, m.getTipo().getValor());
            setTimestampONull(ps, 5, m.getFechaProxima());
            ps.setBoolean(6, m.isMantenimientoRealizado());
            ps.setLong(7, m.getIdMantenimiento());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al actualizar mantenimiento", e);
        }
    }

    /** Borrado fisico. Por ON DELETE CASCADE se va con el su historial. */
    public void eliminar(Long id) {
        String sql = "DELETE FROM registros_mantenimiento WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al eliminar mantenimiento", e);
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private RegistroMantenimiento mapear(ResultSet rs) throws SQLException {
        RegistroMantenimiento m = new RegistroMantenimiento();
        m.setIdMantenimiento(rs.getLong("id"));
        m.setIdEquipo(rs.getLong("id_equipo"));
        m.setFecha(rs.getTimestamp("fecha"));
        m.setMotivo(rs.getString("motivo"));
        m.setTipo(TipoMantenimiento.fromValor(rs.getString("tipo")));
        m.setFechaProxima(rs.getTimestamp("fecha_proxima"));
        m.setMantenimientoRealizado(rs.getBoolean("mantenimiento_realizado"));
        return m;
    }

    private void setTimestampONull(PreparedStatement ps, int indice, Timestamp valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, valor);
        }
    }
}