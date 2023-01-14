package ca.javac.util;

import ca.javac.util.CustomClassLoader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StringSourceCompiler {

  private final static Pattern CLASS_PATTERN = Pattern.compile(
      "public\\s+class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
  private Path tempClassPath;

  public StringSourceCompiler() {
    try {
      tempClassPath = Files.createTempDirectory("");
      log.info("classPath: {}", tempClassPath);
    } catch (IOException e) {
      log.error("Unable to create temp classPath", e);
    }
  }

  public Optional<Class<?>> compile(String source,
      DiagnosticCollector<JavaFileObject> compileCollector) throws ClassNotFoundException {
    var compiler = ToolProvider.getSystemJavaCompiler();
    var javaFileManager = compiler.getStandardFileManager(compileCollector, Locale.ENGLISH,
        Charset.defaultCharset());
    var className = StringUtils.findClassName(source);
    var stringObject = new TmpJavaSourceObject(className, source);
    var options = List.of("-d", tempClassPath.toString(), "-Xlint:all");
    var result = compiler.getTask(null, javaFileManager, compileCollector,
        options, null, List.of(stringObject)).call();
    if (result) {
      return (loadClass(className));
    }

    return Optional.empty();
  }

  public void deleteTempClassPath() {
    try {
      FileSystemUtils.deleteRecursively(tempClassPath);
    } catch (IOException e) {
      log.error("{} deletion was unsuccessful", tempClassPath, e);
    }
  }

  public Optional<Class<?>> loadClass(String className) throws ClassNotFoundException {
    var classLoader = new CustomClassLoader(getClass().getClassLoader(), tempClassPath.toString());
    return Optional.ofNullable(classLoader.loadClass(className));
  }

  static class TmpJavaSourceObject extends SimpleJavaFileObject {

    private final String contents;

    protected TmpJavaSourceObject(String clasName, String contents) {
      super(URI.create("String:///" + clasName + Kind.SOURCE.extension), Kind.SOURCE);
      this.contents = contents;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return contents;
    }
  }
}
