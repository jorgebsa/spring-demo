package com.example.kore.spring.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultPage<T> {

    private final List<T> content;

    private final int size;

    private final int number;

    private final int numberOfElements;

    private final long totalElements;

    private final int totalPages;

    private final boolean first;

    private final boolean last;

    private final boolean empty;

    @JsonCreator
    public ResultPage(@JsonProperty("content") List<T> content,
                      @JsonProperty("size") int size,
                      @JsonProperty("number") int number,
                      @JsonProperty("numberOfElements") int numberOfElements,
                      @JsonProperty("totalElements") long totalElements,
                      @JsonProperty("totalPages") int totalPages,
                      @JsonProperty("first") boolean first,
                      @JsonProperty("last") boolean last,
                      @JsonProperty("empty") boolean empty) {
        this.content = content;
        this.size = size;
        this.number = number;
        this.numberOfElements = numberOfElements;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.empty = empty;
    }

    public List<T> getContent() {
        return content;
    }

    public int getSize() {
        return size;
    }

    public int getNumber() {
        return number;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isEmpty() {
        return empty;
    }
}