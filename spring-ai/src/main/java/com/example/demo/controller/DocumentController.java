package com.example.demo.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class DocumentController {

    @Autowired
    private VectorStore vectorStore;

    @PostMapping("/api/documents/pdf")
    public String uploadPdf(@RequestParam("file") MultipartFile file) {
        // PDF 파일 읽기
        DocumentReader reader = new PagePdfDocumentReader(file.getResource());
        List<Document> documents = reader.read();

        // Custom Metadata 추가
        documents.forEach(document -> {
            document.getMetadata().put("docType", "pdf");
            document.getMetadata().put("businessCategory", "return-policy");
            document.getMetadata().put("sourceFile", file.getOriginalFilename());
        });

        // 벡터데이터베이스에 저장
        vectorStore.write(documents);

        return "저장한 문서 수: " + documents.size();
    }

    @PostMapping("/api/documents/markdown")
    public String uploadMarkdown(@RequestParam("file") MultipartFile file) {
        // Markdown 파일 읽기
        DocumentReader reader = new MarkdownDocumentReader(
                file.getResource(),
                MarkdownDocumentReaderConfig.defaultConfig()
        );
        List<Document> documents = reader.read();

        // Custom Metadata 추가
        documents.forEach(document -> {
            document.getMetadata().put("docType", "markdown");
            document.getMetadata().put("businessCategory", "return-policy");
            document.getMetadata().put("sourceFile", file.getOriginalFilename());
        });

        // 벡터데이터베이스에 저장
        vectorStore.write(documents);

        return "저장한 문서 수: " + documents.size();
    }

    @GetMapping("/api/documents/search")
    public List<Document> search(@RequestParam("question") String question) {
        return vectorStore.similaritySearch(question);
    }

    @GetMapping("/api/documents/search/filtered")
    public List<Document> searchFiltered(@RequestParam("question") String question) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .similarityThreshold(0.7)
                .topK(2)
                .filterExpression("docType == 'markdown'")
                .build());
    }
}
