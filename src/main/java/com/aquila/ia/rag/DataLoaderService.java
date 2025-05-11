package com.aquila.ia.rag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataLoaderService {

    private final static String DONE_IMPORT = "imported-files.xml";

    @Value("${aquila.rag.location}")
    private String rootPath;

    private String importedFilename = null;

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    private final Properties imported = new Properties();
    private File importedFile = null;

    @PostConstruct
    void postConstruct() {
        importedFilename = rootPath + "/" + DONE_IMPORT;
        importedFile = new File(importedFilename);
    }

    public void load() {
        try (InputStream in = new FileInputStream(importedFile)) {
            imported.loadFromXML(in);
        } catch (IOException e) {
            log.warn("Exception:{} when reading imported properties:{}", e.getLocalizedMessage(), importedFilename);
        }
        load(new File(rootPath));
        log.info("load of document done in {}. You can start to send request ...", rootPath);
    }

    private void load(File file) {
        log.info("load dir({})", file);
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(
                subFile -> {
                    final String filename = subFile.toString();
                    load(subFile, filename);
                }
        );
    }

    private void load(File subFile, String filename) {
        final String key = filename; // .replace('\\', '_');
        if (!imported.containsKey(key) && subFile.canRead() && !filename.equals(importedFile.toString())) {
            if (subFile.isDirectory()) load(subFile);
            else {
                try {
                    List<Document> documents = switch (filename.substring(filename.lastIndexOf('.'))) {
                        case ".html" -> loadHtml(filename);
                        case ".pdf" -> loadPdf(filename);
                        case ".jar" -> null;
                        default -> {
                            log.warn("using Tika reader for file:{}", filename);
                            yield loadTika(filename);
                        }
                    };
                    if (documents != null) {
                        final String value = String.format("Nb Documents:%d - File size:%d - Date:%s", documents.size(), subFile.length(), new Date());
                        imported.put(key, value);
                        saveImportedList();
                        log.info("importing {} documents into vector DB file: {}", documents.size(), filename);
                        this.vectorStore.accept(tokenTextSplitter.apply(documents));
                        log.info("importation of file: {} done.", filename);
                    }
                } catch (Throwable t) {
                    log.error(String.format("Can not process file:%s, skipped", filename), t);
                }
            }
        } else log.error("skipping file/dir:{}", subFile);
    }

    private List<Document> loadPdf(String filename) {
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("file://" + filename,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(0)
                                .withNumberOfTopPagesToSkipBeforeDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        return tokenTextSplitter.apply(pdfReader.get());
    }

    /**
     * @return list of documents extracted from an HTML
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
        return tokenTextSplitter.apply(reader.get());
    }

    /**
     * @return list of documents extracted from any Tika supported format:<br/>
     * <a href="https://tika.apache.org/3.1.0/formats.html">Tika Format</a>
     */
    private List<Document> loadTika(String filename) {
        TikaDocumentReader tikaReader = new TikaDocumentReader("file://" + filename);
        return tokenTextSplitter.apply(tikaReader.get());
    }

    private void saveImportedList() {
        try (OutputStream out = new FileOutputStream(importedFilename)) {
            imported.storeToXML(out, "IA-RAG imported documents");
            out.flush();
        } catch (IOException e) {
            log.warn("Exception:{} when writing imported properties:{}", e.getLocalizedMessage(), importedFilename);
        }
    }
}
