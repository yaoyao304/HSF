package vn.hsf.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String title;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatSession() {}

    public ChatSession(String title) {
        this.title = title;
    }

    public static String makeTitle(String firstQuestion) {
        String s = firstQuestion == null ? "" : firstQuestion.strip().replaceAll("\\s+", " ");
        return s.length() > 35 ? s.substring(0, 35) + "..." : s;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ChatMessage> getMessages() { return messages; }
}
