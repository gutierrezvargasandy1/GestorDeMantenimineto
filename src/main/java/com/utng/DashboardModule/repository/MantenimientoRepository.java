package com.utng.DashboardModule.repository;

import com.utng.DashboardModule.model.MantenimientoModel;
import com.utng.config.ConectionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Repositorio adaptado al esquema REAL de PostgreSQL.
 *
 * Tablas que usa:
 * - registros_mantenimiento (el nombre real, no "mantenimientos")
 * - equipos (modelo y estado del equipo)
 * - usuarios (nombre del usuario responsable)
 *
 * Mapeo de ENUMs PostgreSQL a valores de la UI:
 * tipo: preventivo -> Preventivo
 * correctivo -> Correctivo
 *
 * estado: activo -> Activo
 * en_mantenimiento -> En mantenimiento
 * inactivo -> Inactivo
 * de_baja -> De baja
 */
public class MantenimientoRepository {

    private static final String QUERY_BASE = "SELECT " +
            "    e.modelo AS equipo, " +
            "    COALESCE(u.nombre_completo || ' ' || u.apellido_paterno, 'Sin usuario') AS usuario, " +
            "    rm.tipo::TEXT AS tipo_mantenimiento, " +
            "    rm.motivo, " +
            "    'CGTI' AS tecnico, " +
            "    TO_CHAR(COALESCE(rm.fecha_proxima, rm.fecha), 'DD/MM/YYYY') AS proxima_fecha, " +
            "    e.estado::TEXT AS estado " +
            "FROM registros_mantenimiento rm " +
            "JOIN equipos e ON rm.id_equipo = e.id " +
            "LEFT JOIN usuarios u ON e.id_usuario_responsable = u.id ";

    public static ObservableList<MantenimientoModel> obtenerTodos() {
        ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
        String query = QUERY_BASE + "ORDER BY rm.fecha_proxima ASC NULLS LAST";

        try (Connection conn = ConectionDB.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                datos.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener mantenimientos: " + e.getMessage());
            e.printStackTrace();
        }

        return datos;
    }

    public static ObservableList<MantenimientoModel> obtenerPorEstado(String estadoDisplay) {
        ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
        String estadoDB = displayAEstadoDB(estadoDisplay);
        String query = QUERY_BASE +
                "WHERE e.estado = ?::estado_equipo " +
                "ORDER BY rm.fecha_proxima ASC NULLS LAST";

        try (Connection conn = ConectionDB.conectar();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, estadoDB);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                datos.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al filtrar por estado: " + e.getMessage());
            e.printStackTrace();
        }

        return datos;
    }

    public static ObservableList<MantenimientoModel> obtenerPorTipo(String tipoDisplay) {
        ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
        String tipoDB = tipoDisplay.toLowerCase();
        String query = QUERY_BASE +
                "WHERE rm.tipo = ?::tipo_mantenimiento " +
                "ORDER BY rm.fecha_proxima ASC NULLS LAST";

        try (Connection conn = ConectionDB.conectar();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tipoDB);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                datos.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al filtrar por tipo: " + e.getMessage());
            e.printStackTrace();
        }

        return datos;
    }

    public static ObservableList<MantenimientoModel> buscar(String termino) {
        ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
        String query = QUERY_BASE +
                "WHERE e.modelo ILIKE ? OR u.nombre_completo ILIKE ? OR rm.motivo ILIKE ? " +
                "ORDER BY rm.fecha_proxima ASC NULLS LAST";

        try (Connection conn = ConectionDB.conectar();
                PreparedStatement ps = conn.prepareStatement(query)) {

            String patron = "%" + termino + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                datos.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar: " + e.getMessage());
            e.printStackTrace();
        }

        return datos;
    }

    public static int[] obtenerEstadisticas() {
        int[] stats = { 0, 0, 0, 0, 0 };
        String query = "SELECT " +
                "    COUNT(*) AS total, " +
                "    SUM(CASE WHEN estado = 'activo' THEN 1 ELSE 0 END) AS activos, " +
                "    SUM(CASE WHEN estado = 'en_mantenimiento' THEN 1 ELSE 0 END) AS en_mant, " +
                "    SUM(CASE WHEN estado = 'inactivo' THEN 1 ELSE 0 END) AS inactivos, " +
                "    SUM(CASE WHEN estado = 'de_baja' THEN 1 ELSE 0 END) AS de_baja " +
                "FROM equipos";

        try (Connection conn = ConectionDB.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("activos");
                stats[2] = rs.getInt("en_mant");
                stats[3] = rs.getInt("inactivos");
                stats[4] = rs.getInt("de_baja");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadisticas: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    public static int crear(MantenimientoModel m) {
        String queryEquipo = "SELECT id FROM equipos WHERE modelo = ? LIMIT 1";
        String queryInsert = "INSERT INTO registros_mantenimiento " +
                "(id_equipo, motivo, tipo, fecha_proxima, mantenimiento_realizado) " +
                "VALUES (?, ?, ?::tipo_mantenimiento, TO_TIMESTAMP(?, 'DD/MM/YYYY'), FALSE)";

        try (Connection conn = ConectionDB.conectar()) {
            long idEquipo = -1;
            try (PreparedStatement psEq = conn.prepareStatement(queryEquipo)) {
                psEq.setString(1, m.getEquipo());
                ResultSet rs = psEq.executeQuery();
                if (rs.next())
                    idEquipo = rs.getLong("id");
            }

            if (idEquipo == -1) {
                System.err.println("Equipo no encontrado: " + m.getEquipo());
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, idEquipo);
                ps.setString(2, m.getMotivo());
                ps.setString(3, m.getTipo().toLowerCase());
                ps.setString(4, m.getFecha());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("Mantenimiento creado con ID: " + id);
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al crear mantenimiento: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean eliminar(int id) {
        String query = "DELETE FROM registros_mantenimiento WHERE id = ?";
        try (Connection conn = ConectionDB.conectar();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok)
                System.out.println("Mantenimiento eliminado (ID: " + id + ")");
            return ok;

        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static int contarTotal() {
        String query = "SELECT COUNT(*) AS total FROM registros_mantenimiento";
        try (Connection conn = ConectionDB.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next())
                return rs.getInt("total");

        } catch (SQLException e) {
            System.err.println("Error al contar: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // ================================================================
    // HELPERS PRIVADOS
    // ================================================================

    private static MantenimientoModel mapearFila(ResultSet rs) throws SQLException {
        String fecha = rs.getString("proxima_fecha");
        return new MantenimientoModel(
                rs.getString("equipo"),
                rs.getString("usuario"),
                dbATipo(rs.getString("tipo_mantenimiento")),
                rs.getString("motivo"),
                rs.getString("tecnico"),
                fecha != null ? fecha : "Sin fecha",
                dbAEstadoDisplay(rs.getString("estado")));
    }

    private static String dbATipo(String db) {
        if (db == null)
            return "Sin tipo";
        switch (db.toLowerCase()) {
            case "preventivo":
                return "Preventivo";
            case "correctivo":
                return "Correctivo";
            default:
                return db;
        }
    }

    private static String dbAEstadoDisplay(String db) {
        if (db == null)
            return "Inactivo";
        switch (db.toLowerCase()) {
            case "activo":
                return "Activo";
            case "en_mantenimiento":
                return "En mantenimiento";
            case "inactivo":
                return "Inactivo";
            case "de_baja":
                return "De baja";
            default:
                return db;
        }
    }

    private static String displayAEstadoDB(String display) {
        if (display == null)
            return "activo";
        switch (display.toLowerCase()) {
            case "activo":
                return "activo";
            case "en mantenimiento":
                return "en_mantenimiento";
            case "inactivo":
                return "inactivo";
            case "de baja":
                return "de_baja";
            default:
                return display.toLowerCase();
        }
    }
}