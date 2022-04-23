package com.rubenmathews.bytereactor;

import com.rubenmathews.bytereactor.util.ByteReactorUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

class ByteReactorTest {

    private final static Path CLASS_BASE_PATH = Paths.get("com", "rubenmathews", "bytereactor");
    public static final String TEST_PACKAGE_NAME = "com.rubenmathews.bytereactor";
    public static final String SIMPLE_TEST_CLASS_NAME = TEST_PACKAGE_NAME + ".SimpleJavaClass";

    @Nested
    class SimpleCompilationTest {
        @Test
        void whenClassnameAndCodeIsCorrect_shouldCompileAndLoadTheClass() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder()
                    .build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, name));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenClassnameIsIncorrect_shouldThrowException() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClassInvalid";
                String code = getCodeFromFile("SimpleJavaClass.java");
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClass(compilationRequest))
                        .isExactlyInstanceOf(CompilationFailedException.class);
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenInvalidCodeIsPresent_shouldThrowException() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".InvalidCode";
                String code = getCodeFromFile("InvalidCode.java");
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClass(compilationRequest))
                        .isExactlyInstanceOf(CompilationFailedException.class);
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenNoCodeIsPresentInTheGivenString_shouldThrowException() {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".InvalidCode";
                String code = "";
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClass(compilationRequest))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("No classes found after compilation");
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenClassnameAndCodeHasWarningContent_shouldCompileAndLoadTheClass() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().withProcessor(new WarnProcessor())
                    .withReportLevel(DiagnosticReportLevel.ALL)
                    .build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("WarningProducingJavaClass.java");
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, name));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("warn");
            } finally {
                runtimeCompiler.close();
            }
        }
    }

    @Nested
    class FileBasedCompilationTest {
        @Test
        void whenSourceCodeIsProvidedAsFile_shouldLoadTheClass() throws Exception {
            ByteReactor runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, name));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenSourceCodeIsProvidedAsFileButClassNameIsWrong_shouldThrowException() throws Exception {
            ByteReactor runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass1";
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClass(compilationRequest))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Cannot find class of [" + name + "]");
            } finally {
                runtimeCompiler.close();
            }
        }


        @Test
        void whenSourceCodeIsProvidedAsFile_butFileDoesNotExists_shouldThrowFileNotFoundException() throws Exception {
            ByteReactor runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                File code = getResourceFilePath("SimpleJavaClassNonExistent.java").toFile();
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClass(compilationRequest))
                        .isExactlyInstanceOf(FileNotFoundException.class);
            } finally {
                runtimeCompiler.close();
            }
        }
    }

    @Nested
    class ClassToClassFileTest {
        @Test
        void whenSourceCodeIsTextAndDestinationPathIsProvided_shouldCompileAndWriteToDestinationLocation(@TempDir File tempDir) throws Exception {
            Assertions.assertThat(tempDir).isDirectory();
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String code = getCodeFromFile("SimpleJavaClass.java");
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME, tempDir.toPath()));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Path destinationPath = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThat(Files.readAllBytes(destinationPath)).isNotEmpty();
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenSourceCodeIsFileAndDestinationPathIsProvided_shouldCompileAndWriteToDestinationLocation(@TempDir File tempDir) throws Exception {
            Assertions.assertThat(tempDir).isDirectory();
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME, tempDir.toPath()));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Path destinationPath = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThat(Files.readAllBytes(destinationPath)).isNotEmpty();
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenPathIsProvidedInTheCompiler_andSourceWithoutPathIsGiven_shouldSaveTheClassToCompilerSpecifiedPath(@TempDir File tempDir) throws Exception {
            Assertions.assertThat(tempDir).isDirectory();
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().withCompilerDestinationPath(tempDir.toPath()).build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Path compilerDestinationPath = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThat(Files.readAllBytes(compilerDestinationPath)).isNotEmpty();
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenPathIsProvidedInCompiler_andSourceWithADifferentPathIsGiven_shouldSaveTheClassToSourceProvidedPath(@TempDir File tempDir) throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().withCompilerDestinationPath(tempDir.toPath()).build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                Path destinationPath = tempDir.toPath().resolve(Paths.get("test"));
                CompilationRequest compilationRequest = new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME, destinationPath);
                Class<?> klass = runtimeCompiler.loadClass(compilationRequest);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Path compilerDestinationPath = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThatThrownBy(() ->
                        Files.readAllBytes(compilerDestinationPath)
                ).isInstanceOf(NoSuchFileException.class);

                Path actualClassPath = destinationPath.resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThat(Files.readAllBytes(actualClassPath)).isNotEmpty();
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenUnableToWriteToGivenPath_shouldThrowCompilationFailedException(@TempDir File tempDir) throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                Path tempFile = tempDir.toPath().resolve("Sample.txt");
                Files.write(tempFile, new byte[]{});
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                CompilationRequest compilationRequest = new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME, tempFile);
                Assertions.assertThatThrownBy(() ->
                        runtimeCompiler.loadClass(compilationRequest)
                ).isInstanceOf(CompilationFailedException.class);
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenPathIsProvidedInCompiler_butTheFileAlreadyExists_shouldOverwriteOnCompile(@TempDir File tempDir) throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().withCompilerDestinationPath(tempDir.toPath()).build();
            try {
                File code = getResourceFilePath("SimpleJavaClass.java").toFile();
                Path destinationPath = tempDir.toPath().resolve(Paths.get("test"));
                Path actualClassPath = destinationPath.resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                byte[] someContent = "hello".getBytes(StandardCharsets.UTF_8);
                ByteReactorUtil.writeBytesToFile(actualClassPath, someContent);
                Assertions.assertThat(Files.readAllBytes(actualClassPath)).contains(someContent);

                CompilationRequest compilationRequest = new CompilationRequest(code, SIMPLE_TEST_CLASS_NAME, destinationPath);
                Class<?> klass = runtimeCompiler.loadClass(compilationRequest);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Assertions.assertThat(Files.readAllBytes(actualClassPath)).isNotEqualTo(someContent);
            } finally {
                runtimeCompiler.close();
            }
        }

    }

    @Nested
    class MultiClassTest {
        @Test
        void whenMultipleClassesArePresentInTheSource_shouldCompileAndLoadTheCorrectClass() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String code = getCodeFromFile("MultipleClass.java");
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, TEST_PACKAGE_NAME + ".MultipleClass"));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Result From Another class");
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenInnerClassIsPresent_shouldCompileAndLoadTheClass() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String code = getCodeFromFile("InnerClassWithMultipleClass.java");
                Class<?> klass = runtimeCompiler.loadClass(new CompilationRequest(code, TEST_PACKAGE_NAME + ".InnerClassWithMultipleClass"));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Response From Inner Class.Result From Another class");
            } finally {
                runtimeCompiler.close();
            }
        }
    }

    @Nested
    class CodeCollectionTest {

        @Test
        void whenCodeCollectionIsProvided_shouldCompileAndLoadTheClasses() throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                String code1 = getCodeFromFile("InnerClassWithMultipleClass.java");
                File code2 = getResourceFilePath("SimpleJavaClass.java").toFile();
                String multiClassClassName = TEST_PACKAGE_NAME + ".InnerClassWithMultipleClass";
                CompilationRequestBatch collection = CompilationRequestBatch.builder()
                        .add(code1, multiClassClassName)
                        .add(code2, SIMPLE_TEST_CLASS_NAME)
                        .build();

                Map<String, Class<?>> classes = runtimeCompiler.loadClasses(collection);
                Class<?> klass1 = classes.get(SIMPLE_TEST_CLASS_NAME);
                Class<?> klass2 = classes.get(multiClassClassName);
                TestRunnable simpleTestRunner = (TestRunnable) klass1.getDeclaredConstructor().newInstance();
                Assertions.assertThat(simpleTestRunner.run()).isEqualTo("Test");
                TestRunnable testRunner = (TestRunnable) klass2.getDeclaredConstructor().newInstance();
                Assertions.assertThat(testRunner.run()).isEqualTo("Response From Inner Class.Result From Another class");
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenCodeCollectionIsProvidedWithPathsForFewClasses_shouldLoadClassesAndSaveClassToThosePathsAndOthersToCompilerSpecifiedLocation(@TempDir File tempDir) throws Exception {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().withCompilerDestinationPath(tempDir.toPath()).build();
            try {
                String code1 = getCodeFromFile("InnerClassWithMultipleClass.java");
                File code2 = getResourceFilePath("SimpleJavaClass.java").toFile();
                String multiClassClassName = TEST_PACKAGE_NAME + ".InnerClassWithMultipleClass";
                Path anotherDirPath = tempDir.toPath().resolve(Paths.get("anotherDir"));
                CompilationRequestBatch collection = CompilationRequestBatch.builder()
                        .add(code1, multiClassClassName)
                        .add(code2, SIMPLE_TEST_CLASS_NAME, anotherDirPath)
                        .build();

                Map<String, Class<?>> classes = runtimeCompiler.loadClasses(collection);
                Class<?> klass1 = classes.get(SIMPLE_TEST_CLASS_NAME);
                Class<?> klass2 = classes.get(multiClassClassName);
                TestRunnable simpleTestRunner = (TestRunnable) klass1.getDeclaredConstructor().newInstance();
                Assertions.assertThat(simpleTestRunner.run()).isEqualTo("Test");
                TestRunnable testRunner = (TestRunnable) klass2.getDeclaredConstructor().newInstance();
                Assertions.assertThat(testRunner.run()).isEqualTo("Response From Inner Class.Result From Another class");

                Path mainClass = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("InnerClassWithMultipleClass.class");
                Path innerClass = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("InnerClassWithMultipleClass$InnerClass.class");
                Path otherClass = tempDir.toPath().resolve(CLASS_BASE_PATH).resolve("RunnableCode.class");
                Path simpleJavaClassDestinationPath = anotherDirPath.resolve(CLASS_BASE_PATH).resolve("SimpleJavaClass.class");
                Assertions.assertThat(Files.readAllBytes(mainClass)).isNotEmpty();
                Assertions.assertThat(Files.readAllBytes(innerClass)).isNotEmpty();
                Assertions.assertThat(Files.readAllBytes(otherClass)).isNotEmpty();
                Assertions.assertThat(Files.readAllBytes(simpleJavaClassDestinationPath)).isNotEmpty();
            } finally {
                runtimeCompiler.close();
            }
        }

        @Test
        void whenCodeCollectionIsProvidedWithEmptyList_shouldThrowException() {
            RuntimeCompiler runtimeCompiler = ByteReactorBuilder.builder().build();
            try {
                CompilationRequestBatch collection = CompilationRequestBatch.builder()
                        .build();

                Assertions.assertThatThrownBy(() -> runtimeCompiler.loadClasses(collection))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Nothing to compile");
            } finally {
                runtimeCompiler.close();
            }
        }
    }

    @Nested
    class ClassLoaderTest {

        @Test
        void whenClassLoaderIsGivenInCompiler_shouldUseClassLevelClassLoader() throws Exception {
            ClassLoader classLevelClassLoader = new TestClassLoader();
            ByteReactor byteReactor = ByteReactorBuilder.builder().withClassLoader(classLevelClassLoader).build(); ;
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Class<?> klass = byteReactor.loadClass(compilationRequest);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Assertions.assertThat(klass.getClassLoader().getParent()).isEqualTo(classLevelClassLoader);
            } finally {
                byteReactor.close();
            }
        }


        @Test
        void whenLoadingSameClassesIntoSameClassloader_shouldThrowException() throws Exception {
            ClassLoader classLevelClassLoader = new TestClassLoader();
            ByteReactor byteReactor = ByteReactorBuilder.builder().withClassLoader(classLevelClassLoader).build(); ;
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Class<?> klass = byteReactor.loadClass(compilationRequest);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                byteReactor.clearCache();
                byteReactor.clearClassLoaders();
                Assertions.assertThatThrownBy(() -> byteReactor.loadClass(compilationRequest))
                        .isInstanceOf(LinkageError.class);
            } finally {
                byteReactor.close();
            }
        }

        @Test
        void whenNewClassLoaderIsGivenForLoadClass_shouldUseThatSpecifiedClassLoader() throws Exception {
            ClassLoader classLevelClassLoader = new TestClassLoader();
            ByteReactor byteReactor = ByteReactorBuilder.builder().withClassLoader(classLevelClassLoader).build(); ;
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code1 = getCodeFromFile("SimpleJavaClass.java");
                String code2 = getCodeFromFile("SimpleJavaClassWithContentChange.java");
                Class<?> klass1 = byteReactor.loadClass(new CompilationRequest(code1, name));
                ClassLoader newClassLoader = new TestClassLoader();
                Class<?> klass2 = byteReactor.loadClass(new CompilationRequest(code2, name), newClassLoader);

                TestRunnable runner = (TestRunnable) klass1.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Assertions.assertThat(klass1.getClassLoader().getParent()).isEqualTo(classLevelClassLoader);

                TestRunnable runner2 = (TestRunnable) klass2.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner2.run()).isEqualTo("Test2");
                Assertions.assertThat(klass2.getClassLoader().getParent()).isEqualTo(newClassLoader);
            } finally {
                byteReactor.close();
            }
        }


        @Test
        void whenNewClassLoaderIsGivenForLoadClasses_shouldUseThatSpecifiedClassLoader() throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().withClassLoader(ClassLoader.getSystemClassLoader()).build(); ;
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                CompilationRequestBatch collection = CompilationRequestBatch.builder()
                        .add(code, name)
                        .build();
                Map<String, Class<?>> classMap = byteReactor.loadClasses(collection);
                TestClassLoader classLoader = new TestClassLoader();
                Map<String, Class<?>> classMap2 = byteReactor.loadClasses(collection, classLoader);

                Class<?> klass1 = classMap.get(name);
                TestRunnable runner = (TestRunnable) klass1.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");

                Class<?> klass2 = classMap2.get(name);
                TestRunnable runner2 = (TestRunnable) klass2.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner2.run()).isEqualTo("Test");
                Assertions.assertThat(klass2.getClassLoader().getParent()).isEqualTo(classLoader);

            } finally {
                byteReactor.close();
            }
        }

        @Test
        void whenNullNewClassLoaderIsGivenForLoadClassButClassLevelClassLoaderIsGiven_shouldUseClassLevelClassLoader() throws Exception {
            ClassLoader classLevelClassLoader = new TestClassLoader();
            ByteReactor byteReactor = ByteReactorBuilder.builder().withClassLoader(classLevelClassLoader).build(); ;
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                ClassLoader newClassLoader = new TestClassLoader();
                Class<?> klass = byteReactor.loadClass(new CompilationRequest(code, name), null);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Assertions.assertThat(klass.getClassLoader().getParent()).isEqualTo(classLevelClassLoader);
            } finally {
                byteReactor.close();
            }
        }

        @Test
        void whenNullNewClassLoaderIsGivenForLoadClassAndNoClassLevelClassLoaderIsGiven_shouldUseThreadContextClassLoader() throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                ClassLoader newClassLoader = new TestClassLoader();
                Class<?> klass = byteReactor.loadClass(new CompilationRequest(code, name), null);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
            } finally {
                byteReactor.close();
            }
        }


        @Test
        void whenClearingThreadContextClassLoader_shouldRemoveThreadContextClassloaderFromStore() throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                ClassLoader threadContextDynamicClassLoader = DynamicClassLoader.instance(Thread.currentThread().getContextClassLoader());
                Class<?> klass = byteReactor.loadClass(new CompilationRequest(code, name));
                Assertions.assertThat(klass.getClassLoader()).isEqualTo(threadContextDynamicClassLoader);
                byteReactor.clearThreadContextClassLoader();
                byteReactor.clearCache();
                Class<?> klass1 = byteReactor.loadClass(new CompilationRequest(code, name));
                Assertions.assertThat(klass1.getClassLoader()).isNotEqualTo(threadContextDynamicClassLoader);
            } finally {
                byteReactor.close();
            }
        }


        @Test
        void whenNullNewClassLoaderIsGivenForLoadClassAndNoClassLevelClassLoaderAndThreadContextClassLoaderIsNull_shouldUseDefaultClassLoader() throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().build();
            ClassLoader threadContextLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(null);
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClassWithContentChange.java");
                ClassLoader newClassLoader = new TestClassLoader();
                Class<?> klass = byteReactor.loadClass(new CompilationRequest(code, name), null);
                TestRunnable runner2 = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner2.run()).isEqualTo("Test2");
            } finally {
                Thread.currentThread().setContextClassLoader(threadContextLoader);
                byteReactor.close();
            }
        }
    }

    @Nested
    class CacheTest {
        @Test
        void whenClassIsLoadedAndInCache_andOnLoadClass_shouldReturnFromCache() throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                CompilationRequest compilationRequest = new CompilationRequest(code, name);
                Class<?> klass = byteReactor.loadClass(compilationRequest);
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                Class<?> cachedKlass = byteReactor.loadClass(compilationRequest);
                Assertions.assertThat(cachedKlass).isEqualTo(klass);
                byteReactor.clearCache();
                Assertions.assertThatThrownBy(() ->
                        byteReactor.loadClass(compilationRequest)
                ).isInstanceOf(LinkageError.class);
            } finally {
                byteReactor.close();
            }
        }

        @Test
        void whenClassIsLoadedAndInCache_andOnLoadClassWithDifferentClassLoader_shouldReturnNewClass(@TempDir File tempDir) throws Exception {
            ByteReactor byteReactor = ByteReactorBuilder.builder().build();
            try {
                String name = TEST_PACKAGE_NAME + ".SimpleJavaClass";
                String code = getCodeFromFile("SimpleJavaClass.java");
                Class<?> klass = byteReactor.loadClass(new CompilationRequest(code, name));
                TestRunnable runner = (TestRunnable) klass.getDeclaredConstructor().newInstance();
                Assertions.assertThat(runner.run()).isEqualTo("Test");
                byteReactor.clearClassLoaders();
                Class<?> cachedKlass = byteReactor.loadClass(new CompilationRequest(code, name, tempDir.toPath()), ClassLoader.getSystemClassLoader());
                Assertions.assertThat(cachedKlass).isNotEqualTo(klass);
            } finally {
                byteReactor.close();
            }
        }
    }


    private String getCodeFromFile(String filePath) throws IOException {
        byte[] content = Files.readAllBytes(getResourceFilePath(filePath));
        return new String(content);
    }

    private Path getResourceFilePath(String filePath) {
        return Paths.get("src/test/resources", filePath);
    }

    static class TestClassLoader extends ClassLoader {

    }

}
