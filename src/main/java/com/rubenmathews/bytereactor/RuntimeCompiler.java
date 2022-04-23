package com.rubenmathews.bytereactor;

import java.util.Map;

public interface RuntimeCompiler {

  Class<?> loadClass(CompilationRequest compilationRequest);

  Class<?> loadClass(CompilationRequest compilationRequest,
                     ClassLoader classLoader);

  Map<String, Class<?>> loadClasses(CompilationRequestBatch collection);

  Map<String, Class<?>> loadClasses(CompilationRequestBatch collection,
                                    ClassLoader classLoader);

  default void clearCache() {

  }

  void close();
}
