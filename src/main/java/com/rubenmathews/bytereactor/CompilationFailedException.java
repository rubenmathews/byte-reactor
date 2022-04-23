package com.rubenmathews.bytereactor;

public class CompilationFailedException extends RuntimeException {

  public CompilationFailedException(String message) {
    super(message);
  }

  public CompilationFailedException(String message, Throwable e) {
    super(message, e);
  }
}
