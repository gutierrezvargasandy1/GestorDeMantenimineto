package com.utng.AiModule;

import com.utng.AiModule.data.MaintenanceContextRepository;
import com.utng.config.OllamaConfig;

public class PromptContextBuilder {

    private final MaintenanceContextRepository repo;

    public PromptContextBuilder(MaintenanceContextRepository repo) {
        this.repo = repo;
    }

    public String buildContext(String pregunta) {

        String p = pregunta.toLowerCase();

        int maxRows = OllamaConfig.ragMaxRows();

        StringBuilder contexto = new StringBuilder();

        if (p.contains("reporte") && p.contains("mes")) {

            contexto.append(repo.reporteMesActual()).append("\n");

        }

        if (p.contains("técnico") || p.contains("tecnico")) {

            contexto.append(repo.serviciosPorTecnico(maxRows)).append("\n");

        }

        if (p.contains("riesgo") || p.contains("falla") || p.contains("predicción")
                || p.contains("prediccion") || p.contains("consejo") || p.contains("recomienda")) {

            contexto.append(repo.equiposConMasIncidencias(maxRows)).append("\n");

        }

        if (p.contains("próximo") || p.contains("proximo") || p.contains("programado")) {

            contexto.append(repo.proximosMantenimientos(maxRows)).append("\n");

        }

        if (p.contains("software") || p.contains("programa") || p.contains("instalación")
                || p.contains("instalacion")) {

            String termino = extraerPosibleEquipo(pregunta);
            contexto.append(repo.programasInstalados(termino, maxRows)).append("\n");

        }

        if (p.contains("historial")) {

            String termino = extraerPosibleEquipo(pregunta);
            contexto.append(repo.historialEquipo(termino, maxRows)).append("\n");

        }

        if (contexto.isEmpty()) {

            contexto.append(repo.resumenGeneral()).append("\n");

        }

        return contexto.toString();

    }

    /**
     * Heurística simple: toma la última palabra "significativa" de la pregunta
     * como término de búsqueda de modelo/lugar. Si tienes un ComboBox o lista de
     * equipos en tu UI, es mejor pedir al usuario que seleccione el equipo en vez
     * de adivinar por texto libre.
     */
    private String extraerPosibleEquipo(String pregunta) {

        String[] palabras = pregunta.replaceAll("[¿?]", "").trim().split("\\s+");

        return palabras.length > 0 ? palabras[palabras.length - 1] : pregunta;

    }

}