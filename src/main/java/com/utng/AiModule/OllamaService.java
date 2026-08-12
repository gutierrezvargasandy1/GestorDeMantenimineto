package com.utng.AiModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utng.AiModule.OllamaModels.GenerateRequest;
import com.utng.AiModule.OllamaModels.GenerateResponse;
import com.utng.AiModule.OllamaModels.OllamaOptions;
import com.utng.config.OllamaConfig;

public class OllamaService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(OllamaConfig.timeoutSeconds()))
                .build();
    }

    // ================================================================
    // GENERATE SIMPLE (sin contexto de BD)
    // ================================================================

    /**
     * Llamada sincrona (bloqueante). Usar siempre dentro de un hilo/Task,
     * nunca en el hilo de JavaFX.
     */
    public String generate(String prompt) throws OllamaException {
        try {
            GenerateRequest body = buildRequest(prompt);
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OllamaConfig.baseUrl() + "/api/generate"))
                    .timeout(Duration.ofSeconds(OllamaConfig.timeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OllamaException("Ollama respondio con status "
                        + response.statusCode() + ": " + response.body());
            }

            GenerateResponse parsed = mapper.readValue(response.body(), GenerateResponse.class);
            return parsed.response != null ? parsed.response.trim() : "";

        } catch (IOException e) {
            throw new OllamaException("No se pudo conectar con Ollama en "
                    + OllamaConfig.baseUrl()
                    + ". Esta corriendo 'ollama serve'?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OllamaException("La solicitud a Ollama fue interrumpida.", e);
        }
    }

    /**
     * Version asincrona para usar desde controladores JavaFX sin bloquear la UI.
     */
    public CompletableFuture<String> generateAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> generate(prompt));
    }

    // ================================================================
    // GENERATE CON CONTEXTO DE BASE DE DATOS (RAG)
    // ================================================================

    /**
     * Envia la pregunta al modelo junto con el contexto real de la BD.
     * El contexto proviene de PromptContextBuilder.buildContext().
     *
     * @param pregunta   Pregunta original del usuario
     * @param contextoBD Datos reales de la BD construidos por PromptContextBuilder
     * @return Respuesta generada por el modelo
     */
    public String generateConContexto(String pregunta, String contextoBD) {

        String promptCompleto = construirPromptConContexto(pregunta, contextoBD);
        return generate(promptCompleto);
    }

    /**
     * Version asincrona con contexto.
     */
    public CompletableFuture<String> generateConContextoAsync(
            String pregunta, String contextoBD) {
        return CompletableFuture.supplyAsync(() -> generateConContexto(pregunta, contextoBD));
    }

    // ================================================================
    // CONSTRUCCION DEL PROMPT CON CONTEXTO
    // ================================================================

    /**
     * Construye el prompt completo que se envia a Ollama.
     *
     * Estructura:
     * [ROL DEL ASISTENTE]
     * [DATOS REALES DE LA BD]
     * [INSTRUCCIONES DE RESPUESTA]
     * [PREGUNTA DEL USUARIO]
     */
    private String construirPromptConContexto(String pregunta, String contextoBD) {

        if (contextoBD == null || contextoBD.isBlank()) {
            // Sin contexto: responde de forma generica
            return """
                    Eres Rocky, el asistente de soporte tecnico del CGTI.
                    Ayudas con preguntas sobre mantenimiento de equipos de computo.

                    Pregunta del usuario: %s

                    Responde de forma clara y concisa en espanol.
                    """.formatted(pregunta);
        }

        return """
                Eres Rocky, el asistente inteligente del area CGTI (Centro de Gestion
                de Tecnologia e Informacion). Tu unica fuente de verdad son los datos
                reales que te proporcionamos a continuacion. NO inventes informacion
                que no aparezca en estos datos.

                ═══════════════════════════════════════════
                DATOS REALES DE LA BASE DE DATOS (CGTI):
                ═══════════════════════════════════════════
                %s
                ═══════════════════════════════════════════

                INSTRUCCIONES DE RESPUESTA:
                - Responde SOLO con informacion de los datos anteriores.
                - Si la pregunta involucra un equipo especifico, menciona su modelo y ubicacion.
                - Si hay numeros o conteos, usa los datos exactos proporcionados.
                - Si el dato no aparece en el contexto, dilo claramente: "No tengo ese dato disponible."
                - Usa un tono profesional pero amigable en espanol.
                - Cuando recomiendes algo, explica brevemente en que dato te basas.
                - Si hay equipos en estado 'en_mantenimiento' o 'de_baja', mencionalo si es relevante.

                PREGUNTA DEL USUARIO:
                %s

                RESPUESTA:
                """.formatted(contextoBD, pregunta);
    }

    // ================================================================
    // CONSTRUCCION DEL REQUEST BASE
    // ================================================================

    private GenerateRequest buildRequest(String prompt) {
        OllamaOptions options = new OllamaOptions();
        options.temperature = OllamaConfig.temperature();
        options.topP = OllamaConfig.topP();
        options.topK = OllamaConfig.topK();
        options.numPredict = OllamaConfig.numPredict();
        options.numCtx = OllamaConfig.numCtx();

        GenerateRequest req = new GenerateRequest();
        req.model = OllamaConfig.model();
        req.prompt = prompt;
        req.system = OllamaConfig.systemPrompt();
        req.stream = OllamaConfig.stream(); // false = respuesta completa en un JSON
        req.options = options;
        return req;
    }

    // ================================================================
    // EXCEPCION PROPIA
    // ================================================================

    public static class OllamaException extends RuntimeException {
        public OllamaException(String message) {
            super(message);
        }

        public OllamaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}