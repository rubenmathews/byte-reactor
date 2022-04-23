package com.rubenmathews.bytereactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CompilationRequestCollectionTest {

    @Test
    void whenCloseIsCalled_shouldClearAllCompilationUnit() {
        CompilationRequestBatch compilationRequestBatch = CompilationRequestBatch
                .builder()
                .add("test", "test")
                .build();
        Assertions.assertThat(compilationRequestBatch.getCollection()).isNotEmpty();
        compilationRequestBatch.close();
        Assertions.assertThat(compilationRequestBatch.getCollection()).isEmpty();
    }
}
