package ca.javac.service;

import static ca.javac.util.ExecutionTime.withExecutionTime;

import ca.javac.util.ExecutionResult;
import ca.javac.util.ExecutionStatus;
import ca.javac.util.StringSourceCompiler;
import ca.javac.util.Tuple;
import ca.javac.util.CustomInputStream;
import ca.javac.util.CustomSystem;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompileAndCodeRunnerService {

  private static final String NO_OUTPUT = "Nothing.";

  private final StringSourceCompiler sourceCompiler;

  @Value("${java.compiler.execution.time.limit}")
  private int runtimeLimit;

  @Value("${java.compiler.thread.pool.count.min}")
  private int threadCountMin;

  @Value("${java.compiler.thread.pool.count.max}")
  private int threadCountMax;
  private ExecutorService threadPool;

  @PostConstruct
  public void init() {
    threadPool = new ThreadPoolExecutor(threadCountMin, threadCountMax, 10, TimeUnit.MILLISECONDS,
        new LinkedBlockingDeque<>());
  }

  public ExecutionResult execute(String source, String systemIn) {
    log.info("source: {} , systemIn: {}", source.replaceAll("[\n\r]", ""), systemIn);

    var compileCollector = new DiagnosticCollector<JavaFileObject>();
    var byteCodeWithTime = withExecutionTime(() -> getCompiledClass(source, compileCollector));

    return byteCodeWithTime.getLeft()
        .map(clazz -> {
          var resultWithExecutionTime = withExecutionTime(() -> executeCode(systemIn, clazz));
          var result = resultWithExecutionTime.getLeft();
          return ExecutionResult.builder()
              .result(result.getLeft())
              .status(result.getRight())
              .executionTime(byteCodeWithTime.getRight() + resultWithExecutionTime.getRight())
              .build();
        }).orElseGet(() -> ExecutionResult.builder()
            .result(getErrorMessages(compileCollector))
            .status(ExecutionStatus.FAILED)
            .executionTime(byteCodeWithTime.getRight())
            .build());
  }

  private Optional<Class<?>> getCompiledClass(String source,
      DiagnosticCollector<JavaFileObject> compileCollector) {
    try {
      return sourceCompiler.compile(source, compileCollector);
    } catch (ClassNotFoundException e) {
      log.error("Was not able to compile source", e);
      return Optional.empty();
    }
  }

  private String getErrorMessages(DiagnosticCollector<JavaFileObject> compileCollector) {
    return compileCollector.getDiagnostics()
        .stream()
        .map(diagnostic -> "Compilation error at line %d %s".formatted(diagnostic.getLineNumber(),
            diagnostic.getMessage(
                Locale.getDefault())))
        .collect(Collectors.joining(System.lineSeparator()));
  }

  private Tuple<String, ExecutionStatus> executeCode(String systemIn, Class<?> compiledByteCode) {
    String runResult;
    var future
        = CompletableFuture.supplyAsync(() -> execute(compiledByteCode, systemIn), threadPool);

    try {
      return future.get(runtimeLimit, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Unable to get result, ", e);
      runResult = ("Program interrupted.");
    } catch (ExecutionException e) {
      log.error("Unable to get result, ", e);
      runResult = (e.getCause().getMessage());
    } catch (TimeoutException e) {
      log.error("Unable to get result, ", e);
      runResult = ("Time Limit Exceeded.");
    } finally {
      future.cancel(true);
      sourceCompiler.deleteTempClassPath(); // we don't need the classpath anymore
    }

    return Tuple.<String, ExecutionStatus>builder()
        .left(runResult != null ? runResult : NO_OUTPUT)
        .right(ExecutionStatus.FAILED)
        .build();
  }

  private Tuple<String, ExecutionStatus> execute(Class<?> clazz, String systemIn) {
    ExecutionStatus executionStatus;
    try {
      ((CustomInputStream) CustomSystem.in).set(systemIn);
      var mainMethod = clazz.getMethod("main", String[].class);
      if (StringUtils.isNotEmpty(systemIn)) {
        var input = systemIn.split(" ");
        mainMethod.invoke(null, (Object) input);
      } else {
        mainMethod.invoke(null, (Object) new String[]{});
      }
      executionStatus = ExecutionStatus.SUCCEED;
    } catch (NoSuchMethodException | IllegalAccessException e) {
      log.error("invocation was not successful", e);
      executionStatus = ExecutionStatus.FAILED;
    } catch (InvocationTargetException e) {
           /*
             When an exception is thrown inside the called method but not caught, the exception will be received,
             Since this part of the exception is the exception of remote code execution, we need to feed the exception stack back to the client,
             So you cannot use the default printStackTrace() with no parameters to print information to System.err,
             Instead, print the exception information to CustomSystem.err for feedback to the client
             */
      log.error("error: ", e);
      e.getCause().printStackTrace(CustomSystem.err);
      executionStatus = ExecutionStatus.FAILED;
    }
    String res = CustomSystem.getBufferString();
    CustomSystem.closeBuffer();

    return Tuple.<String, ExecutionStatus>builder()
        .left(res != null ? res : NO_OUTPUT)
        .right(executionStatus)
        .build();
  }

}
