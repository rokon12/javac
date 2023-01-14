package ca.javac.util;

public class OutputLimitReachedException extends RuntimeException {
  public OutputLimitReachedException(String message) {
    super(message);
  }
}
