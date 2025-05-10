package com.aquila.ia.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataLoaderService {

    @Value("${aquila.rag.location}")
    private String rootPath;

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    public void load() {
        load(new File(rootPath));
        log.info("load of document done in {}. You can start to send request ...", rootPath);
    }

    private void load(File file) {
        log.info("load({})", file);
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(
                subFile -> {
                    if (subFile.canRead()) {
                        if (subFile.isDirectory()) load(subFile);
                        else {
                            final String filename = subFile.toString();
                            List<Document> documents = switch (filename.substring(filename.lastIndexOf('.'))) {
                                case ".html" -> loadHtml(filename);
                                case ".pdf" -> loadPdf(filename);
                                default -> {
                                    log.warn("file not supported: {}", filename);
                                    yield null;
                                }
                            };
                            if (documents != null) {
                                log.info("importing into vector DB file: {}", filename);
                                this.vectorStore.accept(tokenTextSplitter.apply(documents));
                                log.info("importation of file: {} done.", filename);
                            }
                        }
                    } else log.error("Can not read file/dir:{}", subFile);
                }
        );
    }

    private List<Document> loadPdf(String filename) {
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("file://"+filename,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        return tokenTextSplitter.apply(pdfReader.get());
    }

    /**
     * @return list of documents
     */
    private List<Document> loadHtml(String filename) {
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("body") // Extract paragraphs within <article> tags
                .charset("ISO-8859-1")  // Use ISO-8859-1 encoding
                .includeLinkUrls(true) // Include link URLs in metadata
                .metadataTags(List.of("author", "date")) // Extract author and date meta tags
                .additionalMetadata("source", filename) // Add custom metadata
                .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(filename, config);
        return reader.get();
    }

    @Bean
    public VectorStore getVector() {
        return vectorStore;
    }
}
