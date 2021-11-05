package com.github.jorgebsa.spring.demo.util;

import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.fasterxml.jackson.core.type.TypeReference;

public class ReferenceUtil {

    public static final TypeReference<ResultPage<NoteDTO>> NOTE_RESULT_PAGE_TYPE_REFERENCE = new TypeReference<>() {
    };

    private ReferenceUtil() {

    }

}
