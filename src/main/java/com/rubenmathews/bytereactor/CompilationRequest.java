package com.rubenmathews.bytereactor;

import java.io.File;
import java.nio.file.Path;

public final class CompilationRequest {

  private final CompilationUnit compilationUnit;

  public CompilationRequest(String sourceCode, String className) {
    this(sourceCode, className, null);
  }

  public CompilationRequest(File source, String className) {
    this(source, className, null);
  }

  public CompilationRequest(String sourceCode, String className, Path path) {
    compilationUnit = new CompilationUnit(sourceCode, className, path);
  }

  public CompilationRequest(File source, String className, Path path) {
    compilationUnit = new CompilationUnit(source, className, path);
  }

  CompilationUnit getCompilationUnit() {
    return compilationUnit;
  }
}
