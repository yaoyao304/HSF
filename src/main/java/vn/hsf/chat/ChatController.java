package vn.hsf.chat;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class ChatController {

    public record ChatRequest(Long sessionId, String message, String provider, boolean rag) {}

    private final ChatService chat;
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ObjectMapper json;

    @Value("${ai.provider}") private String defaultProvider;

    public ChatController(ChatService chat, ChatSessionRepository sessionRepo,
            ChatMessageRepository messageRepo, ObjectMapper json) {
        this.chat = chat;
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.json = json;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) Long sessionId, Model model) {
        model.addAttribute("provider", defaultProvider);
        model.addAttribute("sessions", sessionRepo.findAllByOrderByIdDesc());
        model.addAttribute("activeSessionId", sessionId);
        model.addAttribute("messages", sessionId != null
                ? messageRepo.findBySessionIdOrderByIdAsc(sessionId) : List.of());
        return "index";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest req) {
        try {
            String reply = chat.ask(req.provider(), req.message(), req.rag());
            ChatSession session = (req.sessionId() != null)
                    ? sessionRepo.findById(req.sessionId()).orElseGet(() -> createSession(req.message()))
                    : createSession(req.message());
            messageRepo.save(new ChatMessage(session, req.message(), reply, req.provider(), req.rag()));
            return ResponseEntity.ok(Map.of(
                    "reply", reply,
                    "sessionId", session.getId(),
                    "title", session.getTitle()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(e.getMessage())));
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            return ResponseEntity.status(status).body(Map.of("error", status + " " + upstreamMessage(e)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/sessions/{id}/delete")
    public String deleteSession(@PathVariable Long id) {
        sessionRepo.deleteById(id);
        return "redirect:/";
    }

    private ChatSession createSession(String firstQuestion) {
        return sessionRepo.save(new ChatSession(ChatSession.makeTitle(firstQuestion)));
    }

    /** Gemini va Groq deu tra loi dang {"error":{"message":...}}. */
    private String upstreamMessage(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        try {
            String msg = json.readTree(body).path("error").path("message").asText("");
            if (!msg.isBlank()) return msg;
        } catch (Exception ignored) {
            // body khong phai JSON -> tra nguyen van ben duoi
        }
        return body.isBlank() ? e.getStatusText() : body;
    }
}
