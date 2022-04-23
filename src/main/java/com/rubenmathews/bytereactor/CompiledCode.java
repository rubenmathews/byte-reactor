package com.rubenmathews.bytereactor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.tools.SimpleJavaFileObject;

public class CompiledCode extends SimpleJavaFileObject {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  private byte[] byteCode = null;

  public CompiledCode(String className) throws URISyntaxException {
    super(new URI(className), Kind.CLASS);
  }

  @Override
  public OutputStream openOutputStream() {
    return outputStream;
  }

  /**
   * Get the compiled bytecode of the class.
   *
   * @return a byte array of the compiled class
   */
  public byte[] getByteCode() {
    if (byteCode == null) {
      byteCode = outputStream.toByteArray();
    }
    return byteCode;
  }

  private void closeOutputStream() {
    try {
      // closing byte output stream has no effect
      outputStream.close();
    } catch (IOException e) {
      throw new CompilationFailedException("Failed to close, This shouldn't have happened");
    }
  }

  public void close() {
    byteCode = null;
    closeOutputStream();
  }
}


