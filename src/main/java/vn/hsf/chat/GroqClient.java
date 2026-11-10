package vn.hsf.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private final RestClient http;
    private final ObjectMapper json;
    private volatile String model;

    public GroqClient(RestClient aiRestClient, ObjectMapper json,
            @Value("${ai.groq.model}") String model) {
        this.http = aiRestClient;
        this.json = json;
        this.model = model;
    }

    public String ask(String key, String prompt) {
        try {
            return post(key, model, prompt);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 404 && e.getStatusCode().value() != 400) throw e;
            model = findChatModel(key);
            return post(key, model, prompt);
        }
    }

    private String post(String key, String model, String prompt) {
        byte[] body = http.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + key)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", "Luôn trả lời bằng tiếng Việt."),
                                Map.of("role", "user", "content", prompt))))
                .retrieve().body(byte[].class);
        return parse(body).at("/choices/0/message/content").asText("(AI không trả về nội dung)");
    }

    private String findChatModel(String key) {
        byte[] body = http.get()
                .uri("https://api.groq.com/openai/v1/models")
                .header("Authorization", "Bearer " + key)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(byte[].class);
        String fallback = null;
        for (JsonNode m : parse(body).path("data")) {
            String id = m.path("id").asText();
            String lower = id.toLowerCase();
            if (lower.contains("whisper") || lower.contains("guard") || lower.contains("tts")
                    || lower.contains("orpheus") || lower.contains("embed") || lower.contains("allam")) {
                continue;
            }
            if (lower.contains("llama") || lower.contains("openai") || lower.contains("qwen") || lower.contains("gemma")) {
                return id;
            }
            if (fallback == null) fallback = id;
        }
        if (fallback != null) return fallback;
        throw new IllegalStateException("Không tìm thấy model chat nào khả dụng trên tài khoản Groq");
    }

    private JsonNode parse(byte[] bytes) {
        String body = bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
        try {
            return json.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Groq trả về dữ liệu không phải JSON: " + body, e);
        }
    }
}
