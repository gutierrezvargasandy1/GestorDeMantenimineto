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

/** Repositorio de solo lectura para el catalogo sistemas_operativos. */
public class SistemaOperativoRepository {

    public List<SistemaOperativo> obtenerTodos() {
        String sql = "SELECT * FROM sistemas_operativos ORDER BY nombre, version_actual";
        List<SistemaOperativo> lista = new ArrayList<>();

        try (Connection con = ConectionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SistemaOperativo so = new SistemaOperativo();
                so.setIdSistemaOperativo(rs.getLong("id"));
                so.setTipo(rs.getString("tipo"));
                so.setNombre(rs.getString("nombre"));
                so.setVersionActual(rs.getString("version_actual"));
                lista.add(so);
            }
            return lista;

        } catch (SQLException e) {
            throw new AppException("Error al obtener sistemas operativos", e);
        }
    }
}