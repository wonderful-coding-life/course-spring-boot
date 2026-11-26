package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.session.EventFilter;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, SessionService sessionService) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-5.6-terra")
                        .serviceTier("default")
                        .reasoningEffort("none"))
                .defaultAdvisors(SessionAdvisors.builder(sessionService)
                        .memory(memory -> memory.eventFilter(EventFilter.lastN(20)))
                        .build())
                .build();
    }
}
