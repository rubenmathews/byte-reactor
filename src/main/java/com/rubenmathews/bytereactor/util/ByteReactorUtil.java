package com.rubenmathews.bytereactor.util;

import com.rubenmathews.bytereactor.CompilationFailedException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaFileObject;

public class ByteReactorUtil {

  public static final String CLASS_EXTENSION = JavaFileObject.Kind.CLASS.extension;
  public static final String SOURCE_EXTENSION = JavaFileObject.Kind.SOURCE.extension;
  public static final String STRING_CONTENT = "string:///";
  public static final String WRITE_TO_FILE_FAILED = "Unable to write content to file";
  public static final String REPORT_BUG_MESSAGE = "This shouldn't have happened. "
          + "Please raise a bug with exception stacktrace, java version, JVM details. ";

  private ByteReactorUtil() {
  }

  public static URI getSourceUriFromClassname(String className) {
    return URI.create(STRING_CONTENT + getClassPath(className) + SOURCE_EXTENSION);
  }

  /**
   * Write content to the given file path.
   *
   * @param filePath The destination file path
   * @param content  The content that has to be written to the file
   */
  public static void writeBytesToFile(Path filePath, byte[] content) {
    try {
      createDirectories(filePath.getParent());
      createFile(filePath);
      Files.write(filePath, content);
    } catch (IOException e) {
      throw new CompilationFailedException(WRITE_TO_FILE_FAILED, e);
    }
  }

  /**
   * Creates directories if the directory path doesn't exist.
   *
   * @param dirPath The Path of the directory that has to be created
   *
   * @throws IOException
   *         If unable to create directory
   */
  public static void createDirectories(Path dirPath) throws IOException {
    if (Files.notExists(dirPath)) {
      Files.createDirectories(dirPath);
    }
  }

  /**
   * Creates a file for the content to be written.
   *
   * @param filePath The file that has to be created.
   *
   * @throws IOException
   *         If unable to create file
   */
  public static void createFile(Path filePath) throws IOException {
    if (Files.notExists(filePath)) {
      Files.createFile(filePath);
    }
  }

  public static String getDestinationClassFile(String className) {
    return getClassPath(className) + CLASS_EXTENSION;
  }

  private static String getClassPath(String className) {
    return className.replace('.', '/');
  }
}
