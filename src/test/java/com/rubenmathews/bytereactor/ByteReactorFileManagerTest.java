package com.rubenmathews.bytereactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URI;

class ByteReactorFileManagerTest {

    private static final JavaCompiler JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();
    private static final StandardJavaFileManager standardJavaFileManager = JAVA_COMPILER
            .getStandardFileManager(new GenericDiagnosticListener(), null, null);

    @Test
    void whenJavaFileObjectOfKindOtherThanClassIsGivenForGetJavaFileObject_shouldThrowIllegalArgumentException() {
        ByteReactorFileManager byteReactorFileManager = new ByteReactorFileManager(standardJavaFileManager);
        JavaFileManager.Location testLocation = getTestLocation();
        FileObject testFileObject =  new CompilationUnit("test", "test", null);
        Assertions.assertThatThrownBy(() ->
                        byteReactorFileManager.getJavaFileForOutput(testLocation, "test", JavaFileObject.Kind.OTHER, testFileObject))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Expected JavaFileObject.Kind to be CLASS but got [OTHER]");
    }

    @Test
    void whenFileObjectOtherThanCompilationUnitIsGivenForGetJavaFileObject_shouldThrowIllegalArgumentException() {
        ByteReactorFileManager byteReactorFileManager = new ByteReactorFileManager(standardJavaFileManager);
        JavaFileManager.Location testLocation = getTestLocation();
        FileObject testFileObject = new TestFileObject(URI.create("test"), JavaFileObject.Kind.CLASS);
        Assertions.assertThatThrownBy(() ->
                        byteReactorFileManager.getJavaFileForOutput(testLocation, "test", JavaFileObject.Kind.CLASS, testFileObject))
                .isInstanceOf(CompilationFailedException.class).hasMessageContaining("Error while creating compilation output file for");
    }

    private JavaFileManager.Location getTestLocation() {
        return new JavaFileManager.Location() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean isOutputLocation() {
                return false;
            }
        };
    }

    class TestFileObject extends SimpleJavaFileObject {

        protected TestFileObject(URI uri, Kind kind) {
            super(uri, kind);
        }
    }

}
