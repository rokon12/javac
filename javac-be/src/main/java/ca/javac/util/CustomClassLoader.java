package ca.javac.util;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.bytecode.ConstPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomClassLoader extends ClassLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(CustomClassLoader.class);
  public static final String JAVA_LANG_SYSTEM = "java/lang/System";
  public static final String JAVA_UTIL_SCANNER = "java/util/Scanner";

  public static final String CUSTOM_SYSTEM = "ca/javac/util/CustomSystem";
  public static final String CUSTOM_SCANNER = "ca/javac/util/CustomScanner";

  private final String classPath;

  public CustomClassLoader(ClassLoader classLoader, String classPath) {
    super(classLoader);
    this.classPath = classPath;
  }

  @Override
  protected Class<?> findClass(String name) {
    String classPath = name.replace(".", "\\") + ".class";
    var resolvedClassPath = Path.of(this.classPath).resolve(classPath);
    Class<?> clazz;
    try {
      byte[] data = getClassFileBytes(resolvedClassPath);
      var classBytes = editByteCode(data);
      clazz = defineClass(null, classBytes, 0, classBytes.length);
    } catch (Exception e) {
      throw new RuntimeException("Unable to load compiled class", e);
    }
    return clazz;
  }

  private byte[] getClassFileBytes(Path classFile) throws IOException {
    return Files.readAllBytes(classFile);
  }

  private byte[] editByteCode(byte[] bytes) throws IOException, CannotCompileException {
    var classPool = new ClassPool();
    classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
    var cc = classPool.makeClass(new ByteArrayInputStream(bytes));
    var classFile = cc.getClassFile();
    var constPool = classFile.getConstPool();

    repalceSystemClass(constPool, JAVA_LANG_SYSTEM, CUSTOM_SYSTEM);
    repalceSystemClass(constPool, JAVA_UTIL_SCANNER, CUSTOM_SCANNER);

    return cc.toBytecode();
  }

  private void repalceSystemClass(ConstPool constPool, String toReplace, String toBeReplacedWith) {
    constPool.getClassNames().stream()
        .filter(toReplace::equals)
        .findAny()
        .ifPresent(exist -> constPool.renameClass(toReplace, toBeReplacedWith));
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    try {
      return super.loadClass(name, resolve);
    } catch (ClassNotFoundException e) {
      var className = name.replace(".", "\\") + ".class";
      var resolvedClassPath = Path.of(this.classPath).resolve(className);
      try {
        var classBytes = getClassFileBytes(resolvedClassPath);
        return defineClass(null, classBytes, 0, classBytes.length);
      } catch (IOException ioException) {
        LOGGER.info("Unable to laod class from byte", ioException);
        throw new ClassNotFoundException(ioException.getMessage());
      }
    }
  }
}
