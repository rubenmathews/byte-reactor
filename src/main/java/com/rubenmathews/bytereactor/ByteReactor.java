package com.rubenmathews.bytereactor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ByteReactor implements RuntimeCompiler {
  private static final Map<ClassLoader, Map<String, Class<?>>> CACHE;
  private static final JavaCompiler JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();
  private static final List<String> OPTIONS;

  private final ByteReactorFileManager javaByteReactorFileManager;
  private final DynamicClassLoader classLoader;
  private final GenericDiagnosticListener diagnosticListener;
  private final Path classDestination;
  private final Set<? extends Processor> processors;

  static {
    OPTIONS = Collections.singletonList("-g");
    CACHE = new WeakHashMap<>();
  }

  protected ByteReactor(ClassLoader classLoader,
                        DiagnosticReportLevel diagnosticReportLevel,
                        Path destinationPrefix,
                        Set<? extends Processor> processors) {
    this.classLoader = classLoader != null ? DynamicClassLoader.instance(classLoader) : null;
    StandardJavaFileManager standardJavaFileManager = JAVA_COMPILER
            .getStandardFileManager(new GenericDiagnosticListener(), null, null);
    this.javaByteReactorFileManager = new ByteReactorFileManager(standardJavaFileManager);
    diagnosticListener = new GenericDiagnosticListener(diagnosticReportLevel);
    this.classDestination = destinationPrefix;
    this.processors = processors;
  }

  @Override
  public Class<?> loadClass(CompilationRequest compilationRequest) {
    return loadClassWithDynamicClassLoader(compilationRequest, getCurrentClassloader());
  }

  @Override
  public Class<?> loadClass(CompilationRequest compilationRequest, ClassLoader classLoader) {
    return loadClassWithDynamicClassLoader(compilationRequest,
            instantiateDynamicClassLoader(classLoader));
  }


  @Override
  public Map<String, Class<?>> loadClasses(CompilationRequestBatch compilationCollection) {
    return loadAllClassWithDynamicClassloader(compilationCollection,
            getCurrentClassloader());
  }

  @Override
  public Map<String, Class<?>> loadClasses(CompilationRequestBatch compilationCollection,
                                           ClassLoader classLoader) {
    return loadAllClassWithDynamicClassloader(compilationCollection,
            instantiateDynamicClassLoader(classLoader));
  }


  private DynamicClassLoader getCurrentClassloader() {
    if (classLoader != null) {
      return classLoader;
    }
    return instantiateDynamicClassLoader(null);
  }

  private Class<?> loadClassWithDynamicClassLoader(CompilationRequest compilationRequest,
                                                   DynamicClassLoader dynamicClassLoader) {
    Objects.requireNonNull(compilationRequest);
    return compileAndLoadClass(dynamicClassLoader, compilationRequest.getCompilationUnit());
  }

  private Map<String, Class<?>> loadAllClassWithDynamicClassloader(
          CompilationRequestBatch compilationCollection,
          DynamicClassLoader classLoader) {
    Objects.requireNonNull(compilationCollection, "Null Compilation request collection provided");
    return compileAndLoadClasses(classLoader, compilationCollection.getCollection());
  }

  private Class<?> compileAndLoadClass(DynamicClassLoader classLoader, CompilationUnit unit) {
    return compileAndLoadClasses(classLoader, Collections.singletonList(unit))
            .get(unit.getClassName());
  }

  private Map<String, Class<?>> compileAndLoadClasses(DynamicClassLoader dynamicClassLoader,
                                                      List<CompilationUnit> compilationUnitList) {
    Map<String, Class<?>> loadedClasses = new HashMap<>();
    List<CompilationUnit> compilationUnits = new ArrayList<>();
    prepareCompilationUnit(compilationUnitList);
    compilationUnitList.forEach(compilation -> {
      Class<?> klass = null;
      /*
       * We will need to compile when the destination path is given even if the classes are cached.
       * Because file can have multiple classes, classes are cached based on canonical class name.
       * We will not know how many classes are present in the same file,so won't be able to retrieve
       * from cache.
       */
      if (!compilation.hasDestinationPath()) {
        klass = getCachedClasses(dynamicClassLoader).get(compilation.getClassName());
      }
      if (klass != null) {
        loadedClasses.put(compilation.getClassName(), klass);
      } else {
        compilationUnits.add(compilation);
      }
    });

    if (compilationUnits.isEmpty()) {
      return loadedClasses;
    }

    JavaCompiler.CompilationTask compilationTask = getCompilationTask(compilationUnits);
    compilationTask.setProcessors(processors);
    if (Boolean.TRUE.equals(compilationTask.call())) {
      compilationUnits.forEach(compilationUnit -> {
        compilationUnit.writeToFile();
        loadedClasses.putAll(cacheAndLoadClass(dynamicClassLoader, compilationUnit));
      });
      return loadedClasses;
    }

    throw new CompilationFailedException("Compilation Failed, check Diagnostic Logs");
  }

  private void prepareCompilationUnit(List<CompilationUnit> compilationUnitList) {
    if (compilationUnitList.isEmpty()) {
      throw new IllegalArgumentException("Nothing to compile");
    }
    for (CompilationUnit compilationUnit : compilationUnitList) {
      compilationUnit.validateSource();
      compilationUnit.setDestinationPrefixIfAbsent(classDestination);
    }
  }

  private DynamicClassLoader instantiateDynamicClassLoader(ClassLoader givenClassLoader) {
    ClassLoader currentClassLoader = givenClassLoader;

    if (currentClassLoader == null) {
      if (classLoader != null) {
        return classLoader;
      }
      currentClassLoader = getThreadContextClassLoader();
    }

    if (currentClassLoader == null) {
      currentClassLoader = getClass().getClassLoader();
    }

    return DynamicClassLoader.instance(currentClassLoader);
  }

  private ClassLoader getThreadContextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }


  private Map<String, Class<?>> getCachedClasses(DynamicClassLoader dynamicClassLoader) {
    synchronized (CACHE) {
      CACHE.computeIfAbsent(dynamicClassLoader, value -> new HashMap<>());
      return CACHE.get(dynamicClassLoader);
    }
  }

  private JavaCompiler.CompilationTask getCompilationTask(List<? extends JavaFileObject> units) {
    return JAVA_COMPILER.getTask(null,
            javaByteReactorFileManager,
            diagnosticListener,
            OPTIONS,
            null,
            units);
  }

  private Map<String, Class<?>> cacheAndLoadClass(DynamicClassLoader classLoader,
                                                  CompilationUnit unit) {
    validateCompiledClass(unit);
    Map<String, Class<?>> loadedClasses = new HashMap<>();
    Map<String, CompiledCode> compiledCodeMap = unit.getCompiledClasses();
    try {
      for (Map.Entry<String, CompiledCode> entry : compiledCodeMap.entrySet()) {
        String className = entry.getKey();
        CompiledCode compiledCode = entry.getValue();
        byte[] bytecode = compiledCode.getByteCode();
        Class<?> currentClass = classLoader.loadClass(bytecode);
        cacheLoadedClass(classLoader, className, currentClass);
        loadedClasses.put(className, currentClass);
      }
    } finally {
      compiledCodeMap.values().forEach(CompiledCode::close);
    }
    return loadedClasses;
  }

  private void validateCompiledClass(CompilationUnit unit) {
    requireNonEmptyCompliedCodes(unit.getCompiledClasses());
    String className = unit.getClassName();
    Set<String> availableClassNames = unit.getAvailableClassNames();
    if (!availableClassNames.contains(className)) {
      throw new IllegalArgumentException("Cannot find class of ["
              + className + "] in the given code");
    }
  }

  private void requireNonEmptyCompliedCodes(Map<?, ?> collection) {
    if (collection.isEmpty()) {
      throw new IllegalArgumentException("No classes found after compilation");
    }
  }

  private void cacheLoadedClass(DynamicClassLoader classLoader, String name, Class<?> klass) {
    synchronized (CACHE) {
      Map<String, Class<?>> loadedClasses = getCachedClasses(classLoader);
      loadedClasses.put(name, klass);
    }
  }

  @Override
  public void clearCache() {
    synchronized (CACHE) {
      CACHE.clear();
    }
  }

  @Override
  public void close() {
    clearAllProcessors();
    clearClassLoaders();
    clearCache();
  }

  public void clearAllProcessors() {
    this.processors.clear();
  }

  public void clearThreadContextClassLoader() {
    DynamicClassLoader.removeClassLoader(getThreadContextClassLoader());
  }

  public void clearClassLoader(ClassLoader classLoader) {
    DynamicClassLoader.removeClassLoader(classLoader);
  }

  public void clearClassLoaders() {
    DynamicClassLoader.clearAllClassLoaders();
  }

}
