package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Tag("integration")
@SpringBootTest
@Slf4j
class RagTests {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void testEmbedding() {
        String text = """
                [무선 청소기 사용 설명서]
                제품 사용 전 반드시 배터리를 완전히 충전해 주세요.
                전원 버튼을 3초간 길게 눌러 전원을 켜면 LED 표시등이 점등됩니다.
                먼지통이 가득 차면 흡입력이 떨어질 수 있으므로, 청소 후 반드시 먼지통과 필터를 세척하세요.
                필터는 완전히 건조된 후 다시 장착해야 하며, 젖은 상태로 사용할 경우 고장의 원인이 될 수 있습니다.
                배터리는 약 500회 충전이 가능합니다.
                """;

        float[] embedding = embeddingModel.embed(text);

        log.info("임베딩 차원 수: {}", embedding.length);
        log.info("임베딩 벡터: {}", Arrays.toString(embedding));
    }

    @Autowired
    private VectorStore vectorStore;

    private final List<String> texts = List.of(
            """
            [무선 청소기 사용 설명서]
            제품 사용 전 반드시 배터리를 완전히 충전해 주세요.
            전원 버튼을 3초간 길게 눌러 전원을 켜면 LED 표시등이 점등됩니다.
            먼지통과 필터는 청소 후 세척하고 완전히 건조해야 합니다.
            """,
            """
            [전자제품 A/S 및 교환 정책]
            본 제품은 구입일로부터 1년간 무상 보증이 제공됩니다.
            제품 수리는 서비스 센터 방문 또는 택배 접수가 가능합니다.
            교환 또는 환불은 구입 후 7일 이내에 가능합니다.
            """,
            """
            [온라인 쇼핑몰 반품 정책]
            상품은 수령 후 7일 이내에 반품할 수 있습니다.
            단순 변심으로 인한 반품은 왕복 배송비가 청구될 수 있습니다.
            제품 이상이나 오배송의 경우 배송비는 당사에서 부담합니다.
            """
    );

    @Test
    void testVectorStore() {
        List<Document> documents = texts.stream()
                .map(Document::new)
                .toList();

        vectorStore.write(documents);

        log.info("저장한 문서 수: {}", documents.size());
    }
}
