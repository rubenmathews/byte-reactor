# ByteReactor
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Frubenmathews%2Fbyte-reactor.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Frubenmathews%2Fbyte-reactor?ref=badge_shield)


ByteReactor is a Runtime Compiler for java. ByteReactor compiles the given source and loads the compiled class

> Note: Requires JDK in Runtime

Install ByteReactor from maven

```xml
<dependency>
    <groupId>com.rubenmathews</groupId>
    <artifactId>byte-reactor</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage

You can use loadClass in ByteReactor to compile and load the class. The classname and the source code has to be provided as an argument to the loadClass method.

```java
interface  SomeInterface {
  String run();
};

String className = "com.rubenmathews.bytereactor.SimpleJavaClass";
String code = "package com.rubenmathews.bytereactor;\n" +
        "\n" +
        "public class SimpleJavaClass implement SomeInterface{\n" +
        "\n" +
        "    @Override \n" +
        "    public String run() {\n" +
        "        return \"Test\";\n" +
        "    }\n" +
        "\n" +
        "}";


ByteReactor byteReactor = ByteReactorBuilder.builder().build();
Class<?> loadedClass = byteReactor.loadClass(new CompilationRequest(code, className));
SomeInterface runner = (SomeInterface) klass.getDeclaredConstructor().newInstance();
runner.run();
byteReactor.clearCache();
byteReactor.close();
```

You could access methods of the compiled class by providing an interface in your java application and the making source code implementing that interface.

### Other ways of loading source
You can also provide a file as source or even a list of sources to be compiled. The list of sources could be files or source code as string

##### Loading source from a file

```java
File code = Paths.get("src/test/resources/SimpleJavaClass.java").toFile();
String className = "com.rubenmathews.bytereactor.SimpleJavaClass";
ByteReactor byteReactor = ByteReactorBuilder.builder().build();
Class<?> loadedClass = byteReactor.loadClass(new CompilationRequest(code, className));
```

##### Providing multiple sources

```java
File code1 = Paths.get("src/test/resources/SimpleJavaClass1.java").toFile();
File code2 = Paths.get("src/test/resources/SimpleJavaClass2.java").toFile();
String className1 = "com.rubenmathews.bytereactor.SimpleJavaClass1";
String className2 = "com.rubenmathews.bytereactor.SimpleJavaClass2";

CompilationRequestBatch batch = CompilationRequestBatch.builder()
        .add(code1, className1)
        .add(code2, className2)
        .build();

ByteReactor byteReactor = ByteReactorBuilder.builder().build();
Class<?> loadedClass = byteReactor.loadClasses(batch);
```

> Note: ByteReactor caches the loaded classes using the classname provided, next time when you provide a classname it loads the classes from the cache.But when the destination path is provided, the source is again compiled even if it's available in the cache.
### Saving Compiled Class

You can also choose to save the compiled the code to a file

```java
File code = Paths.get("src/test/resources/SimpleJavaClass.java").toFile();
String className = "com.rubenmathews.bytereactor.SimpleJavaClass";
Path destinationPath = Paths.get("src/test/target");
ByteReactor byteReactor = ByteReactorBuilder.builder().build();
Class<?> loadedClass = byteReactor.loadClass(new CompilationRequest(code, className, destinationPath));
```

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Frubenmathews%2Fbyte-reactor.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Frubenmathews%2Fbyte-reactor?ref=badge_large)