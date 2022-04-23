package com.rubenmathews.bytereactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class CompilationUnitTest {

    @Test
    void whenCloseIsCalled_shouldClearAllCompiledCodes() throws Exception {
        CompilationUnit compilationUnit = new CompilationUnit("test", "test", null);
        compilationUnit.addCompiledCode("test", new CompiledCode(""));
        Assertions.assertThat(compilationUnit.getCompiledClasses()).isNotEmpty();
        compilationUnit.close();
        Assertions.assertThat(compilationUnit.getCompiledClasses()).isEmpty();
    }
}
