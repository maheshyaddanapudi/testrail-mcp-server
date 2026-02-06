package io.github.testrail.mcp.registry;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service that maintains an in-memory Apache Lucene index of all tools registered
 * in the {@link InternalToolRegistry}.
 *
 * <p>At startup this service reads every {@link InternalToolRegistry.ToolDefinition}
 * and indexes its name, description, category, keywords, and examples into a
 * {@link ByteBuffersDirectory} (RAM-only index). Subsequent calls to
 * {@link #search(String, int)} execute a multi-field query against the index and
 * return ranked results.</p>
 *
 * <p>The search layer is intentionally decoupled from the registry: the registry
 * owns tool metadata and invocation handles, while this service owns the search
 * index and ranking logic.</p>
 */
@Service
public class LuceneToolIndexService {

    private static final Logger log = LoggerFactory.getLogger(LuceneToolIndexService.class);

    /** Lucene fields used in the index. */
    static final String FIELD_NAME = "name";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_CATEGORY = "category";
    static final String FIELD_KEYWORDS = "keywords";
    static final String FIELD_EXAMPLES = "examples";

    /** Fields that the multi-field query parser searches across. */
    private static final String[] SEARCH_FIELDS = {
            FIELD_NAME, FIELD_DESCRIPTION, FIELD_CATEGORY, FIELD_KEYWORDS, FIELD_EXAMPLES
    };

    /** Default maximum number of search results. */
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final InternalToolRegistry toolRegistry;
    private final Directory directory;
    private final StandardAnalyzer analyzer;

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public LuceneToolIndexService(InternalToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        this.directory = new ByteBuffersDirectory();
        this.analyzer = new StandardAnalyzer();
    }

    @PostConstruct
    public void init() {
        buildIndex();
        log.info("LuceneToolIndexService initialized — indexed {} tools", toolRegistry.getToolCount());
    }

    /**
     * Builds the Lucene index from all tools currently registered in the
     * {@link InternalToolRegistry}. Each tool becomes a single Lucene document
     * with searchable fields for name, description, category, keywords, and examples.
     */
    void buildIndex() {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (InternalToolRegistry.ToolDefinition tool : toolRegistry.getAllTools()) {
                Document doc = createDocument(tool);
                writer.addDocument(doc);
            }
            writer.commit();
        } catch (IOException e) {
            log.error("Failed to build Lucene index", e);
            throw new RuntimeException("Failed to build Lucene tool index", e);
        }

        openReader();
    }

    /**
     * Creates a Lucene {@link Document} from a {@link InternalToolRegistry.ToolDefinition}.
     *
     * <p>The tool name is stored as a {@link StringField} (exact match, not tokenized)
     * as well as a {@link TextField} (tokenized for search). Description, category,
     * keywords, and examples are stored as {@link TextField}s for full-text search.</p>
     */
    Document createDocument(InternalToolRegistry.ToolDefinition tool) {
        Document doc = new Document();

        // Name: stored as exact (for retrieval) and tokenized (for search)
        doc.add(new StringField(FIELD_NAME, tool.getName(), Field.Store.YES));
        doc.add(new TextField(FIELD_NAME + "_text", tool.getName().replace('_', ' '), Field.Store.NO));

        // Description: full-text searchable
        doc.add(new TextField(FIELD_DESCRIPTION, tool.getDescription(), Field.Store.NO));

        // Category: searchable
        doc.add(new TextField(FIELD_CATEGORY, tool.getCategory(), Field.Store.YES));

        // Keywords: joined into a single searchable text field
        String keywordsText = String.join(" ", tool.getKeywords());
        doc.add(new TextField(FIELD_KEYWORDS, keywordsText, Field.Store.NO));

        // Examples: joined into a single searchable text field
        String examplesText = String.join(" ", tool.getExamples());
        doc.add(new TextField(FIELD_EXAMPLES, examplesText, Field.Store.NO));

        return doc;
    }

    /**
     * Opens (or re-opens) the index reader and searcher after an index build or refresh.
     */
    private void openReader() {
        try {
            if (indexReader != null) {
                indexReader.close();
            }
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (IOException e) {
            log.error("Failed to open Lucene index reader", e);
            throw new RuntimeException("Failed to open Lucene index reader", e);
        }
    }

    /**
     * Searches the tool index for tools matching the given query string.
     * Returns up to {@code maxResults} results ranked by relevance.
     *
     * @param queryString the user's search query (natural language or keywords)
     * @param maxResults  maximum number of results to return
     * @return a list of {@link ToolSearchResult} ordered by descending relevance score
     */
    public List<ToolSearchResult> search(String queryString, int maxResults) {
        if (queryString == null || queryString.isBlank()) {
            return Collections.emptyList();
        }

        if (indexSearcher == null) {
            log.warn("Search called before index was built");
            return Collections.emptyList();
        }

        try {
            String sanitized = sanitizeQuery(queryString);
            if (sanitized.isEmpty()) {
                return Collections.emptyList();
            }

            MultiFieldQueryParser parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer);
            parser.setDefaultOperator(MultiFieldQueryParser.Operator.OR);
            Query query = parser.parse(sanitized);

            TopDocs topDocs = indexSearcher.search(query, maxResults);
            List<ToolSearchResult> results = new ArrayList<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = indexSearcher.storedFields().document(scoreDoc.doc);
                String toolName = doc.get(FIELD_NAME);
                String category = doc.get(FIELD_CATEGORY);

                InternalToolRegistry.ToolDefinition toolDef = toolRegistry.getTool(toolName);
                if (toolDef != null) {
                    results.add(new ToolSearchResult(
                            toolName,
                            category,
                            toolDef,
                            scoreDoc.score
                    ));
                }
            }

            return results;

        } catch (ParseException e) {
            log.warn("Failed to parse search query '{}': {}", queryString, e.getMessage());
            return Collections.emptyList();
        } catch (IOException e) {
            log.error("Error executing search query '{}'", queryString, e);
            return Collections.emptyList();
        }
    }

    /**
     * Convenience overload that uses the default maximum result count.
     */
    public List<ToolSearchResult> search(String queryString) {
        return search(queryString, DEFAULT_MAX_RESULTS);
    }

    /**
     * Sanitizes a raw query string by escaping or removing characters that are
     * special in Lucene query syntax, while preserving meaningful search terms.
     */
    String sanitizeQuery(String raw) {
        if (raw == null) {
            return "";
        }
        // Remove Lucene special characters that could cause parse errors
        // Keep alphanumeric, spaces, hyphens, and underscores
        String sanitized = raw.replaceAll("[+\\-!(){}\\[\\]^\"~*?:\\\\/]", " ");
        // Collapse multiple spaces
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        return sanitized;
    }

    /**
     * Returns the total number of documents in the index.
     */
    public int getIndexedToolCount() {
        return indexReader != null ? indexReader.numDocs() : 0;
    }

    // ── Result DTO ──────────────────────────────────────────────────────────

    /**
     * Immutable result object returned by {@link #search(String, int)}.
     * Contains the tool name, category, the full tool definition (with all
     * annotation metadata), and the Lucene relevance score.
     */
    public static class ToolSearchResult {
        private final String toolName;
        private final String category;
        private final InternalToolRegistry.ToolDefinition toolDefinition;
        private final float score;

        public ToolSearchResult(String toolName, String category,
                                InternalToolRegistry.ToolDefinition toolDefinition, float score) {
            this.toolName = toolName;
            this.category = category;
            this.toolDefinition = toolDefinition;
            this.score = score;
        }

        public String getToolName() { return toolName; }
        public String getCategory() { return category; }
        public InternalToolRegistry.ToolDefinition getToolDefinition() { return toolDefinition; }
        public float getScore() { return score; }
    }
}
