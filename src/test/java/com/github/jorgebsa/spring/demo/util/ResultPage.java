package com.github.jorgebsa.spring.demo.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultPage<T>(List<T> content,
                            int size,
                            int number,
                            int numberOfElements,
                            long totalElements,
                            int totalPages,
                            boolean first,
                            boolean last,
                            boolean empty) {

}