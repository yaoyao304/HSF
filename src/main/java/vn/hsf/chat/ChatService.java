package vn.hsf.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final RagService rag;
    private final GeminiClient gemini;
    private final GroqClient groq;
    private final String geminiKey;
    private final String groqKey;

    public ChatService(RagService rag, GeminiClient gemini, GroqClient groq,
            @Value("${ai.gemini.api-key}") String geminiKey,
            @Value("${ai.groq.api-key}") String groqKey) {
        this.rag = rag;
        this.gemini = gemini;
        this.groq = groq;
        this.geminiKey = geminiKey;
        this.groqKey = groqKey;
    }

    /** Key chi doc tu .env / application.properties. */
    public String ask(String provider, String message, boolean useRag) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Tin nhắn trống");
        boolean useGroq = "groq".equals(provider);
        String key = clean(useGroq ? groqKey : geminiKey);
        if (key.isEmpty() || key.startsWith("put_new_")) {
            throw new IllegalArgumentException("Chưa cấu hình API key trong file .env");
        }

        String prompt = message;
        if (useRag) {
            String context = rag.retrieve(message);
            if (context.isEmpty()) context = "(không tìm thấy đoạn nào liên quan)";
            prompt = "Chỉ trả lời dựa trên TÀI LIỆU dưới đây. Nếu tài liệu không có thông tin thì nói rõ là không tìm thấy.\n\n"
                    + "TÀI LIỆU:\n" + context + "\n\nCÂU HỎI: " + message;
        }
        return useGroq ? groq.ask(key, prompt) : gemini.ask(key, prompt);
    }

    // key copy-paste hay dinh khoang trang / \r\n -> server tra 401
    static String clean(String key) {
        return key == null ? "" : key.replaceAll("\\s", "");
    }
}
