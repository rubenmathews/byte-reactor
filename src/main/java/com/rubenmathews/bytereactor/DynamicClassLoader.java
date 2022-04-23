package com.rubenmathews.bytereactor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {

  private static final Map<ClassLoader, DynamicClassLoader> CLASS_LOADER_STORE;

  static {
    CLASS_LOADER_STORE = Collections.synchronizedMap(new HashMap<>());
  }

  private DynamicClassLoader(ClassLoader classLoader) {
    super(classLoader);
  }

  public Class<?> loadClass(byte[] byteCode) {
    return defineClass(null, byteCode, 0, byteCode.length);
  }

  /**
   *  Instance returns a dynamic classloader for the given classloader.
   *  If the classloader is available in the cache, it returns from the cache.
   *  Else a new instance of dynamic class loader if created stored in cache and returned
   * @param classLoader The parent classloader
   * @return  DynamicClassLoader for the parent classloader
   */
  public static DynamicClassLoader instance(ClassLoader classLoader) {
    synchronized (CLASS_LOADER_STORE) {
      return CLASS_LOADER_STORE.computeIfAbsent(classLoader, DynamicClassLoader::new);
    }
  }

  public static void removeClassLoader(ClassLoader classLoader) {
    CLASS_LOADER_STORE.remove(classLoader);
  }

  public static void clearAllClassLoaders() {
    CLASS_LOADER_STORE.clear();
  }
}
