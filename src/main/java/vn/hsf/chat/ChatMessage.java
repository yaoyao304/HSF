package vn.hsf.chat;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String question;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String answer;

    @Column(length = 20)
    private String provider;

    private boolean rag;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {}

    public ChatMessage(ChatSession session, String question, String answer, String provider, boolean rag) {
        this.session = session;
        this.question = question;
        this.answer = answer;
        this.provider = provider;
        this.rag = rag;
    }

    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public String getProvider() { return provider; }
    public boolean isRag() { return rag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
