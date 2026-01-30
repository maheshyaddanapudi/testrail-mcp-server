package io.github.testrail.mcp.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an internal tool that will be discovered automatically
 * and made available through the Lucene search system.
 * These tools are NOT exposed directly to MCP clients - only through
 * search_tools and execute_tool.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalTool {
    
    /**
     * Unique tool name. Defaults to method name converted to snake_case if not specified.
     * Example: "get_test_case", "add_result"
     */
    String name() default "";
    
    /**
     * Human-readable description explaining what this tool does.
     * This is indexed by Lucene for search and shown to LLMs.
     * Be descriptive - include when to use, what it returns, etc.
     */
    String description();
    
    /**
     * Category for grouping tools.
     * Examples: "test-cases", "test-runs", "test-results", "projects", "milestones"
     */
    String category();
    
    /**
     * Usage examples showing how to call this tool via execute_tool.
     * These are indexed for search and help LLMs understand parameter format.
     * Example: {"execute_tool('get_test_case', {caseId: 123})"}
     */
    String[] examples() default {};
    
    /**
     * Additional keywords to improve search relevance.
     * Include synonyms, related terms, and alternative phrasings.
     * Example: {"fetch", "retrieve", "read", "show", "view"}
     */
    String[] keywords() default {};
}
