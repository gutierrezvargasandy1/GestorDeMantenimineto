package com.utng.AiModule;

import com.utng.AiModule.data.MaintenanceContextRepository;
import com.utng.config.OllamaConfig;
import com.utng.util.AppException;

/**
 * Construye el contexto de base de datos que se inyecta al modelo IA.
 *
 * Analiza la pregunta del usuario y selecciona los datos más relevantes
 * de la BD para que el modelo responda con información real y completa.
 */
public class PromptContextBuilder {

    private final MaintenanceContextRepository repo;

    public PromptContextBuilder(MaintenanceContextRepository repo) {
        this.repo = repo;
    }

    /**
     * Analiza la pregunta y construye el contexto de BD adecuado.
     * Combina múltiples fuentes de datos cuando la pregunta es amplia.
     */
    public String buildContext(String pregunta) throws AppException {

        String p = pregunta.toLowerCase().trim();
        int maxRows = OllamaConfig.ragMaxRows();

        StringBuilder contexto = new StringBuilder();

        // ── EQUIPO ESPECIFICO ─────────────────────────────────────────
        // Detecta si preguntan por un equipo/ubicacion concreta
        boolean esEquipoEspecifico = mencionaEquipoEspecifico(p);

        if (esEquipoEspecifico) {
            String termino = extraerTerminoEquipo(p);
            contexto.append(repo.historialEquipo(termino, maxRows)).append("\n");
            // Tambien incluye resumen para contexto general
            contexto.append(repo.resumenGeneral()).append("\n");
        }

        // ── TODOS LOS EQUIPOS / INVENTARIO ───────────────────────────
        if (contiene(p, "todos los equipos", "lista de equipos", "inventario",
                "cuantos equipos", "que equipos", "equipos hay",
                "equipos tiene", "equipos activos", "dame todos")) {
            contexto.append(repo.resumenGeneral()).append("\n");
            contexto.append(repo.todosLosEquipos()).append("\n");
        }

        // ── REPORTE DEL MES ───────────────────────────────────────────
        if (contiene(p, "reporte", "mes", "mensual", "este mes",
                "del mes", "periodo", "resumen mensual")) {
            contexto.append(repo.reporteMesActual()).append("\n");
        }

        // ── TECNICOS / SERVICIOS ──────────────────────────────────────
        if (contiene(p, "tecnico", "tecnicos", "servicio por",
                "quien hizo", "quien realizo", "cuantos hizo",
                "cuantos servicios", "responsable")) {
            contexto.append(repo.serviciosPorTecnico(maxRows)).append("\n");
            contexto.append(repo.todosLosUsuarios()).append("\n");
        }

        // ── PROXIMOS / PENDIENTES ─────────────────────────────────────
        if (contiene(p, "proximo", "proximos", "pendiente", "pendientes",
                "programado", "programados", "cuando",
                "siguiente", "por hacer", "falta")) {
            contexto.append(repo.proximosMantenimientos(maxRows)).append("\n");
        }

        // ── FALLAS / CORRECTIVOS / RIESGO ────────────────────────────
        if (contiene(p, "falla", "fallas", "correctivo", "correctivos",
                "incidencia", "incidencias", "problema", "riesgo",
                "prediccion", "recomienda", "mas fallas",
                "equipo problematico")) {
            contexto.append(repo.equiposConMasIncidencias(maxRows)).append("\n");
            contexto.append(repo.todosLosMantenimientos(maxRows)).append("\n");
        }

        // ── PREVENTIVOS ───────────────────────────────────────────────
        if (contiene(p, "preventivo", "preventivos",
                "mantenimiento preventivo", "limpieza")) {
            contexto.append(repo.todosLosMantenimientos(maxRows)).append("\n");
        }

        // ── SOFTWARE / PROGRAMAS / INSTALACIONES ─────────────────────
        if (contiene(p, "software", "programa", "programas", "instalacion",
                "instalaciones", "version", "aplicacion",
                "office", "chrome", "firefox", "windows", "ubuntu",
                "sistema operativo", "driver")) {
            String termino = extraerTerminoEquipo(p);
            contexto.append(repo.programasInstalados(termino, maxRows)).append("\n");
        }

        // ── USUARIOS / PERSONAL ───────────────────────────────────────
        if (contiene(p, "usuario", "usuarios", "personal",
                "administrador", "correo", "quien tiene")) {
            contexto.append(repo.todosLosUsuarios()).append("\n");
        }

        // ── HISTORIAL EXPLICITO ───────────────────────────────────────
        if (contiene(p, "historial") && !esEquipoEspecifico) {
            String termino = extraerTerminoEquipo(p);
            contexto.append(repo.historialEquipo(termino, maxRows)).append("\n");
        }

        // ── FALLBACK: si no clasifica nada, pasa contexto completo ────
        if (contexto.length() == 0) {
            contexto.append(repo.resumenGeneral()).append("\n");
            contexto.append(repo.todosLosEquipos()).append("\n");
            contexto.append(repo.todosLosMantenimientos(maxRows)).append("\n");
            contexto.append(repo.proximosMantenimientos(10)).append("\n");
        }

        return contexto.toString();
    }

    // ================================================================
    // HELPERS PRIVADOS
    // ================================================================

    /** Devuelve true si la pregunta menciona un equipo o ubicacion especifica. */
    private boolean mencionaEquipoEspecifico(String p) {
        return contiene(p,
                // Marcas / modelos
                "dell", "hp", "lenovo", "acer", "asus",
                "optiplex", "thinkcentre", "thinkpad", "elitebook",
                "probook", "latitude", "precision", "aspire", "expertbook",
                "proLiant", "proliant",
                // Ubicaciones conocidas del esquema
                "oficina administrativa", "recursos humanos", "contabilidad",
                "ventas", "recepcion", "compras", "soporte", "servidor",
                "desarrollo", "direccion",
                // Frases que implican equipo especifico
                "historial de", "historial del", "del equipo", "ese equipo");
    }

    /**
     * Extrae el termino de busqueda mas util para historialEquipo().
     * Prioriza: marca/modelo > ubicacion > ultima palabra significativa.
     */
    private String extraerTerminoEquipo(String p) {

        // Modelos y marcas conocidas
        String[] modelos = {
                "dell", "hp", "lenovo", "acer", "asus",
                "optiplex", "thinkcentre", "thinkpad", "elitebook",
                "probook", "latitude", "precision", "aspire", "expertbook",
                "proliant"
        };
        for (String m : modelos) {
            if (p.contains(m))
                return m;
        }

        // Ubicaciones del esquema
        String[] ubicaciones = {
                "oficina administrativa", "recursos humanos",
                "contabilidad", "ventas", "recepcion", "compras",
                "soporte", "servidor", "desarrollo", "direccion"
        };
        for (String ub : ubicaciones) {
            if (p.contains(ub))
                return ub;
        }

        // Heuristica: ultima palabra significativa de la pregunta
        String[] palabras = p.replaceAll("[¿?.!,]", "").trim().split("\\s+");
        for (int i = palabras.length - 1; i >= 0; i--) {
            String w = palabras[i];
            if (w.length() > 4 && !esPalabraComun(w))
                return w;
        }

        // Sin termino identificable: busqueda amplia
        return "";
    }

    private boolean contiene(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p))
                return true;
        }
        return false;
    }

    private boolean esPalabraComun(String w) {
        String[] comunes = {
                "como", "cual", "cuales", "cuando", "donde", "quien",
                "tiene", "puede", "hacer", "mostrar", "dame", "dime",
                "quiero", "necesito", "equipo", "equipos", "mantenimiento",
                "mantenimientos", "registro", "registros", "informacion",
                "datos", "lista", "todos", "todas", "para", "este", "esta",
                "estos", "estas", "cuanto", "cuantos", "sobre", "acerca",
                "activos", "inactivos", "baja", "estado", "seria", "podrian"
        };
        for (String c : comunes) {
            if (w.equals(c))
                return true;
        }
        return false;
    }
}