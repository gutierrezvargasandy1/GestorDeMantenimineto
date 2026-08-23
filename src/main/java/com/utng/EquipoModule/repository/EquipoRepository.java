package com.utng.EquipoModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.utng.EquipoModule.model.equipo.Equipo;
import com.utng.EquipoModule.model.equipo.EstadoEquipo;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio real para la tabla equipos (DB.sql).
 */
public class EquipoRepository {

    public List<Equipo> obtenerTodos() {
        String sql = "SELECT * FROM equipos ORDER BY modelo";
        List<Equipo> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEquipo(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener equipos", e);
        }
    }

    /**
     * Actualiza unicamente el estado de un equipo.
     * fecha_actualizacion la pone sola el trigger trg_equipos_actualizado.
     */
    public void actualizarEstado(Long idEquipo, String nuevaEtiquetaEstado) {
        EstadoEquipo nuevoEstado = EstadoEquipo.fromValor(nuevaEtiquetaEstado);

        String sql = "UPDATE equipos SET estado = ?::estado_equipo WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.getValor());
            ps.setLong(2, idEquipo);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new AppException("Error al actualizar el estado del equipo", ex);
        }
    }

    public Equipo buscarPorId(Long id) {
        String sql = "SELECT * FROM equipos WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearEquipo(rs) : null;
            }

        } catch (SQLException e) {
            throw new AppException("Error al buscar equipo por id", e);
        }
    }

    /**
     * Inserta el equipo y devuelve el mismo objeto con id y fechas ya asignadas.
     */
    public Equipo guardar(Equipo e) {
        String sql = """
                INSERT INTO equipos
                    (modelo, lugar, almacenamiento, memoria_ram, procesador, anio_creacion,
                     estado, id_sistema_operativo, id_usuario_responsable)
                VALUES (?, ?, ?, ?, ?, ?, ?::estado_equipo, ?, ?)
                RETURNING id, fecha_creacion, fecha_actualizacion
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getModelo());
            ps.setString(2, e.getLugar());
            ps.setString(3, e.getAlmacenamiento());
            ps.setString(4, e.getMemoriaRam());
            ps.setString(5, e.getProcesador());
            setShortONull(ps, 6, e.getAnioCreacion());
            ps.setString(7, (e.getEstado() == null ? EstadoEquipo.ACTIVO : e.getEstado()).getValor());
            setLongONull(ps, 8, e.getIdSistemaOperativo());
            setLongONull(ps, 9, e.getIdUsuarioResponsable());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setIdEquipo(rs.getLong("id"));
                    e.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                    e.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                }
            }
            return e;

        } catch (SQLException ex) {
            throw new AppException("Error al guardar equipo", ex);
        }
    }

    /**
     * Actualiza el equipo. fecha_actualizacion la pone sola el trigger
     * trg_equipos_actualizado.
     */
    public void actualizar(Equipo e) {
        String sql = """
                UPDATE equipos SET
                    modelo = ?, lugar = ?, almacenamiento = ?, memoria_ram = ?, procesador = ?,
                    anio_creacion = ?, estado = ?::estado_equipo,
                    id_sistema_operativo = ?, id_usuario_responsable = ?
                WHERE id = ?
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getModelo());
            ps.setString(2, e.getLugar());
            ps.setString(3, e.getAlmacenamiento());
            ps.setString(4, e.getMemoriaRam());
            ps.setString(5, e.getProcesador());
            setShortONull(ps, 6, e.getAnioCreacion());
            ps.setString(7, (e.getEstado() == null ? EstadoEquipo.ACTIVO : e.getEstado()).getValor());
            setLongONull(ps, 8, e.getIdSistemaOperativo());
            setLongONull(ps, 9, e.getIdUsuarioResponsable());
            ps.setLong(10, e.getIdEquipo());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new AppException("Error al actualizar equipo", ex);
        }
    }

    /**
     * Borrado fisico. Por ON DELETE CASCADE se van con el sus mantenimientos,
     * actualizaciones, historial y programas.
     */
    public void eliminar(Long id) {
        String sql = "DELETE FROM equipos WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al eliminar equipo", e);
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Equipo mapearEquipo(ResultSet rs) throws SQLException {
        Equipo e = new Equipo();
        e.setIdEquipo(rs.getLong("id"));
        e.setModelo(rs.getString("modelo"));
        e.setLugar(rs.getString("lugar"));
        e.setAlmacenamiento(rs.getString("almacenamiento"));
        e.setMemoriaRam(rs.getString("memoria_ram"));
        e.setProcesador(rs.getString("procesador"));

        short anio = rs.getShort("anio_creacion");
        e.setAnioCreacion(rs.wasNull() ? null : anio);

        e.setEstado(EstadoEquipo.fromValor(rs.getString("estado")));

        long idSo = rs.getLong("id_sistema_operativo");
        e.setIdSistemaOperativo(rs.wasNull() ? null : idSo);

        long idResp = rs.getLong("id_usuario_responsable");
        e.setIdUsuarioResponsable(rs.wasNull() ? null : idResp);

        e.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        e.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        return e;
    }

    private void setShortONull(PreparedStatement ps, int indice, Short valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.SMALLINT);
        } else {
            ps.setShort(indice, valor);
        }
    }

    private void setLongONull(PreparedStatement ps, int indice, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.BIGINT);
        } else {
            ps.setLong(indice, valor);
        }
    }
}