package vn.hsf.chat;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeminiClient {

    private final RestClient http;
    private final ObjectMapper json;
    private final String model;
    private final List<String> fallbackModels;

    public GeminiClient(RestClient aiRestClient, ObjectMapper json,
            @Value("${ai.gemini.model}") String model,
            @Value("${ai.gemini.fallback-models:gemini-flash-lite-latest,gemma-3-27b-it}") List<String> fallbackModels) {
        this.http = aiRestClient;
        this.json = json;
        this.model = model;
        this.fallbackModels = fallbackModels;
    }

    public String ask(String key, String prompt) {
        List<String> models = new ArrayList<>();
        models.add(model);
        fallbackModels.stream().filter(m -> !models.contains(m)).forEach(models::add);
        RestClientResponseException lastError = null;
        for (String candidate : models) {
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    return request(key, candidate, prompt);
                } catch (RestClientResponseException e) {
                    int code = e.getStatusCode().value();
                    if (code != 503 && code != 429 && code != 404) throw e;
                    lastError = e;
                    // 429 = het quota model nay, 404 = model khong ton tai -> sang model ke tiep ngay
                    if (code != 503) break;
                    if (attempt == 0) pauseBeforeRetry();
                }
            }
        }
        throw lastError;
    }

    private String request(String key, String model, String prompt) {
        byte[] body = http.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{m}:generateContent", model)
                .header("x-goog-api-key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))))
                .retrieve().body(byte[].class);
        return parse(body == null ? "" : new String(body, StandardCharsets.UTF_8)).at("/candidates/0/content/parts/0/text").asText("(AI không trả về nội dung)");
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Đã hủy khi chờ Gemini thử lại", e);
        }
    }

    private JsonNode parse(String body) {
        try {
            return json.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini trả về dữ liệu không phải JSON: " + body, e);
        }
    }
}
