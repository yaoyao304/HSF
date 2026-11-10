package vn.hsf.chat;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiHttpConfig {

    @Bean
    RestClient aiRestClient() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "ai-chat-rag/1.0")
                .build();
    }
}
