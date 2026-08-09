package com.utng.AiModule;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OllamaModels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OllamaOptions {
        @JsonProperty("temperature") public double temperature;
        @JsonProperty("top_p")       public double topP;
        @JsonProperty("top_k")       public int topK;
        @JsonProperty("num_predict") public int numPredict;
        @JsonProperty("num_ctx")     public int numCtx;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerateRequest {
        public String model;
        public String prompt;
        public String system;
        public boolean stream;
        public OllamaOptions options;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerateResponse {
        public String model;
        public String response;
        public boolean done;
    }
}