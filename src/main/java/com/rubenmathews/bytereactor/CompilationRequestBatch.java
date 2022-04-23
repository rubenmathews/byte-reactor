package com.rubenmathews.bytereactor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CompilationRequestBatch {

  private final List<CompilationUnit> compilationUnitList;

  private CompilationRequestBatch(List<CompilationUnit> compilationUnitList) {
    this.compilationUnitList = compilationUnitList;
  }

  List<CompilationUnit> getCollection() {
    return compilationUnitList;
  }

  public static CompilationRequestBatch.Builder builder() {
    return new Builder();
  }

  public void close() {
    compilationUnitList.forEach(CompilationUnit::close);
    compilationUnitList.clear();
  }

  public static class Builder {
    private final List<CompilationUnit> compilationUnitList = new ArrayList<>();

    private Builder() {

    }

    public Builder add(String sourceCode, String className) {
      return add(sourceCode, className, null);
    }

    public Builder add(File source, String className) {
      return add(source, className, null);
    }

    public Builder add(String sourceCode, String className, Path path) {
      compilationUnitList.add(new CompilationUnit(sourceCode, className, path));
      return this;
    }

    public Builder add(File source, String className, Path path) {
      compilationUnitList.add(new CompilationUnit(source, className, path));
      return this;
    }

    public CompilationRequestBatch build() {
      return new CompilationRequestBatch(compilationUnitList);
    }
  }
}
