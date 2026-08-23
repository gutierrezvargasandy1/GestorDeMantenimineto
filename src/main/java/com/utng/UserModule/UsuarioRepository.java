package com.utng.UserModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;
import com.utng.config.ConectionDB;
import com.utng.util.AppException;

public class UsuarioRepository {

        // nuevo método dentro de la clase
        /**
         * Trae los usuarios activos de un rol especifico, para llenar
         * la lista de contactos del chat (consultores <-> tecnicos).
         */
        public List<Usuario> obtenerActivosPorRol(TipoUsuario rol) {

                String sql = """
                                SELECT *
                                FROM usuarios
                                WHERE rol = ? AND activo = TRUE
                                ORDER BY nombre_completo
                                """;

                List<Usuario> usuarios = new ArrayList<>();

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setObject(1, rol.getValor(), java.sql.Types.OTHER);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                usuarios.add(mapearUsuario(rs));
                        }

                        return usuarios;

                } catch (SQLException e) {
                        throw new AppException("Error al obtener usuarios por rol", e);
                }
        }

        // =========================
        // CREATE
        // =========================
        public void guardar(Usuario usuario) {

                String sql = """
                                INSERT INTO usuarios(
                                    nombre_completo,
                                    apellido_paterno,
                                    apellido_materno,
                                    correo,
                                    contrasena_hash,
                                    rol,
                                    codigo_recuperacion,
                                    intentos_recuperacion,
                                    requiere_recuperacion,
                                    fecha_codigo,
                                    activo
                                )
                                VALUES(?,?,?,?,?,?::rol_usuario,?,?,?,?,?)
                                RETURNING id, fecha_creacion
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, usuario.getNombreCompleto());
                        ps.setString(2, usuario.getApellidoPaterno());
                        ps.setString(3, usuario.getApellidoMaterno());
                        ps.setString(4, usuario.getCorreo());
                        ps.setString(5, usuario.getPassword());
                        ps.setString(6, usuario.getTipoUsuario().getValor());
                        ps.setString(7, usuario.getCodigoRecuperacion());
                        ps.setInt(8, usuario.getIntentosRecuperacion() == null ? 0 : usuario.getIntentosRecuperacion());
                        ps.setBoolean(9, Boolean.TRUE.equals(usuario.getRecuperacionActiva()));
                        ps.setTimestamp(10, usuario.getFechaCodigo());
                        ps.setBoolean(11, Boolean.TRUE.equals(usuario.getActivo()));

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        usuario.setIdUsuario(rs.getLong("id"));
                                        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                                }
                        }

                } catch (SQLException e) {
                        throw new AppException("Error al guardar usuario", e);
                }
        }

        public void reactivar(Long id) {

                String sql = """
                                UPDATE usuarios
                                SET activo = TRUE
                                WHERE id = ?
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setLong(1, id);
                        ps.executeUpdate();

                } catch (SQLException e) {
                        throw new AppException("Error al reactivar usuario", e);
                }
        }
        // =========================
        // READ BY ID
        // =========================

        public Usuario buscarPorId(Long id) {

                String sql = """
                                SELECT *
                                FROM usuarios
                                WHERE id = ?
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setLong(1, id);

                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {

                                return mapearUsuario(rs);

                        }

                        return null;

                } catch (SQLException e) {

                        throw new AppException(
                                        "Error al buscar usuario por id",
                                        e);

                }

        }

        // =========================
        // READ BY CORREO
        // =========================

        public Usuario buscarPorCorreo(String correo) {

                String sql = """
                                SELECT *
                                FROM usuarios
                                WHERE correo = ?
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, correo);

                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {

                                return mapearUsuario(rs);

                        }

                        return null;

                } catch (SQLException e) {

                        throw new AppException(
                                        "Error al buscar usuario por correo",
                                        e);

                }

        }

        // =========================
        // READ ALL
        // =========================

        public List<Usuario> obtenerTodos() {

                List<Usuario> usuarios = new ArrayList<>();

                String sql = """
                                SELECT *
                                FROM usuarios
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                                usuarios.add(
                                                mapearUsuario(rs));

                        }

                        return usuarios;

                } catch (SQLException e) {

                        throw new AppException(
                                        "Error al obtener usuarios",
                                        e);

                }

        }

        // =========================
        // UPDATE
        // =========================

        public void actualizar(Usuario usuario) {

                String sql = """
                                UPDATE usuarios
                                SET
                                    nombre_completo = ?,
                                    apellido_paterno = ?,
                                    apellido_materno = ?,
                                    correo = ?,
                                    contrasena_hash = ?,
                                    rol = ?,
                                    codigo_recuperacion = ?,
                                    intentos_recuperacion = ?,
                                    requiere_recuperacion = ?,
                                    fecha_codigo = ?,
                                    activo = ?

                                WHERE id = ?
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, usuario.getNombreCompleto());

                        ps.setString(2, usuario.getApellidoPaterno());

                        ps.setString(3, usuario.getApellidoMaterno());

                        ps.setString(4, usuario.getCorreo());

                        ps.setString(5, usuario.getPassword());

                        ps.setObject(
                                        6,
                                        usuario.getTipoUsuario().getValor(),
                                        java.sql.Types.OTHER);
                        ps.setString(7, usuario.getCodigoRecuperacion());

                        ps.setInt(8, usuario.getIntentosRecuperacion());

                        ps.setBoolean(9, usuario.getRecuperacionActiva());

                        ps.setTimestamp(10, usuario.getFechaCodigo());

                        ps.setBoolean(11, usuario.getActivo());

                        ps.setLong(12, usuario.getIdUsuario());

                        ps.executeUpdate();

                } catch (SQLException e) {

                        throw new AppException(
                                        "Error al actualizar usuario",
                                        e);

                }

        }

        // =========================
        // DELETE LOGICO
        // =========================

        public void eliminar(Long id) {

                String sql = """
                                UPDATE usuarios
                                SET activo = FALSE
                                WHERE id = ?
                                """;

                try (
                                Connection con = ConectionDB.conectar();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setLong(1, id);

                        ps.executeUpdate();

                } catch (SQLException e) {

                        throw new AppException(
                                        "Error al eliminar usuario",
                                        e);

                }

        }

        // =========================
        // MAPPER
        // =========================

        private Usuario mapearUsuario(ResultSet rs)
                        throws SQLException {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(
                                rs.getLong("id"));

                usuario.setNombreCompleto(
                                rs.getString("nombre_completo"));

                usuario.setApellidoPaterno(
                                rs.getString("apellido_paterno"));

                usuario.setApellidoMaterno(
                                rs.getString("apellido_materno"));

                usuario.setCorreo(
                                rs.getString("correo"));

                usuario.setPassword(
                                rs.getString("contrasena_hash"));

                usuario.setTipoUsuario(
                                TipoUsuario.fromValor(
                                                rs.getString("rol")));

                usuario.setCodigoRecuperacion(
                                rs.getString("codigo_recuperacion"));

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

}