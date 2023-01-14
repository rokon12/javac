package ca.javac.util;

import java.util.Optional;
import java.util.regex.Pattern;

public class StringUtils {

  private final static Pattern CLASS_PATTERN = Pattern.compile(
      "public\\s+class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");

  public static String findClassName(String source) {
    return validClassNameExists(source)
        .orElseThrow(() -> new IllegalArgumentException("No valid class name found"));
  }

  public static Optional<String> validClassNameExists(final String source) {
    var matcher = CLASS_PATTERN.matcher(source);
    String className;
    if (matcher.find()) {
      className = matcher.group(1);
    } else {
      return Optional.empty();
    }
    return Optional.of(className);
  }
}
