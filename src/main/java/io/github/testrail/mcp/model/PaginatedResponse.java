package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic wrapper for paginated TestRail API responses.
 *
 * @param <T> the type of items in the response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedResponse<T> {

    private Integer offset;
    private Integer limit;
    private Integer size;

    @JsonProperty("_links")
    private Links links;

    // The actual items - field name varies by endpoint
    private List<T> items;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * Returns whether there are more results available.
     *
     * @return true if there are more results
     */
    public boolean hasMore() {
        return links != null && links.getNext() != null;
    }

    /**
     * Pagination links.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private String next;
        private String prev;

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrev() {
            return prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }
    }
}
