package com.utng.AuthModule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.utng.AuthModule.model.Usuario.TipoUsuario;
import com.utng.AuthModule.model.Usuario.Usuario;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

public class AuthRepository {

    public Usuario obtenerPorCorreo(String correo) {

        String sql = """
                SELECT *
                FROM usuarios
                WHERE correo = ?
                """;

        try (
                Connection connection = ConectionDB.conectar();
                PreparedStatement ps = connection.prepareStatement(sql);) {

            ps.setString(1, correo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(rs.getLong("id"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setApellidoPaterno(rs.getString("apellido_paterno"));
                usuario.setApellidoMaterno(rs.getString("apellido_materno"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setPassword(rs.getString("contrasena_hash"));
                usuario.setTipoUsuario(
                        TipoUsuario.fromValor(
                                rs.getString("rol")));
                usuario.setCodigoRecuperacion(rs.getString("codigo_recuperacion"));
                usuario.setIntentosRecuperacion(
                        rs.getInt("intentos_recuperacion"));
                usuario.setRecuperacionActiva(
                        rs.getBoolean("requiere_recuperacion"));
                usuario.setFechaCodigo(
                        rs.getTimestamp("fecha_codigo"));
                usuario.setFechaCreacion(
                        rs.getTimestamp("fecha_creacion"));
                usuario.setActivo(
                        rs.getBoolean("activo"));

                return usuario;

            }

            return null;

        } catch (SQLException e) {

            throw new AppException(
                    "Error al consultar el usuario.",
                    e);

        }

    }

}
