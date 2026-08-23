package com.utng.SistemasOperativosModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

/** Repositorio real para el catalogo sistemas_operativos. */
public class SistemaOperativoRepository {

    public List<SistemaOperativo> obtenerTodos() {
        String sql = "SELECT * FROM sistemas_operativos ORDER BY nombre, version_actual";
        List<SistemaOperativo> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener sistemas operativos", e);
        }
    }

    public SistemaOperativo buscarPorId(Long id) {
        String sql = "SELECT * FROM sistemas_operativos WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw new AppException("Error al buscar sistema operativo por id", e);
        }
    }

    /**
     * Inserta el sistema operativo y le asigna el id real generado por la BD.
     * "tipo" NO es ENUM aqui (es VARCHAR(50) libre), asi que no lleva cast.
     */
    public SistemaOperativo guardar(SistemaOperativo so) {
        String sql = """
                INSERT INTO sistemas_operativos (tipo, nombre, version_actual)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, so.getTipo());
            ps.setString(2, so.getNombre());
            ps.setString(3, so.getVersionActual());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    so.setIdSistemaOperativo(rs.getLong("id"));
                }
            }
            return so;

        } catch (SQLException e) {
            throw new AppException("Error al guardar sistema operativo", e);
        }
    }

    public void actualizar(SistemaOperativo so) {
        String sql = """
                UPDATE sistemas_operativos
                SET tipo = ?, nombre = ?, version_actual = ?
                WHERE id = ?
                """;

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, so.getTipo());
            ps.setString(2, so.getNombre());
            ps.setString(3, so.getVersionActual());
            ps.setLong(4, so.getIdSistemaOperativo());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al actualizar sistema operativo", e);
        }
    }

    /**
     * Borrado fisico. Por equipos.id_sistema_operativo (ON DELETE SET NULL),
     * los equipos que lo tuvieran asignado quedan sin SO, no se borran.
     */
    public void eliminar(Long id) {
        String sql = "DELETE FROM sistemas_operativos WHERE id = ?";

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error al eliminar sistema operativo", e);
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private SistemaOperativo mapear(ResultSet rs) throws SQLException {
        SistemaOperativo so = new SistemaOperativo();
        so.setIdSistemaOperativo(rs.getLong("id"));
        so.setTipo(rs.getString("tipo"));
        so.setNombre(rs.getString("nombre"));
        so.setVersionActual(rs.getString("version_actual"));
        return so;
    }
}