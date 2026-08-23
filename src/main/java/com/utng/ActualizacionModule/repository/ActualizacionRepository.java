package com.utng.ActualizacionModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.utng.ActualizacionModule.model.actualizacion.RegistroActualizacion;
import com.utng.ActualizacionModule.model.actualizacion.TipoActualizacion;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio real para la tabla registros_actualizaciones (DB.sql).
 * Mismo estilo que EquipoRepository / MantenimientoRepository.
 */
public class ActualizacionRepository {

    public List<RegistroActualizacion> obtenerTodos() {
        String sql = "SELECT * FROM registros_actualizaciones ORDER BY fecha DESC";
        List<RegistroActualizacion> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener actualizaciones", e);
        }
    }

    public RegistroActualizacion buscarPorId(Long id) {
        String sql = "SELECT * FROM registros_actualizaciones WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw new AppException("Error al buscar actualizacion por id", e);
        }
    }

    /** Inserta el registro y le asigna el id y la fecha reales de la BD. */
    public RegistroActualizacion guardar(RegistroActualizacion a) {
        String sql = """
                INSERT INTO registros_actualizaciones
                    (id_equipo, tipo, fecha, nombre_actualizado, version_actual, version_actualizada)
                VALUES (?, ?::tipo_actualizacion, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, a.getIdEquipo());
            ps.setString(2, a.getTipo().getValor());
            ps.setTimestamp(3, a.getFecha());
            ps.setString(4, a.getNombreActualizado());
            setStringONull(ps, 5, a.getVersionActual());
            ps.setString(6, a.getVersionActualizada());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setIdActualizacion(rs.getLong("id"));
                }
            }
            return a;

        } catch (SQLException e) {
            throw new AppException("Error al guardar actualizacion", e);
        }
    }

    public void actualizar(RegistroActualizacion a) {
        String sql = """
                UPDATE registros_actualizaciones SET
                    id_equipo = ?, tipo = ?::tipo_actualizacion, fecha = ?,
                    nombre_actualizado = ?, version_actual = ?, version_actualizada = ?
                WHERE id = ?
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, a.getIdEquipo());
            ps.setString(2, a.getTipo().getValor());
            ps.setTimestamp(3, a.getFecha());
            ps.setString(4, a.getNombreActualizado());
            setStringONull(ps, 5, a.getVersionActual());
            ps.setString(6, a.getVersionActualizada());
            ps.setLong(7, a.getIdActualizacion());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al actualizar actualizacion", e);
        }
    }

    /** Borrado fisico. Por ON DELETE CASCADE se va con el su historial. */
    public void eliminar(Long id) {
        String sql = "DELETE FROM registros_actualizaciones WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al eliminar actualizacion", e);
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private RegistroActualizacion mapear(ResultSet rs) throws SQLException {
        RegistroActualizacion a = new RegistroActualizacion();
        a.setIdActualizacion(rs.getLong("id"));
        a.setIdEquipo(rs.getLong("id_equipo"));
        a.setTipo(TipoActualizacion.fromValor(rs.getString("tipo")));
        a.setFecha(rs.getTimestamp("fecha"));
        a.setNombreActualizado(rs.getString("nombre_actualizado"));
        a.setVersionActual(rs.getString("version_actual"));
        a.setVersionActualizada(rs.getString("version_actualizada"));
        return a;
    }

    private void setStringONull(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }
}