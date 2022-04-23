package com.rubenmathews.bytereactor;

import com.rubenmathews.bytereactor.util.ByteReactorUtil;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class ByteReactorFileManager extends ForwardingJavaFileManager<JavaFileManager> {

  protected ByteReactorFileManager(JavaFileManager fileManager) {
    super(fileManager);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                                             JavaFileObject.Kind kind, FileObject fileObject) {
    if (JavaFileObject.Kind.CLASS != kind) {
      throw new IllegalArgumentException("Expected JavaFileObject.Kind to be CLASS but got ["
              + kind + "]");
    }

    try {
      if (fileObject instanceof CompilationUnit) {
        CompiledCode compiledCode = new CompiledCode(className);
        CompilationUnit source = (CompilationUnit) fileObject;
        source.addCompiledCode(className, compiledCode);
        return compiledCode;
      }
      throw new IllegalArgumentException("Expected object of type CompilationUnit but got "
              + fileObject.getClass() + ". " + ByteReactorUtil.REPORT_BUG_MESSAGE);
    } catch (Exception e) {
      throw new CompilationFailedException("Error while creating compilation output file for "
              + className, e);
    }

  }
}
