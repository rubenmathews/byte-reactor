package com.rubenmathews.bytereactor;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.Processor;

public class ByteReactorBuilder {
  private ClassLoader classLoader;
  private DiagnosticReportLevel reportLevel = DiagnosticReportLevel.WARN;
  private Path classDestination;
  private final Set<Processor> processors = new HashSet<>();

  private ByteReactorBuilder() {

  }

  public static ByteReactorBuilder builder() {
    return new ByteReactorBuilder();
  }

  public ByteReactorBuilder withReportLevel(DiagnosticReportLevel reportLevel) {
    this.reportLevel = reportLevel;
    return this;
  }

  public ByteReactorBuilder withCompilerDestinationPath(Path path) {
    this.classDestination = path;
    return this;
  }

  public ByteReactorBuilder withClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public <T extends Processor> ByteReactorBuilder withProcessor(T processor) {
    this.processors.add(processor);
    return this;
  }

  public ByteReactor build() {
    Objects.requireNonNull(reportLevel);
    return new ByteReactor(classLoader, reportLevel, classDestination, processors);
  }
}
