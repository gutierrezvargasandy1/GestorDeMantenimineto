package com.utng.TecnicoModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.utng.TecnicoModule.model.RegistroActualizacion;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/**
 * Repositorio real para registros_actualizaciones, usado por la pantalla del
 * Tecnico.
 */
public class ActualizacionTecnicoRepository {

    private static final String SELECT_BASE = """
            SELECT
                a.id, a.id_equipo, e.modelo AS equipo_nombre,
                a.tipo::TEXT AS tipo, a.nombre_actualizado, a.version_actual,
                a.version_actualizada, a.fecha,
                e.id_usuario_responsable,
                COALESCE(u.nombre_completo || ' ' || u.apellido_paterno, 'Sin asignar') AS responsable_nombre
            FROM registros_actualizaciones a
            JOIN equipos e ON e.id = a.id_equipo
            LEFT JOIN usuarios u ON u.id = e.id_usuario_responsable
            """;

    public List<RegistroActualizacion> obtenerTodos() {
        String sql = SELECT_BASE + " ORDER BY a.fecha DESC";
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
            ps.setString(2, valorEnum(a.getTipo()));
            ps.setTimestamp(3, Timestamp.valueOf(a.getFecha().atStartOfDay()));
            ps.setString(4, a.getNombreActualizado());
            ps.setString(5, a.getVersionActual());
            ps.setString(6, a.getVersionActualizada());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setId(rs.getLong("id"));
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
            ps.setString(2, valorEnum(a.getTipo()));
            ps.setTimestamp(3, Timestamp.valueOf(a.getFecha().atStartOfDay()));
            ps.setString(4, a.getNombreActualizado());
            ps.setString(5, a.getVersionActual());
            ps.setString(6, a.getVersionActualizada());
            ps.setLong(7, a.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al actualizar registro de actualizacion", e);
        }
    }

    /** "Sistema operativo" -> "sistema_operativo" (coincide con el ENUM). */
    private String valorEnum(String etiqueta) {
        if (etiqueta == null)
            return null;
        return etiqueta.trim().toLowerCase().replace(" ", "_");
    }

    private RegistroActualizacion mapear(ResultSet rs) throws SQLException {
        RegistroActualizacion a = new RegistroActualizacion();
        a.setId(rs.getLong("id"));
        a.setIdEquipo(rs.getLong("id_equipo"));
        a.setEquipoNombre(rs.getString("equipo_nombre"));
        a.setTipo(etiquetaLegible(rs.getString("tipo")));
        a.setNombreActualizado(rs.getString("nombre_actualizado"));
        a.setVersionActual(rs.getString("version_actual"));
        a.setVersionActualizada(rs.getString("version_actualizada"));
        a.setFecha(rs.getTimestamp("fecha").toLocalDateTime().toLocalDate());

        long idResp = rs.getLong("id_usuario_responsable");
        a.setIdUsuarioResponsable(rs.wasNull() ? null : idResp);
        a.setUsuarioResponsable(rs.getString("responsable_nombre"));

        return a;
    }

    /** "sistema_operativo" -> "Sistema operativo" */
    private String etiquetaLegible(String valor) {
        if (valor == null)
            return valor;
        String v = valor.replace("_", " ");
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}