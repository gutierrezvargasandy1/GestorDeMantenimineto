package com.utng.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class OllamaConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // no truena si no existe el .env, usa defaults
            .load();

    private OllamaConfig() {
    }

    public static String baseUrl() {
        return get("OLLAMA_BASE_URL", "http://localhost:11434");
    }

    public static String model() {
        return get("OLLAMA_MODEL", "llama3");
    }

    public static double temperature() {
        return Double.parseDouble(get("OLLAMA_TEMPERATURE", "0.7"));
    }

    public static double topP() {
        return Double.parseDouble(get("OLLAMA_TOP_P", "0.9"));
    }

    public static int topK() {
        return Integer.parseInt(get("OLLAMA_TOP_K", "40"));
    }

    public static int numPredict() {
        return Integer.parseInt(get("OLLAMA_NUM_PREDICT", "512"));
    }

    public static int numCtx() {
        return Integer.parseInt(get("OLLAMA_NUM_CTX", "4096"));
    }

    public static int timeoutSeconds() {
        return Integer.parseInt(get("OLLAMA_TIMEOUT_SECONDS", "60"));
    }

    public static boolean stream() {
        return Boolean.parseBoolean(get("OLLAMA_STREAM", "false"));
    }

    public static String systemPrompt() {
        return get("OLLAMA_SYSTEM_PROMPT",
                "Eres el asistente del CGTI. Respondes de forma breve y clara.");
    }

    /** Prioriza variables de entorno del sistema sobre el archivo .env. */
    private static String get(String key, String defaultValue) {
        String sysEnv = System.getenv(key);
        if (sysEnv != null && !sysEnv.isBlank())
            return sysEnv;

        String fileEnv = dotenv.get(key);
        return (fileEnv != null && !fileEnv.isBlank()) ? fileEnv : defaultValue;
    }

    public static boolean ragEnabled() {
        return Boolean.parseBoolean(get("OLLAMA_RAG_ENABLED", "true"));
    }

    public static int ragMaxRows() {
        return Integer.parseInt(get("OLLAMA_RAG_MAX_ROWS", "25"));
    }
}