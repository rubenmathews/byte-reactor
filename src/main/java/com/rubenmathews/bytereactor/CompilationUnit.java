package com.rubenmathews.bytereactor;

import com.rubenmathews.bytereactor.util.ByteReactorUtil;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javax.tools.SimpleJavaFileObject;

public class CompilationUnit extends SimpleJavaFileObject {
  private final String javaCode;
  private final String className;
  private final Map<String, CompiledCode> compiledClasses;
  private final File sourceFile;
  private Path destinationPrefix;

  public CompilationUnit(String code, String className, Path path) {
    this(ByteReactorUtil.getSourceUriFromClassname(className), code, null, className, path);
  }

  public CompilationUnit(File sourceFile, String className, Path path) {
    this(Objects.requireNonNull(sourceFile).toURI(), null, sourceFile, className, path);
  }

  private CompilationUnit(URI source,
                          String javaCode,
                          File sourceFile,
                          String className,
                          Path destinationPrefix) {
    super(source, Kind.SOURCE);
    this.className = className;
    this.destinationPrefix = destinationPrefix;
    this.javaCode = javaCode;
    this.sourceFile = sourceFile;
    this.compiledClasses = Collections.synchronizedMap(new WeakHashMap<>());
  }

  /**
   * Validate the Source File Location if the source is file.
   *
   *  @throws FileNotFoundException
   *          If the file doesn't exist
   */
  public void validateSource() {
    if (sourceFile != null && Files.notExists(sourceFile.toPath())) {
      throw new FileNotFoundException("Unable to locate source file");
    }
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    if (isFileSource()) {
      return new String(Files.readAllBytes(sourceFile.toPath()));
    }
    return javaCode;
  }

  public Set<String> getAvailableClassNames() {
    return compiledClasses.keySet();
  }

  /**
   * Set destination path if the compiled classes have to be written to a file.
   * @param prefix The Directory which the compiled classes have to be written
   */
  public void setDestinationPrefixIfAbsent(Path prefix) {
    if (!hasDestinationPath()) {
      destinationPrefix = prefix;
    }
  }

  /**
   * If destination path is specified, writeToFile will write the compiled classes
   * to the given destination location.
   */
  public void writeToFile() {
    if (hasDestinationPath()) {
      compiledClasses.forEach((compiledClassName, compiledClass) -> {
        String dirPrefix = destinationPrefix.toString();
        String destinationClassFile = ByteReactorUtil.getDestinationClassFile(compiledClassName);
        Path filePath = Paths.get(dirPrefix, destinationClassFile);
        ByteReactorUtil.writeBytesToFile(filePath, compiledClass.getByteCode());
      });
    }
  }

  public boolean isFileSource() {
    return sourceFile != null;
  }

  public String getClassName() {
    return className;
  }

  public void addCompiledCode(String className, CompiledCode innerClass) {
    compiledClasses.put(className, innerClass);
  }

  public Map<String, CompiledCode> getCompiledClasses() {
    return compiledClasses;
  }

  public void close() {
    compiledClasses.values().forEach(CompiledCode::close);
    compiledClasses.clear();
  }

  public boolean hasDestinationPath() {
    return destinationPrefix != null;
  }

}
