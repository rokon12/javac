package ca.javac.util;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class ExecutionTime {
  public static <T> Tuple<T, Long> withExecutionTime(Supplier<T> supplier) {
    var start = Instant.now();
    var t = supplier.get();
    var millis = Duration.between(start, Instant.now()).toMillis();
    return new Tuple<>(t, millis);
  }
}
