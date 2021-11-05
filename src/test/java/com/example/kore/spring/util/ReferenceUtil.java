package com.example.kore.spring.util;

import com.example.kore.spring.base.NoteDTO;
import com.fasterxml.jackson.core.type.TypeReference;

public class ReferenceUtil {

    public static final TypeReference<ResultPage<NoteDTO>> NOTE_RESULT_PAGE_TYPE_REFERENCE = new TypeReference<>() {
    };

    private ReferenceUtil() {

    }

}
