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

    /**
     * Llamada síncrona (bloqueante). Úsala siempre dentro de un hilo/Task, nunca en
     * el hilo de JavaFX.
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

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OllamaException("Ollama respondió con status " + response.statusCode()
                        + ": " + response.body());
            }

            GenerateResponse parsed = mapper.readValue(response.body(), GenerateResponse.class);
            return parsed.response != null ? parsed.response.trim() : "";

        } catch (IOException e) {
            throw new OllamaException("No se pudo conectar con Ollama en " + OllamaConfig.baseUrl()
                    + ". ¿Está corriendo 'ollama serve'?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OllamaException("La solicitud a Ollama fue interrumpida.", e);
        }
    }

    /**
     * Versión asíncrona para usar directamente desde controladores JavaFX sin
     * bloquear la UI.
     */
    public CompletableFuture<String> generateAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> generate(prompt));
    }

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
        req.stream = OllamaConfig.stream(); // false = respuesta completa en un solo JSON
        req.options = options;
        return req;
    }

    public static class OllamaException extends RuntimeException {
        public OllamaException(String message) {
            super(message);
        }

        public OllamaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String generateConContexto(String pregunta, String contextoBD) {
        String promptCompleto = """
                Aquí tienes datos reales de la base de datos de mantenimiento (úsalos como única fuente de verdad, no inventes datos que no estén aquí):

                %s

                Pregunta del usuario: %s

                Responde basándote solo en los datos anteriores. Si haces una recomendación o predicción, explica brevemente en qué dato te basas.
                """
                .formatted(contextoBD, pregunta);

        return generate(promptCompleto);
    }
}