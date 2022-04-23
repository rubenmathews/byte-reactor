package com.rubenmathews.bytereactor;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericDiagnosticListener implements DiagnosticListener<JavaFileObject> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericDiagnosticListener.class);

  private final DiagnosticReportLevel diagnosticReportLevel;

  public GenericDiagnosticListener() {
    this(DiagnosticReportLevel.ALL);
  }

  public GenericDiagnosticListener(DiagnosticReportLevel reportLevel) {
    this.diagnosticReportLevel = reportLevel;
  }

  @Override
  public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
    switch (diagnostic.getKind()) {
      case ERROR:
        reportErrors(diagnostic.toString());
        break;
      case WARNING:
      case MANDATORY_WARNING:
        reportWarnings(diagnostic.toString());
        break;
      default:
        reportAsInfo(diagnostic.toString());
        break;
    }
  }

  private void reportAsInfo(String message) {
    if (isLevelEnabled(DiagnosticReportLevel.ALL)) {
      LOGGER.info(message);
    }
  }

  private void reportWarnings(String message) {
    if (isLevelEnabled(DiagnosticReportLevel.WARN)) {
      LOGGER.warn(message);
    }
  }

  private void reportErrors(String message) {
    if (isLevelEnabled(DiagnosticReportLevel.ERROR)) {
      LOGGER.error(message);
    }
  }

  private boolean isLevelEnabled(DiagnosticReportLevel reportLevel) {
    return reportLevel.ordinal() >= this.diagnosticReportLevel.ordinal();
  }
}
