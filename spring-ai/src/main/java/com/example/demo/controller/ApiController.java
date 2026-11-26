package com.example.demo.controller;

import com.example.demo.dto.ProductRequest;
import com.example.demo.tool.KnowledgeSearchTool;
import com.example.demo.tool.ProductOrderTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private ChatModel chatModel;

    @PostMapping("/api/chat-model/chats")
    public String postChatModel(@RequestBody String message) {
        return chatModel.call(message);
    }

    @Autowired
    private ChatClient.Builder builder;

    @PostMapping("/api/chat-client/chats")
    public String postChatClient(@RequestBody String message) {
        var simpleChatClient = builder.build();
        return simpleChatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    private final String systemMessage = """
                너는 전문 마케팅 카피라이터야.
                입력된 제품 정보를 기반으로 온라인 쇼핑몰/블로그/홍보 페이지에 사용할 매력적인 마케팅 문구를 작성해 줘.
                작성 조건
                1. 소비자의 관심을 끌 수 있도록 첫 문장은 강렬하거나 공감 가는 표현을 사용해.
                2. 제품 특징을 자연스럽게 녹여서 장점이 잘 드러나게 작성해.
                3. 가격과 구매 링크는 구매를 자극하는 문구와 함께 포함시켜.
                   - 예: "지금 2,400원에 만나보세요  http://example.com/p123"
                4. 글자 수는 약 300~500자로 하고, 캐주얼하지만 설득력 있는 톤으로 작성해.
                5. 필요하면 감각적인 이모지도 활용해.
            """;

    private final String userMessage = """
                입력 정보
                - 제품명: {name}
                - 가격: {price}
                - 구매 링크: {link}
                - 제품 특징: {features}
            """;

    @PostMapping("/api/marketing")
    public String postMarketing(@RequestBody ProductRequest product) {
        var simpleChatClient = builder.build();
        return simpleChatClient.prompt()
                .system(systemMessage)
                .user(user -> user.text(userMessage)
                        .param("name", product.getName())
                        .param("price", product.getPrice())
                        .param("link", product.getLink())
                        .param("features", product.getFeatures()))
                .call()
                .content();
    }

    @PostMapping("/api/chat-options/chats")
    public String postChatOptions(@RequestBody String message) {
        var simpleChatClient = builder.build();
        return simpleChatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model("gpt-5.6-terra")
                        .serviceTier("default")
                        .reasoningEffort("none"))
                .user(message)
                .call()
                .content();
    }

    @Autowired
    private ChatClient chatClient;

    @PostMapping("/api/session/chats")
    public String postAdvisorChat(@RequestBody String message,
                                  @RequestParam("sessionId") String sessionId) {
        return chatClient.prompt()
                .system("질문에 짧고 명료하게 답변해 줘.")
                .advisors(SessionAdvisors.session(sessionId))
                .user(message)
                .call()
                .content();
    }

    @Autowired
    private SessionService sessionService;

    @DeleteMapping("/api/session/chats")
    public void deleteSession(@RequestParam("sessionId") String sessionId) {
        sessionService.delete(sessionId);
    }

    @Autowired
    private VectorStore vectorStore;

    @PostMapping("/api/chat-rag/chats")
    public String postRag(@RequestBody String message,
                          @RequestParam("sessionId") String sessionId) {
        return chatClient.prompt()
                .advisors(SessionAdvisors.session(sessionId))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.7)
                                .topK(2)
                                .filterExpression("docType == 'markdown'")
                                .build())
                        .build())
                .user(message)
                .call()
                .content();
    }

    @Autowired
    private ProductOrderTool productOrderTool;

    private static final String customerSupportSystemMessage = """
        당신은 캠퍼스 쇼핑몰의 고객지원 상담사입니다.
        다음 원칙에 따라 답변하세요.
        1. 확인된 사실에 근거하여 정확하게 답변합니다.
        2. 제공된 정보만으로 답변할 수 없거나 확실하지 않은 내용은 추측하지 않습니다.
        3. 답변하기 어려운 문의는 고객센터 02-500-5000으로 안내합니다.
        4. 답변은 짧고 명확하게 작성합니다.
        5. 답변은 순수 텍스트(Plain Text) 형식으로 작성하며, Markdown 문법은 사용하지 않습니다.
        6. 사용자가 인사만 하거나 구체적인 문의 없이 말을 건 경우에는 간단히 인사한 뒤 문의 내용을 요청합니다.
        7. 문의 내용을 요청할 때는 문의 유형이나 예시를 나열하지 않습니다.
        """;

    @PostMapping("/api/chat-tools/chats")
    public String postToolChat(@RequestBody String message,
                               @RequestParam("sessionId") String sessionId,
                               @RequestParam("username") String username) {
        return chatClient.prompt()
                .system(customerSupportSystemMessage)
                .advisors(SessionAdvisors.session(sessionId))
                .tools(productOrderTool)
                .toolContext(Map.of("username", username))
                .user(message)
                .call()
                .content();
    }

    @Autowired
    private KnowledgeSearchTool knowledgeSearchTool;

    @PostMapping("/api/chat-tool-rag/chats")
    public String postToolRag(@RequestBody String message,
                              @RequestParam("sessionId") String sessionId,
                              @RequestParam("username") String username) {
        return chatClient.prompt()
                .system(customerSupportSystemMessage + """
                        반품 문의 처리 원칙
                        1. 반품 정책에 관한 질문은 지식 검색 도구를 호출하고 그 결과를 근거로 답변합니다.
                        2. 검색 결과에 필요한 정보가 없으면 추측하지 말고 고객센터로 안내합니다.
                        """)
                .advisors(SessionAdvisors.session(sessionId))
                .tools(productOrderTool, knowledgeSearchTool)
                .toolContext(Map.of("username", username))
                .user(message)
                .call()
                .content();
    }

    @Autowired
    private JsonMapper jsonMapper;

    @PostMapping(value = {"/api/chat-stream/chats", "/chats"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> postStreamChat(@RequestBody String message,
                                       @RequestParam("sessionId") String sessionId,
                                       @RequestParam("username") String username) {
        return chatClient.prompt()
                .system(customerSupportSystemMessage + """
                        반품 문의 처리 원칙
                        1. 반품 정책에 관한 질문은 지식 검색 도구를 호출하고 그 결과를 근거로 답변합니다.
                        2. 검색 결과에 필요한 정보가 없으면 추측하지 말고 고객센터로 안내합니다.
                        """)
                .advisors(SessionAdvisors.session(sessionId))
                .tools(productOrderTool, knowledgeSearchTool)
                .toolContext(Map.of("username", username))
                .user(message)
                .stream()
                .content()
                .map(jsonMapper::writeValueAsString);
    }
}
