package com.rubenmathews.bytereactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class RuntimeCompilerTest {

    @Test
    void whenCompilerImplementationIsNoProvidedShouldUseDefaultClearCache(){
        RuntimeCompiler runtimeCompiler = getTestRuntimeCompilerWithNoCache();
        Assertions.assertThatCode(runtimeCompiler::clearCache).doesNotThrowAnyException();
    }

    private RuntimeCompiler getTestRuntimeCompilerWithNoCache() {
        return new RuntimeCompiler() {
            @Override
            public Class<?> loadClass(CompilationRequest compilationRequest) {
                return null;
            }

            @Override
            public Class<?> loadClass(CompilationRequest compilationRequest, ClassLoader classLoader) {
                return null;
            }

            @Override
            public Map<String, Class<?>> loadClasses(CompilationRequestBatch compilationRequestBatch) {
                return null;
            }

            @Override
            public Map<String, Class<?>> loadClasses(CompilationRequestBatch compilationRequestBatch, ClassLoader classLoader) {
                return null;
            }

            @Override
            public void close() {

            }
        };
    }
}
